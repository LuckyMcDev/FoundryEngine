package de.luckymcdev.foundryengine.client.imgui.backend;

import imgui.*;
import imgui.callback.ImPlatformFuncViewport;
import imgui.flag.ImGuiBackendFlags;
import imgui.flag.ImGuiConfigFlags;
import imgui.flag.ImGuiViewportFlags;
import imgui.type.ImInt;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GLCapabilities;

import java.nio.ByteBuffer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL21.GL_PIXEL_UNPACK_BUFFER;
import static org.lwjgl.opengl.GL21.GL_PIXEL_UNPACK_BUFFER_BINDING;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.opengl.GL32.*;
import static org.lwjgl.opengl.GL33.GL_SAMPLER_BINDING;
import static org.lwjgl.opengl.GL33.glBindSampler;
import static org.lwjgl.opengl.GL45.GL_CLIP_ORIGIN;

/**
 * This class is a straightforward port of the
 * <a href="https://raw.githubusercontent.com/ocornut/imgui/32f4c234a8edd9a85b32a91c9e29afac15c50028/backends/imgui_impl_opengl3.cpp">imgui_impl_opengl3.cpp</a>.
 * <p>
 * It does support a backup and restoring of the GL state in the same way the original Dear ImGui code does.
 * Some of the very specific OpenGL variables may be ignored here,
 */
public class ImGuiImplGl3 {
    protected static final String OS = System.getProperty("os.name", "generic").toLowerCase();
    protected static final boolean IS_APPLE = OS.contains("mac") || OS.contains("darwin");

    private final Properties props = new Properties();

    /**
     * Method to do an initialization of the {@link ImGuiImplGl3} state.
     * It SHOULD be called before calling of the {@link ImGuiImplGl3#renderDrawData(ImDrawData)} method.
     * <p>
     * Method takes an argument, which should be a valid GLSL string with the version to use.
     * <pre>
     * ----------------------------------------
     * OpenGL    GLSL      GLSL
     * version   version   string
     * ---------------------------------------
     *  2.0       110       "#version 110"
     *  2.1       120       "#version 120"
     *  3.0       130       "#version 130"
     *  3.1       140       "#version 140"
     *  3.2       150       "#version 150"
     *  3.3       330       "#version 330 core"
     *  4.0       400       "#version 400 core"
     *  4.1       410       "#version 410 core"
     *  4.2       420       "#version 410 core"
     *  4.3       430       "#version 430 core"
     *  ES 3.0    300       "#version 300 es"   = WebGL 2.0
     * ---------------------------------------
     * </pre>
     * <p>
     * If the argument is null, then a "#version 130" (150 for APPLE) string will be used by default.
     *
     * @param glslVersion string with the version of the GLSL
     * @return true when initialized
     */
    public boolean init(final String glslVersion) {
        data = newData();

        final ImGuiIO io = ImGui.getIO();
        io.setBackendRendererName("imgui-java_impl_opengl3");

        {
            final String glVersionStr = glGetString(GL_VERSION);
            int major = glGetInteger(GL_MAJOR_VERSION);
            int minor = glGetInteger(GL_MINOR_VERSION);
            if (major == 0 && minor == 0) {
                if (glVersionStr != null) {
                    final String[] parts = glVersionStr.split("\\.");
                    major = Integer.parseInt(parts[0]);
                    minor = Integer.parseInt(parts[1]);
                }
            }
            data.glVersion = major * 100 + minor * 10;
            data.maxTextureSize = glGetInteger(GL_MAX_TEXTURE_SIZE);

            if (glVersionStr != null && glVersionStr.startsWith("OpenGL ES 3")) {
                data.glProfileIsES3 = true;
            }

            if (!data.glProfileIsES3 && data.glVersion >= 320) {
                data.glProfileMask = glGetInteger(GL_CONTEXT_PROFILE_MASK);
            }
            data.glProfileIsCompat = (data.glProfileMask & GL_CONTEXT_COMPATIBILITY_PROFILE_BIT) != 0;

            if (data.glVersion < 330) {
                try {
                    data.glCapabilities = GL.getCapabilities();
                } catch (IllegalStateException ignored) {
                }
            }
        }

        if (data.glVersion >= 320) {
            io.addBackendFlags(ImGuiBackendFlags.RendererHasVtxOffset);
        }

        io.addBackendFlags(ImGuiBackendFlags.RendererHasViewports);

        if (glslVersion == null) {
            if (IS_APPLE) {
                data.glslVersion = "#version 150";
            } else {
                data.glslVersion = "#version 130";
            }
        } else {
            data.glslVersion = glslVersion;
        }

        {
            final int[] currentTexture = new int[1];
            glGetIntegerv(GL_TEXTURE_BINDING_2D, currentTexture);
        }

        data.hasPolygonMode = !data.glProfileIsES3;
        data.hasBindSampler = data.glVersion >= 330 || data.glProfileIsES3;
        data.hasClipOrigin = data.glVersion >= 450;

        if (ImGui.getIO().hasConfigFlags(ImGuiConfigFlags.ViewportsEnable)) {
            initPlatformInterface();
        }

        return true;
    }

