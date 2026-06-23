package de.luckymcdev.foundryengine.client.imgui.backend;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.buffers.Std140Builder;
import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.ColorTargetState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.shaders.ShaderSource;
import com.mojang.blaze3d.shaders.UniformType;
import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.GpuDevice;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.*;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import de.luckymcdev.foundryengine.common.Common;
import imgui.*;
import imgui.flag.ImGuiBackendFlags;
import imgui.type.ImInt;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.ApiStatus;
import org.joml.Matrix4f;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.NativeResource;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;

@ApiStatus.Internal
public final class ImGuiRenderer implements NativeResource {

    private static final Identifier VERTEX_SHADER_ID = Common.id("imgui_shader_vertex");
    private static final Identifier FRAGMENT_SHADER_ID = Common.id("imgui_shader_fragment");
    private static final Identifier PIPELINE_ID = Common.id("pipeline/imgui");

    private static final String VERTEX_SOURCE = """
            #version 410 core
            layout (location = 0) in vec2 Position;
            layout (location = 1) in vec2 UV;
            layout (location = 2) in vec4 Color;
            layout(std140) uniform Projection {
                mat4 ProjMtx;
            };
            out vec2 Frag_UV;
            out vec4 Frag_Color;
            void main()
            {
                Frag_UV = UV;
                Frag_Color = Color;
                gl_Position = ProjMtx * vec4(Position.xy, 0, 1);
            }
            """;

    private static final String FRAGMENT_SOURCE = """
            #version 410 core
            in vec2 Frag_UV;
            in vec4 Frag_Color;
            uniform sampler2D Texture;
            layout (location = 0) out vec4 Out_Color;
            void main()
            {
                Out_Color = Frag_Color * texture(Texture, Frag_UV.st);
            }
            """;

    private static final ShaderSource SHADER_SOURCE = (id, type) -> {
        if (id.equals(VERTEX_SHADER_ID)) return VERTEX_SOURCE;
        if (id.equals(FRAGMENT_SHADER_ID)) return FRAGMENT_SOURCE;
        return null;
    };

    private static VertexFormat VERTEX_FORMAT;
    private static final RenderPipeline PIPELINE = RenderPipeline.builder()
            .withLocation(PIPELINE_ID)
            .withVertexShader(VERTEX_SHADER_ID)
            .withFragmentShader(FRAGMENT_SHADER_ID)
            .withSampler("Texture")
            .withUniform("Projection", UniformType.UNIFORM_BUFFER)
            .withColorTargetState(new ColorTargetState(Optional.of(BlendFunction.TRANSLUCENT), ColorTargetState.WRITE_ALL))
            .withCull(false)
            .withVertexFormat(VERTEX_FORMAT, VertexFormat.Mode.TRIANGLES)
            .build();

    static {
        VertexFormatElement posElement = null;
        for (int i = 7; i < VertexFormatElement.MAX_COUNT; i++) {
            VertexFormatElement element = VertexFormatElement.byId(i);
            if (element == null) {
                posElement = VertexFormatElement.register(i, 0, VertexFormatElement.Type.FLOAT, false, 2);
                break;
            }
        }
        if (posElement == null) {
            throw new IllegalStateException("Failed to create custom vertex format for ImGui");
        }
        VERTEX_FORMAT = VertexFormat.builder()
                .add("Position", posElement)
                .add("UV", VertexFormatElement.UV0)
                .add("Color", VertexFormatElement.COLOR)
                .build();
    }

    private final List<GpuTextureView> textures = new ArrayList<>();
    private final List<GpuBuffer> vertexBuffers = new ArrayList<>();
    private final List<GpuBuffer> indexBuffers = new ArrayList<>();
    private GpuTexture fontTexture;
    private GpuTextureView fontTextureView;
    private CachedImguiOrthoBuffer orthoBuffer;
    private GpuSampler linearSampler;
    private int elementSize;
    private boolean initialized;

    public void init() {
        GpuDevice device = RenderSystem.getDevice();
        device.precompilePipeline(PIPELINE, SHADER_SOURCE);

        ImGuiIO io = ImGui.getIO();
        io.setBackendRendererName("foundryengine_imgui_renderer");
        io.addBackendFlags(ImGuiBackendFlags.RendererHasVtxOffset);

        orthoBuffer = new CachedImguiOrthoBuffer(-1.0F, 1.0F);
        linearSampler = RenderSystem.getSamplerCache().getClampToEdge(FilterMode.LINEAR);

        initialized = true;
    }

    @Override
    public void free() {
        if (!initialized) return;

        destroyDeviceObjects();

        if (linearSampler != null) {
            linearSampler.close();
            linearSampler = null;
        }

        ImGuiIO io = ImGui.getIO();
        io.setBackendRendererName(null);
        io.removeBackendFlags(ImGuiBackendFlags.RendererHasVtxOffset | ImGuiBackendFlags.RendererHasViewports);
        initialized = false;
    }

    public void newFrame() {
        if (fontTexture == null) {
            createFontsTexture();
        }
    }

    public void renderDrawData(ImDrawData drawData, RenderTarget renderTarget) {
        renderDrawData(drawData, renderTarget, OptionalInt.empty());
    }

    public void renderDrawData(ImDrawData drawData, RenderTarget renderTarget, OptionalInt clearColor) {
        GpuDevice device = RenderSystem.getDevice();
        device.precompilePipeline(PIPELINE, SHADER_SOURCE);

        int fbWidth = (int) (drawData.getDisplaySizeX() * drawData.getFramebufferScaleX());
        int fbHeight = (int) (drawData.getDisplaySizeY() * drawData.getFramebufferScaleY());
        if (fbWidth <= 0 || fbHeight <= 0) {
            clearVertexBuffers(0);
            return;
        }

        int cmdListsCount = drawData.getCmdListsCount();
        if (cmdListsCount <= 0) {
            clearVertexBuffers(0);
            return;
        }

        float L = drawData.getDisplayPosX();
        float R = drawData.getDisplayPosX() + drawData.getDisplaySizeX();
        float T = drawData.getDisplayPosY();
        float B = drawData.getDisplayPosY() + drawData.getDisplaySizeY();

        float clipOffX = drawData.getDisplayPosX();
        float clipOffY = drawData.getDisplayPosY();
        float clipScaleX = drawData.getFramebufferScaleX();
        float clipScaleY = drawData.getFramebufferScaleY();

        if (ImDrawData.sizeOfImDrawIdx() != elementSize) {
            clearIndexBuffers();
        }
        elementSize = ImDrawData.sizeOfImDrawIdx();
        clearVertexBuffers(cmdListsCount);

        CommandEncoder encoder = device.createCommandEncoder();

        for (int n = 0; n < cmdListsCount; n++) {
            int vertexBufferSize = drawData.getCmdListVtxBufferSize(n) * ImDrawData.sizeOfImDrawVert();
            int indexBufferSize = drawData.getCmdListIdxBufferSize(n) * elementSize;

            GpuBuffer vertexBuffer = getOrCreateVertexBuffer(n, vertexBufferSize);
            GpuBuffer indexBuffer = getOrCreateIndexBuffer(n, indexBufferSize);

            encoder.writeToBuffer(vertexBuffer.slice(0, vertexBufferSize), drawData.getCmdListVtxBufferData(n));
            encoder.writeToBuffer(indexBuffer.slice(0, indexBufferSize), drawData.getCmdListIdxBufferData(n));
        }

        if (clearColor.isPresent()) {
            encoder.clearColorTexture(renderTarget.getColorTexture(), clearColor.getAsInt());
        }

        GpuBufferSlice projSlice = orthoBuffer.getBuffer(L, R, B, T);

        GpuTextureView colorTexture = renderTarget.getColorTextureView();
        try (RenderPass pass = encoder.createRenderPass(
                () -> "ImGui",
                colorTexture,
                OptionalInt.empty())) {

            pass.setPipeline(PIPELINE);
            pass.setUniform("Projection", projSlice);

            for (int n = 0; n < cmdListsCount; n++) {
                GpuBuffer vtxBuf = vertexBuffers.get(n);
                GpuBuffer idxBuf = indexBuffers.get(n);

                pass.setVertexBuffer(0, vtxBuf);
                pass.setIndexBuffer(idxBuf, elementSize == 2 ? VertexFormat.IndexType.SHORT : VertexFormat.IndexType.INT);

                int cmdBufferSize = drawData.getCmdListCmdBufferSize(n);
                var clipRect = new ImVec4();
                for (int cmdIdx = 0; cmdIdx < cmdBufferSize; cmdIdx++) {
                    drawData.getCmdListCmdBufferClipRect(clipRect, n, cmdIdx);
                    float clipMinX = (clipRect.x - clipOffX) * clipScaleX;
                    float clipMinY = (clipRect.y - clipOffY) * clipScaleY;
                    float clipMaxX = (clipRect.z - clipOffX) * clipScaleX;
                    float clipMaxY = (clipRect.w - clipOffY) * clipScaleY;

                    if (clipMaxX <= clipMinX || clipMaxY <= clipMinY) continue;

                    int minX = Math.max((int) clipMinX, 0);
                    int minY = Math.max((int) (fbHeight - clipMaxY), 0);

                    int scissorWidth = Math.clamp((int) (clipMaxX - clipMinX), 0, colorTexture.getWidth(0) - minX);
                    int scissorHeight = Math.clamp((int) (clipMaxY - clipMinY), 0, colorTexture.getHeight(0) - minY);
                    pass.enableScissor(minX, minY, scissorWidth, scissorHeight);

                    long textureId = drawData.getCmdListCmdBufferTextureId(n, cmdIdx);
                    int vtxOffset = drawData.getCmdListCmdBufferVtxOffset(n, cmdIdx);
                    int idxOffset = drawData.getCmdListCmdBufferIdxOffset(n, cmdIdx);
                    int elemCount = drawData.getCmdListCmdBufferElemCount(n, cmdIdx);

                    if (textureId == 1) {
                        pass.bindTexture("Texture", fontTextureView, linearSampler);
                    } else {
                        int idx = (int) (textureId - 2);
                        if (idx >= 0 && idx < textures.size()) {
                            pass.bindTexture("Texture", textures.get(idx), linearSampler);
                        }
                    }

                    pass.drawIndexed(vtxOffset, idxOffset, elemCount, 1);
                }
            }
        }
    }