    protected Data data = null;

    public void shutdown() {
        final ImGuiIO io = ImGui.getIO();

        shutdownPlatformInterface();
        destroyDeviceObjects();

        io.setBackendRendererName(null);
        io.removeBackendFlags(ImGuiBackendFlags.RendererHasVtxOffset | ImGuiBackendFlags.RendererHasViewports);
        data = null;
    }

    protected Data newData() {
        return new Data();
    }

    /**
     * Method to do an initialization of the {@link ImGuiImplGl3} state.
     * It SHOULD be called before calling of the {@link ImGuiImplGl3#renderDrawData(ImDrawData)} method.
     * <p>
     * Unlike in the {@link #init(String)} method, here the glslVersion argument is omitted.
     * Thus, a "#version 130" string will be used instead.
     *
     * @return true when initialized
     */
    public boolean init() {
        return init(null);
    }

    protected void setupRenderState(final ImDrawData drawData, final int fbWidth, final int fbHeight, final int gVertexArrayObject) {
        glEnable(GL_BLEND);
        glBlendEquation(GL_FUNC_ADD);
        glBlendFuncSeparate(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, GL_ONE, GL_ONE_MINUS_SRC_ALPHA);
        glDisable(GL_CULL_FACE);
        glDisable(GL_DEPTH_TEST);
        glDisable(GL_STENCIL_TEST);
        glEnable(GL_SCISSOR_TEST);

        if (!data.glProfileIsES3 && data.glVersion >= 310) {
            glDisable(GL_PRIMITIVE_RESTART);
        }
        if (data.hasPolygonMode) {
            glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
        }

        boolean clipOriginLowerLeft = true;
        if (data.hasClipOrigin) {
            final int[] currentClipOrigin = new int[1];
            glGetIntegerv(GL_CLIP_ORIGIN, currentClipOrigin);
            if (currentClipOrigin[0] == GL_UPPER_LEFT) {
                clipOriginLowerLeft = false;
            }
        }

        glViewport(0, 0, fbWidth, fbHeight);
        float L = drawData.getDisplayPosX();
        float R = drawData.getDisplayPosX() + drawData.getDisplaySizeX();
        float T = drawData.getDisplayPosY();
        float B = drawData.getDisplayPosY() + drawData.getDisplaySizeY();

        if (data.hasClipOrigin && !clipOriginLowerLeft) {
            float tmp = T;
            T = B;
            B = tmp;
        }

        props.orthoProjMatrix[0] = 2.0f / (R - L);
        props.orthoProjMatrix[5] = 2.0f / (T - B);
        props.orthoProjMatrix[10] = -1.0f;
        props.orthoProjMatrix[12] = (R + L) / (L - R);
        props.orthoProjMatrix[13] = (T + B) / (B - T);
        props.orthoProjMatrix[15] = 1.0f;

        glUseProgram(data.shaderHandle);
        glUniform1i(data.attribLocationTex, 0);
        glUniformMatrix4fv(data.attribLocationProjMtx, false, props.orthoProjMatrix);

        if (data.hasBindSampler) {
            glBindSampler(0, 0);
        }

        glBindVertexArray(gVertexArrayObject);

        glBindBuffer(GL_ARRAY_BUFFER, data.vboHandle);
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, data.elementsHandle);
        glEnableVertexAttribArray(data.attribLocationVtxPos);
        glEnableVertexAttribArray(data.attribLocationVtxUV);
        glEnableVertexAttribArray(data.attribLocationVtxColor);
        glVertexAttribPointer(data.attribLocationVtxPos, 2, GL_FLOAT, false, ImDrawData.sizeOfImDrawVert(), 0);
        glVertexAttribPointer(data.attribLocationVtxUV, 2, GL_FLOAT, false, ImDrawData.sizeOfImDrawVert(), 8);
        glVertexAttribPointer(data.attribLocationVtxColor, 4, GL_UNSIGNED_BYTE, true, ImDrawData.sizeOfImDrawVert(), 16);
    }

    /**
     * OpenGL3 Render function.
     *
     * @param drawData draw data to render
     */
    public void renderDrawData(final ImDrawData drawData) {
        final int fbWidth = (int) (drawData.getDisplaySizeX() * drawData.getFramebufferScaleX());
        final int fbHeight = (int) (drawData.getDisplaySizeY() * drawData.getFramebufferScaleY());
        if (fbWidth <= 0 || fbHeight <= 0) {
            return;
        }

        if (drawData.getCmdListsCount() <= 0) {
            return;
        }

        glGetIntegerv(GL_ACTIVE_TEXTURE, props.lastActiveTexture);
        glActiveTexture(GL_TEXTURE0);
        glGetIntegerv(GL_CURRENT_PROGRAM, props.lastProgram);
        glGetIntegerv(GL_TEXTURE_BINDING_2D, props.lastTexture);
        if (data.hasBindSampler) {
            glGetIntegerv(GL_SAMPLER_BINDING, props.lastSampler);
        }
        glGetIntegerv(GL_ARRAY_BUFFER_BINDING, props.lastArrayBuffer);
        glGetIntegerv(GL_VERTEX_ARRAY_BINDING, props.lastVertexArrayObject);
        if (data.hasPolygonMode) {
            glGetIntegerv(GL_POLYGON_MODE, props.lastPolygonMode);
        }
        glGetIntegerv(GL_VIEWPORT, props.lastViewport);
        glGetIntegerv(GL_SCISSOR_BOX, props.lastScissorBox);
        glGetIntegerv(GL_BLEND_SRC_RGB, props.lastBlendSrcRgb);
        glGetIntegerv(GL_BLEND_DST_RGB, props.lastBlendDstRgb);
        glGetIntegerv(GL_BLEND_SRC_ALPHA, props.lastBlendSrcAlpha);
        glGetIntegerv(GL_BLEND_DST_ALPHA, props.lastBlendDstAlpha);
        glGetIntegerv(GL_BLEND_EQUATION_RGB, props.lastBlendEquationRgb);
        glGetIntegerv(GL_BLEND_EQUATION_ALPHA, props.lastBlendEquationAlpha);
        props.lastEnableBlend = glIsEnabled(GL_BLEND);
        props.lastEnableCullFace = glIsEnabled(GL_CULL_FACE);
        props.lastEnableDepthTest = glIsEnabled(GL_DEPTH_TEST);
        props.lastEnableStencilTest = glIsEnabled(GL_STENCIL_TEST);
        props.lastEnableScissorTest = glIsEnabled(GL_SCISSOR_TEST);
        if (!data.glProfileIsES3 && data.glVersion >= 310) {
            props.lastEnablePrimitiveRestart = glIsEnabled(GL_PRIMITIVE_RESTART);
        }

        final int vertexArrayObject = glGenVertexArrays();
        setupRenderState(drawData, fbWidth, fbHeight, vertexArrayObject);

        final float clipOffX = drawData.getDisplayPosX();
        final float clipOffY = drawData.getDisplayPosY();
        final float clipScaleX = drawData.getFramebufferScaleX();
        final float clipScaleY = drawData.getFramebufferScaleY();

        for (int n = 0; n < drawData.getCmdListsCount(); n++) {
            glBufferData(GL_ARRAY_BUFFER, drawData.getCmdListVtxBufferData(n), GL_STREAM_DRAW);
            glBufferData(GL_ELEMENT_ARRAY_BUFFER, drawData.getCmdListIdxBufferData(n), GL_STREAM_DRAW);

            for (int cmdIdx = 0; cmdIdx < drawData.getCmdListCmdBufferSize(n); cmdIdx++) {
                drawData.getCmdListCmdBufferClipRect(props.clipRect, n, cmdIdx);

                final float clipMinX = (props.clipRect.x - clipOffX) * clipScaleX;
                final float clipMinY = (props.clipRect.y - clipOffY) * clipScaleY;
                final float clipMaxX = (props.clipRect.z - clipOffX) * clipScaleX;
                final float clipMaxY = (props.clipRect.w - clipOffY) * clipScaleY;

                if (clipMaxX <= clipMinX || clipMaxY <= clipMinY) {
                    continue;
                }

                glScissor((int) clipMinX, (int) (fbHeight - clipMaxY), (int) (clipMaxX - clipMinX), (int) (clipMaxY - clipMinY));

                final long textureId = drawData.getCmdListCmdBufferTextureId(n, cmdIdx);
                final int elemCount = drawData.getCmdListCmdBufferElemCount(n, cmdIdx);
                final int idxOffset = drawData.getCmdListCmdBufferIdxOffset(n, cmdIdx);
                final int vtxOffset = drawData.getCmdListCmdBufferVtxOffset(n, cmdIdx);
                final long indices = idxOffset * (long) ImDrawData.sizeOfImDrawIdx();
                final int type = ImDrawData.sizeOfImDrawIdx() == 2 ? GL_UNSIGNED_SHORT : GL_UNSIGNED_INT;

                glBindTexture(GL_TEXTURE_2D, (int) textureId);

                if (data.glVersion >= 320) {
                    glDrawElementsBaseVertex(GL_TRIANGLES, elemCount, type, indices, vtxOffset);
                } else {
                    glDrawElements(GL_TRIANGLES, elemCount, type, indices);
                }
            }
        }

        glDeleteVertexArrays(vertexArrayObject);

        // Restore modified GL state
        if (props.lastProgram[0] == 0 || glIsProgram(props.lastProgram[0])) {
            glUseProgram(props.lastProgram[0]);
        }
        glBindTexture(GL_TEXTURE_2D, props.lastTexture[0]);
        if (data.hasBindSampler) {
            glBindSampler(0, props.lastSampler[0]);
        }
        glActiveTexture(props.lastActiveTexture[0]);
        glBindVertexArray(props.lastVertexArrayObject[0]);
        glBindBuffer(GL_ARRAY_BUFFER, props.lastArrayBuffer[0]);
        glBlendEquationSeparate(props.lastBlendEquationRgb[0], props.lastBlendEquationAlpha[0]);
        glBlendFuncSeparate(props.lastBlendSrcRgb[0], props.lastBlendDstRgb[0], props.lastBlendSrcAlpha[0], props.lastBlendDstAlpha[0]);
        if (props.lastEnableBlend) glEnable(GL_BLEND);
        else glDisable(GL_BLEND);
        if (props.lastEnableCullFace) glEnable(GL_CULL_FACE);
        else glDisable(GL_CULL_FACE);
        if (props.lastEnableDepthTest) glEnable(GL_DEPTH_TEST);
        else glDisable(GL_DEPTH_TEST);
        if (props.lastEnableStencilTest) glEnable(GL_STENCIL_TEST);
        else glDisable(GL_STENCIL_TEST);
        if (props.lastEnableScissorTest) glEnable(GL_SCISSOR_TEST);
        else glDisable(GL_SCISSOR_TEST);
        if (!data.glProfileIsES3 && data.glVersion >= 310) {
            if (props.lastEnablePrimitiveRestart) {
                glEnable(GL_PRIMITIVE_RESTART);
            } else {
                glDisable(GL_PRIMITIVE_RESTART);
            }
        }
        if (data.hasPolygonMode) {
            if (data.glVersion <= 310 || data.glProfileIsCompat) {
                glPolygonMode(GL_FRONT, props.lastPolygonMode[0]);
                glPolygonMode(GL_BACK, props.lastPolygonMode[1]);
            } else {
                glPolygonMode(GL_FRONT_AND_BACK, props.lastPolygonMode[0]);
            }
        }
        glViewport(props.lastViewport[0], props.lastViewport[1], props.lastViewport[2], props.lastViewport[3]);
        glScissor(props.lastScissorBox[0], props.lastScissorBox[1], props.lastScissorBox[2], props.lastScissorBox[3]);
    }

    public void newFrame() {
        if (data.shaderHandle == 0) {
            createDeviceObjects();
        }
    }

    public boolean createFontsTexture() {
        if (data == null) return false;

        final ImFontAtlas fontAtlas = ImGui.getIO().getFonts();
        if (fontAtlas == null || !fontAtlas.isBuilt()) return false;

        final ImInt width = new ImInt();
        final ImInt height = new ImInt();
        final ByteBuffer pixels = fontAtlas.getTexDataAsRGBA32(width, height);

        if (pixels == null || width.get() <= 0 || height.get() <= 0) return false;

        final int[] lastTexture = new int[1];
        glGetIntegerv(GL_TEXTURE_BINDING_2D, lastTexture);

        try {
            data.fontTexture = glGenTextures();
            glBindTexture(GL_TEXTURE_2D, data.fontTexture);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
            glPixelStorei(GL_UNPACK_ALIGNMENT, 1);
            glPixelStorei(GL_UNPACK_SKIP_PIXELS, 0);
            glPixelStorei(GL_UNPACK_SKIP_ROWS, 0);
            glPixelStorei(GL_UNPACK_ROW_LENGTH, 0);
            glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width.get(), height.get(), 0, GL_RGBA, GL_UNSIGNED_BYTE, pixels);
        } catch (Exception e) {
            if (data.fontTexture != 0) {
                glDeleteTextures(data.fontTexture);
                data.fontTexture = 0;
            }
            glBindTexture(GL_TEXTURE_2D, lastTexture[0]);
            return false;
        }

        fontAtlas.setTexID(data.fontTexture);

        glBindTexture(GL_TEXTURE_2D, lastTexture[0]);

        return true;
    }

    public void destroyFontsTexture() {
        if (data == null) {
            return;
        }
        final ImGuiIO io = ImGui.getIO();
        if (data.fontTexture != 0) {
            glDeleteTextures(data.fontTexture);
            io.getFonts().setTexID(0);
            data.fontTexture = 0;
        }
    }

    protected boolean createDeviceObjects() {
        final int[] lastTexture = new int[1];
        final int[] lastArrayBuffer = new int[1];
        final int[] lastPixelUnpackBuffer = new int[1];
        final int[] lastVertexArray = new int[1];
        glGetIntegerv(GL_TEXTURE_BINDING_2D, lastTexture);
        glGetIntegerv(GL_ARRAY_BUFFER_BINDING, lastArrayBuffer);
        if (data.glVersion >= 210) {
            glGetIntegerv(GL_PIXEL_UNPACK_BUFFER_BINDING, lastPixelUnpackBuffer);
            glBindBuffer(GL_PIXEL_UNPACK_BUFFER, 0);
        }
        glGetIntegerv(GL_VERTEX_ARRAY_BINDING, lastVertexArray);

        final int glslVersionValue = parseGlslVersionString(data.glslVersion);

        final CharSequence vertexShader;
        final CharSequence fragmentShader;

        if (glslVersionValue < 130) {
            vertexShader = vertexShaderGlsl120();
            fragmentShader = fragmentShaderGlsl120();
        } else if (glslVersionValue >= 410) {
            vertexShader = vertexShaderGlsl410Core();
            fragmentShader = fragmentShaderGlsl410Core();
        } else if (glslVersionValue == 300) {
            vertexShader = vertexShaderGlsl300es();
            fragmentShader = fragmentShaderGlsl300es();
        } else {
            vertexShader = vertexShaderGlsl130();
            fragmentShader = fragmentShaderGlsl130();
        }

        final int vertHandle = glCreateShader(GL_VERTEX_SHADER);
        glShaderSource(vertHandle, vertexShader);
        glCompileShader(vertHandle);
        checkShader(vertHandle, "vertex shader");

        final int fragHandle = glCreateShader(GL_FRAGMENT_SHADER);
        glShaderSource(fragHandle, fragmentShader);
        glCompileShader(fragHandle);
        checkShader(fragHandle, "fragment shader");

        data.shaderHandle = glCreateProgram();
        glAttachShader(data.shaderHandle, vertHandle);
        glAttachShader(data.shaderHandle, fragHandle);
        glLinkProgram(data.shaderHandle);
        checkProgram(data.shaderHandle, "shader program");

        glDetachShader(data.shaderHandle, vertHandle);
        glDetachShader(data.shaderHandle, fragHandle);
        glDeleteShader(vertHandle);
        glDeleteShader(fragHandle);

        data.attribLocationTex = glGetUniformLocation(data.shaderHandle, "Texture");
        data.attribLocationProjMtx = glGetUniformLocation(data.shaderHandle, "ProjMtx");
        data.attribLocationVtxPos = glGetAttribLocation(data.shaderHandle, "Position");
        data.attribLocationVtxUV = glGetAttribLocation(data.shaderHandle, "UV");
        data.attribLocationVtxColor = glGetAttribLocation(data.shaderHandle, "Color");

        data.vboHandle = glGenBuffers();
        data.elementsHandle = glGenBuffers();

        glBindTexture(GL_TEXTURE_2D, lastTexture[0]);
        glBindBuffer(GL_ARRAY_BUFFER, lastArrayBuffer[0]);
        if (data.glVersion >= 210) {
            glBindBuffer(GL_PIXEL_UNPACK_BUFFER, lastPixelUnpackBuffer[0]);
        }
        glBindVertexArray(lastVertexArray[0]);

        return true;
    }

    /**
     * Data class to store implementation specific fields.
     * Same as {@code ImGui_ImplOpenGL3_Data}.
     */
    protected static class Data {
        protected int glVersion = 0;
        protected boolean glProfileIsES3;
        protected boolean glProfileIsCompat;
        protected int glProfileMask;
        protected int maxTextureSize;
        protected GLCapabilities glCapabilities = null;
        protected String glslVersion = "";
        protected int fontTexture = 0;
        protected int shaderHandle = 0;
        protected int attribLocationTex = 0;
        protected int attribLocationProjMtx = 0;
        protected int attribLocationVtxPos = 0;
        protected int attribLocationVtxUV = 0;
        protected int attribLocationVtxColor = 0;
        protected int vboHandle = 0;
        protected int elementsHandle = 0;
        protected boolean hasPolygonMode;
        protected boolean hasBindSampler;
        protected boolean hasClipOrigin;
    }

    protected boolean checkShader(final int handle, final String desc) {
        final int[] status = new int[1];
        final int[] logLength = new int[1];
        glGetShaderiv(handle, GL_COMPILE_STATUS, status);
        glGetShaderiv(handle, GL_INFO_LOG_LENGTH, logLength);
        if (status[0] == GL_FALSE) {
            System.err.printf("%s: failed to compile %s! With GLSL: %s\n", this, desc, data.glslVersion);
        }
        if (logLength[0] > 1) {
            final String log = glGetShaderInfoLog(handle);
            System.err.println(log);
        }
        return status[0] == GL_TRUE;
    }

    protected boolean checkProgram(final int handle, final String desc) {
        final int[] status = new int[1];
        final int[] logLength = new int[1];
        glGetProgramiv(handle, GL_LINK_STATUS, status);
        glGetProgramiv(handle, GL_INFO_LOG_LENGTH, logLength);
        if (status[0] == GL_FALSE) {
            System.err.printf("%s: failed to link %s! With GLSL: %s\n", this, desc, data.glslVersion);
        }
        if (logLength[0] > 1) {
            final String log = glGetProgramInfoLog(handle);
            System.err.println(log);
        }
        return status[0] == GL_TRUE;
    }

    protected int parseGlslVersionString(final String glslVersion) {
        final Pattern p = Pattern.compile("\\d+");
        final Matcher m = p.matcher(glslVersion);

        if (m.find()) {
            return Integer.parseInt(m.group());
        }

        return 130;
    }

    /**
     * Internal class to store containers for frequently used arrays.
     */
    private static final class Properties {
        private final ImVec4 clipRect = new ImVec4();
        private final float[] orthoProjMatrix = new float[4 * 4];
        private final int[] lastActiveTexture = new int[1];
        private final int[] lastProgram = new int[1];
        private final int[] lastTexture = new int[1];
        private final int[] lastSampler = new int[1];
        private final int[] lastArrayBuffer = new int[1];
        private final int[] lastVertexArrayObject = new int[1];
        private final int[] lastPolygonMode = new int[2];
        private final int[] lastViewport = new int[4];
        private final int[] lastScissorBox = new int[4];
        private final int[] lastBlendSrcRgb = new int[1];
        private final int[] lastBlendDstRgb = new int[1];
        private final int[] lastBlendSrcAlpha = new int[1];
        private final int[] lastBlendDstAlpha = new int[1];
        private final int[] lastBlendEquationRgb = new int[1];
        private final int[] lastBlendEquationAlpha = new int[1];
        private boolean lastEnableBlend = false;
        private boolean lastEnableCullFace = false;
        private boolean lastEnableDepthTest = false;
        private boolean lastEnableStencilTest = false;
        private boolean lastEnableScissorTest = false;
        private boolean lastEnablePrimitiveRestart = false;
    }

    public void destroyDeviceObjects() {
        if (data.vboHandle != 0) {
            glDeleteBuffers(data.vboHandle);
            data.vboHandle = 0;
        }
        if (data.elementsHandle != 0) {
            glDeleteBuffers(data.elementsHandle);
            data.elementsHandle = 0;
        }
        if (data.shaderHandle != 0) {
            glDeleteProgram(data.shaderHandle);
            data.shaderHandle = 0;
        }
        destroyFontsTexture();
    }

    private final class RendererRenderWindowFunction extends ImPlatformFuncViewport {
        @Override
        public void accept(final ImGuiViewport vp) {
            if (!vp.hasFlags(ImGuiViewportFlags.NoRendererClear)) {
                glClearColor(0, 0, 0, 0);
                glClear(GL_COLOR_BUFFER_BIT);
            }
            renderDrawData(vp.getDrawData());
        }
    }

    protected void initPlatformInterface() {
        ImGui.getPlatformIO().setRendererRenderWindow(new RendererRenderWindowFunction());
    }

    protected void shutdownPlatformInterface() {
        ImGui.destroyPlatformWindows();
    }

    protected String vertexShaderGlsl120() {
        return data.glslVersion + "\n"
                + "uniform mat4 ProjMtx;\n"
                + "attribute vec2 Position;\n"
                + "attribute vec2 UV;\n"
                + "attribute vec4 Color;\n"
                + "varying vec2 Frag_UV;\n"
                + "varying vec4 Frag_Color;\n"
                + "void main()\n"
                + "{\n"
                + "    Frag_UV = UV;\n"
                + "    Frag_Color = Color;\n"
                + "    gl_Position = ProjMtx * vec4(Position.xy,0,1);\n"
                + "}\n";
    }

    protected String vertexShaderGlsl130() {
        return data.glslVersion + "\n"
                + "uniform mat4 ProjMtx;\n"
                + "in vec2 Position;\n"
                + "in vec2 UV;\n"
                + "in vec4 Color;\n"
                + "out vec2 Frag_UV;\n"
                + "out vec4 Frag_Color;\n"
                + "void main()\n"
                + "{\n"
                + "    Frag_UV = UV;\n"
                + "    Frag_Color = Color;\n"
                + "    gl_Position = ProjMtx * vec4(Position.xy,0,1);\n"
                + "}\n";
    }

    private String vertexShaderGlsl300es() {
        return data.glslVersion + "\n"
                + "precision highp float;\n"
                + "layout (location = 0) in vec2 Position;\n"
                + "layout (location = 1) in vec2 UV;\n"
                + "layout (location = 2) in vec4 Color;\n"
                + "uniform mat4 ProjMtx;\n"
                + "out vec2 Frag_UV;\n"
                + "out vec4 Frag_Color;\n"
                + "void main()\n"
                + "{\n"
                + "    Frag_UV = UV;\n"
                + "    Frag_Color = Color;\n"
                + "    gl_Position = ProjMtx * vec4(Position.xy,0,1);\n"
                + "}\n";
    }

    protected String vertexShaderGlsl410Core() {
        return data.glslVersion + "\n"
                + "layout (location = 0) in vec2 Position;\n"
                + "layout (location = 1) in vec2 UV;\n"
                + "layout (location = 2) in vec4 Color;\n"
                + "uniform mat4 ProjMtx;\n"
                + "out vec2 Frag_UV;\n"
                + "out vec4 Frag_Color;\n"
                + "void main()\n"
                + "{\n"
                + "    Frag_UV = UV;\n"
                + "    Frag_Color = Color;\n"
                + "    gl_Position = ProjMtx * vec4(Position.xy,0,1);\n"
                + "}\n";
    }

    protected String fragmentShaderGlsl120() {
        return data.glslVersion + "\n"
                + "#ifdef GL_ES\n"
                + "    precision mediump float;\n"
                + "#endif\n"
                + "uniform sampler2D Texture;\n"
                + "varying vec2 Frag_UV;\n"
                + "varying vec4 Frag_Color;\n"
                + "void main()\n"
                + "{\n"
                + "    gl_FragColor = Frag_Color * texture2D(Texture, Frag_UV.st);\n"
                + "}\n";
    }

    protected String fragmentShaderGlsl130() {
        return data.glslVersion + "\n"
                + "uniform sampler2D Texture;\n"
                + "in vec2 Frag_UV;\n"
                + "in vec4 Frag_Color;\n"
                + "out vec4 Out_Color;\n"
                + "void main()\n"
                + "{\n"
                + "    Out_Color = Frag_Color * texture(Texture, Frag_UV.st);\n"
                + "}\n";
    }

    protected String fragmentShaderGlsl300es() {
        return data.glslVersion + "\n"
                + "precision mediump float;\n"
                + "uniform sampler2D Texture;\n"
                + "in vec2 Frag_UV;\n"
                + "in vec4 Frag_Color;\n"
                + "layout (location = 0) out vec4 Out_Color;\n"
                + "void main()\n"
                + "{\n"
                + "    Out_Color = Frag_Color * texture(Texture, Frag_UV.st);\n"
                + "}\n";
    }

    protected String fragmentShaderGlsl410Core() {
        return data.glslVersion + "\n"
                + "in vec2 Frag_UV;\n"
                + "in vec4 Frag_Color;\n"
                + "uniform sampler2D Texture;\n"
                + "layout (location = 0) out vec4 Out_Color;\n"
                + "void main()\n"
                + "{\n"
                + "    Out_Color = Frag_Color * texture(Texture, Frag_UV.st);\n"
                + "}\n";
    }
}