    public void postDraw() {
        clearTextures();
    }

    public void discard() {
        clearTextures();
        clearVertexBuffers(0);
    }

    public void recreateFontsTexture() {
        destroyFontsTexture();
        createFontsTexture();
    }

    public long registerTexture(GpuTextureView view) {
        textures.add(view);
        return textures.size() + 1;
    }

    public void clearTextures() {
        textures.clear();
    }

    public void createFontsTexture() {
        ImFontAtlas fontAtlas = ImGui.getIO().getFonts();
        ImInt width = new ImInt();
        ImInt height = new ImInt();
        ByteBuffer pixels = fontAtlas.getTexDataAsRGBA32(width, height);

        GpuDevice device = RenderSystem.getDevice();
        fontTexture = device.createTexture(
                "ImGui Font Atlas",
                GpuTexture.USAGE_COPY_DST | GpuTexture.USAGE_TEXTURE_BINDING,
                TextureFormat.RGBA8,
                width.get(),
                height.get(),
                1,
                1);
        device.createCommandEncoder().writeToTexture(
                fontTexture,
                pixels,
                NativeImage.Format.RGBA,
                0,
                0,
                0,
                0,
                width.get(),
                height.get());
        fontTextureView = device.createTextureView(fontTexture);

        fontAtlas.setTexID(1);
    }

    public void destroyFontsTexture() {
        if (fontTextureView != null) {
            fontTextureView.close();
            fontTextureView = null;
        }
        if (fontTexture != null) {
            fontTexture.close();
            ImGui.getIO().getFonts().setTexID(0);
            fontTexture = null;
        }
    }

    private void destroyDeviceObjects() {
        orthoBuffer.close();
        clearVertexBuffers(0);
        clearIndexBuffers();
        destroyFontsTexture();
    }

    private GpuBuffer getOrCreateVertexBuffer(int index, int size) {
        GpuDevice device = RenderSystem.getDevice();
        while (vertexBuffers.size() <= index) {
            int idx = vertexBuffers.size();
            vertexBuffers.add(device.createBuffer(() -> "ImGui Vertex Buffer " + idx,
                    GpuBuffer.USAGE_COPY_DST | GpuBuffer.USAGE_VERTEX, 1));
        }
        GpuBuffer buffer = vertexBuffers.get(index);
        if (buffer.size() < size) {
            buffer.close();
            buffer = device.createBuffer(() -> "ImGui Vertex Buffer " + index,
                    GpuBuffer.USAGE_COPY_DST | GpuBuffer.USAGE_VERTEX, size);
            vertexBuffers.set(index, buffer);
        }
        return buffer;
    }

    private GpuBuffer getOrCreateIndexBuffer(int index, int size) {
        GpuDevice device = RenderSystem.getDevice();
        while (indexBuffers.size() <= index) {
            int idx = indexBuffers.size();
            indexBuffers.add(device.createBuffer(() -> "ImGui Index Buffer " + idx,
                    GpuBuffer.USAGE_COPY_DST | GpuBuffer.USAGE_INDEX, 1));
        }
        GpuBuffer buffer = indexBuffers.get(index);
        if (buffer.size() < size) {
            buffer.close();
            buffer = device.createBuffer(() -> "ImGui Index Buffer " + index,
                    GpuBuffer.USAGE_COPY_DST | GpuBuffer.USAGE_INDEX, size);
            indexBuffers.set(index, buffer);
        }
        return buffer;
    }

    private void clearVertexBuffers(int keepCount) {
        while (vertexBuffers.size() > keepCount) {
            vertexBuffers.removeLast().close();
        }
    }

    private void clearIndexBuffers() {
        for (GpuBuffer buf : indexBuffers) {
            buf.close();
        }
        indexBuffers.clear();
    }

    private static final class CachedImguiOrthoBuffer implements AutoCloseable {
        private final GpuBuffer buffer;
        private final GpuBufferSlice slice;
        private final float zNear;
        private final float zFar;
        private final Matrix4f projectionMatrix;
        private float left;
        private float right;
        private float bottom;
        private float top;

        CachedImguiOrthoBuffer(float zNear, float zFar) {
            this.zNear = zNear;
            this.zFar = zFar;
            this.projectionMatrix = new Matrix4f();
            this.buffer = RenderSystem.getDevice().createBuffer(
                    () -> "Projection matrix UBO ImGui",
                    GpuBuffer.USAGE_COPY_DST | GpuBuffer.USAGE_UNIFORM,
                    RenderSystem.PROJECTION_MATRIX_UBO_SIZE);
            this.slice = this.buffer.slice(0, RenderSystem.PROJECTION_MATRIX_UBO_SIZE);
        }

        GpuBufferSlice getBuffer(float left, float right, float bottom, float top) {
            if (this.left != left || this.right != right || this.bottom != bottom || this.top != top) {
                Matrix4f mat = projectionMatrix.setOrtho(left, right, bottom, top, zNear, zFar);
                try (MemoryStack stack = MemoryStack.stackPush()) {
                    ByteBuffer buf = Std140Builder.onStack(stack, RenderSystem.PROJECTION_MATRIX_UBO_SIZE)
                            .putMat4f(mat).get();
                    RenderSystem.getDevice().createCommandEncoder().writeToBuffer(buffer.slice(), buf);
                }
                this.left = left;
                this.right = right;
                this.bottom = bottom;
                this.top = top;
            }
            return slice;
        }

        @Override
        public void close() {
            buffer.close();
        }
    }
}
