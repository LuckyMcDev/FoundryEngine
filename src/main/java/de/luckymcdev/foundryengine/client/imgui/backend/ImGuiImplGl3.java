package de.luckymcdev.foundryengine.client.imgui.backend;

import com.mojang.blaze3d.opengl.GlConst;
import com.mojang.blaze3d.opengl.GlStateManager;
import imgui.ImDrawData;
import imgui.ImFontAtlas;
import imgui.ImGui;
import imgui.ImGuiIO;
import imgui.ImGuiViewport;
import imgui.ImVec4;
import imgui.callback.ImPlatformFuncViewport;
import imgui.flag.ImGuiBackendFlags;
import imgui.flag.ImGuiConfigFlags;
import imgui.flag.ImGuiViewportFlags;
import imgui.type.ImInt;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GLCapabilities;

import java.nio.ByteBuffer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.lwjgl.opengl.GL32.*;
import static org.lwjgl.opengl.GL33.GL_SAMPLER_BINDING;
import static org.lwjgl.opengl.GL33.glBindSampler;
import static org.lwjgl.opengl.GL45.GL_CLIP_ORIGIN;

/**
 * Ported ImGui OpenGL3 Backend tailored for Minecraft's Blaze3D GlStateManager
 * with safe Multi-Viewport support. Supports OpenGL 3.3 and higher.
 */
public class ImGuiImplGl3 {
	protected static final String OS = System.getProperty("os.name", "generic").toLowerCase();
	protected static final boolean IS_APPLE = OS.contains("mac") || OS.contains("darwin");

	private final Properties props = new Properties();
	protected Data data = null;

	// Flag to track whether we are currently rendering a secondary viewport (external OS window)
	private boolean renderingSecondaryViewport = false;

	/**
	 * Method to do an initialization of the state.
	 * It SHOULD be called before calling of the {@link ImGuiImplGl3#renderDrawData(ImDrawData)} method.
	 *
	 * @param glslVersion string with the version of the GLSL
	 * @return true when initialized
	 */
	public boolean init(final String glslVersion) {
		data = newData();

		final ImGuiIO io = ImGui.getIO();
		io.setBackendRendererName("imgui-java_impl_opengl3_blaze3d");

		{
			final String glVersionStr = GlStateManager._getString(GL_VERSION);
			int major = GlStateManager._getInteger(GL_MAJOR_VERSION);
			int minor = GlStateManager._getInteger(GL_MINOR_VERSION);
			if (major == 0 && minor == 0) {
				if (glVersionStr != null) {
					final String[] parts = glVersionStr.split("\\.");
					major = Integer.parseInt(parts[0]);
					minor = Integer.parseInt(parts[1]);
				}
			}
			data.glVersion = major * 100 + minor * 10;
			data.maxTextureSize = GlStateManager._getInteger(GlConst.GL_MAX_TEXTURE_SIZE);

			if (glVersionStr != null && glVersionStr.startsWith("OpenGL ES 3")) {
				data.glProfileIsES3 = true;
			}

			if (!data.glProfileIsES3) {
				data.glProfileMask = GlStateManager._getInteger(GL_CONTEXT_PROFILE_MASK);
			}
			data.glProfileIsCompat = (data.glProfileMask & GL_CONTEXT_COMPATIBILITY_PROFILE_BIT) != 0;
		}

		io.addBackendFlags(ImGuiBackendFlags.RendererHasVtxOffset);
		io.addBackendFlags(ImGuiBackendFlags.RendererHasViewports);

		if (glslVersion == null) {
			data.glslVersion = "#version 330 core";
		} else {
			data.glslVersion = glslVersion;
		}

		{
			final int[] currentTexture = new int[1];
			glGetIntegerv(GL_TEXTURE_BINDING_2D, currentTexture);
		}

		data.hasPolygonMode = !data.glProfileIsES3;
		data.hasBindSampler = true; // Always true for OpenGL >= 330 and ES 3
		data.hasClipOrigin = data.glVersion >= 450;

		if (ImGui.getIO().hasConfigFlags(ImGuiConfigFlags.ViewportsEnable)) {
			initPlatformInterface();
		}

		return true;
	}

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

	public boolean init() {
		return init(null);
	}

	protected void setupRenderState(final ImDrawData drawData, final int fbWidth, final int fbHeight, final int gVertexArrayObject) {
		if (renderingSecondaryViewport) {
			// SECONDARY WINDOWS: Use raw GL to avoid corrupting GlStateManager's static cache
			glEnable(GL_BLEND);
			glBlendEquation(GL_FUNC_ADD);
			glBlendFuncSeparate(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, GL_ONE, GL_ONE_MINUS_SRC_ALPHA);
			glDisable(GL_CULL_FACE);
			glDisable(GL_DEPTH_TEST);
			glDisable(GL_STENCIL_TEST);
			glEnable(GL_SCISSOR_TEST);
		} else {
			// MAIN WINDOW: Use GlStateManager so Minecraft's state cache stays perfectly in sync
			GlStateManager._enableBlend();
			glBlendEquation(GlConst.GL_FUNC_ADD);
			GlStateManager._blendFuncSeparate(GlConst.GL_SRC_ALPHA, GlConst.GL_ONE_MINUS_SRC_ALPHA, GlConst.GL_ONE, GlConst.GL_ONE_MINUS_SRC_ALPHA);
			GlStateManager._disableCull();
			GlStateManager._disableDepthTest();
			GlStateManager._disableStencilTest();
			GlStateManager._enableScissorTest();
		}

		if (!data.glProfileIsES3) {
			glDisable(GL_PRIMITIVE_RESTART);
		}
		if (data.hasPolygonMode) {
			if (renderingSecondaryViewport) {
				glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
			} else {
				GlStateManager._polygonMode(GlConst.GL_FRONT_AND_BACK, GlConst.GL_FILL);
			}
		}

		boolean clipOriginLowerLeft = true;
		if (data.hasClipOrigin) {
			final int[] currentClipOrigin = new int[1];
			glGetIntegerv(GL_CLIP_ORIGIN, currentClipOrigin);
			if (currentClipOrigin[0] == GL_UPPER_LEFT) {
				clipOriginLowerLeft = false;
			}
		}

		if (renderingSecondaryViewport) {
			glViewport(0, 0, fbWidth, fbHeight);
		} else {
			GlStateManager._viewport(0, 0, fbWidth, fbHeight);
		}

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

		if (renderingSecondaryViewport) {
			glUseProgram(data.shaderHandle);
			glUniform1i(data.attribLocationTex, 0);
		} else {
			GlStateManager._glUseProgram(data.shaderHandle);
			GlStateManager._glUniform1i(data.attribLocationTex, 0);
		}
		glUniformMatrix4fv(data.attribLocationProjMtx, false, props.orthoProjMatrix);

		glBindSampler(0, 0);

		if (renderingSecondaryViewport) {
			glBindVertexArray(gVertexArrayObject);
			glBindBuffer(GL_ARRAY_BUFFER, data.vboHandle);
			glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, data.elementsHandle);
			glEnableVertexAttribArray(data.attribLocationVtxPos);
			glEnableVertexAttribArray(data.attribLocationVtxUV);
			glEnableVertexAttribArray(data.attribLocationVtxColor);
			glVertexAttribPointer(data.attribLocationVtxPos, 2, GL_FLOAT, false, ImDrawData.sizeOfImDrawVert(), 0);
			glVertexAttribPointer(data.attribLocationVtxUV, 2, GL_FLOAT, false, ImDrawData.sizeOfImDrawVert(), 8);
			glVertexAttribPointer(data.attribLocationVtxColor, 4, GL_UNSIGNED_BYTE, true, ImDrawData.sizeOfImDrawVert(), 16);
		} else {
			GlStateManager._glBindVertexArray(gVertexArrayObject);
			GlStateManager._glBindBuffer(GlConst.GL_ARRAY_BUFFER, data.vboHandle);
			GlStateManager._glBindBuffer(GlConst.GL_ELEMENT_ARRAY_BUFFER, data.elementsHandle);
			GlStateManager._enableVertexAttribArray(data.attribLocationVtxPos);
			GlStateManager._enableVertexAttribArray(data.attribLocationVtxUV);
			GlStateManager._enableVertexAttribArray(data.attribLocationVtxColor);
			GlStateManager._vertexAttribPointer(data.attribLocationVtxPos, 2, GlConst.GL_FLOAT, false, ImDrawData.sizeOfImDrawVert(), 0);
			GlStateManager._vertexAttribPointer(data.attribLocationVtxUV, 2, GlConst.GL_FLOAT, false, ImDrawData.sizeOfImDrawVert(), 8);
			GlStateManager._vertexAttribPointer(data.attribLocationVtxColor, 4, GlConst.GL_UNSIGNED_BYTE, true, ImDrawData.sizeOfImDrawVert(), 16);
		}
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

		if (renderingSecondaryViewport) {
			glActiveTexture(GL_TEXTURE0);
		} else {
			GlStateManager._activeTexture(GlConst.GL_TEXTURE0);
		}

		glGetIntegerv(GL_CURRENT_PROGRAM, props.lastProgram);
		glGetIntegerv(GL_TEXTURE_BINDING_2D, props.lastTexture);
		glGetIntegerv(GL_SAMPLER_BINDING, props.lastSampler);
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
		if (!data.glProfileIsES3) {
			props.lastEnablePrimitiveRestart = glIsEnabled(GL_PRIMITIVE_RESTART);
		}

		final int vertexArrayObject = renderingSecondaryViewport ? glGenVertexArrays() : GlStateManager._glGenVertexArrays();
		setupRenderState(drawData, fbWidth, fbHeight, vertexArrayObject);

		final float clipOffX = drawData.getDisplayPosX();
		final float clipOffY = drawData.getDisplayPosY();
		final float clipScaleX = drawData.getFramebufferScaleX();
		final float clipScaleY = drawData.getFramebufferScaleY();

		for (int n = 0; n < drawData.getCmdListsCount(); n++) {
			if (renderingSecondaryViewport) {
				glBufferData(GL_ARRAY_BUFFER, drawData.getCmdListVtxBufferData(n), GL_STREAM_DRAW);
				glBufferData(GL_ELEMENT_ARRAY_BUFFER, drawData.getCmdListIdxBufferData(n), GL_STREAM_DRAW);
			} else {
				GlStateManager._glBufferData(GlConst.GL_ARRAY_BUFFER, drawData.getCmdListVtxBufferData(n), GlConst.GL_STREAM_DRAW);
				GlStateManager._glBufferData(GlConst.GL_ELEMENT_ARRAY_BUFFER, drawData.getCmdListIdxBufferData(n), GlConst.GL_STREAM_DRAW);
			}

			for (int cmdIdx = 0; cmdIdx < drawData.getCmdListCmdBufferSize(n); cmdIdx++) {
				drawData.getCmdListCmdBufferClipRect(props.clipRect, n, cmdIdx);

				final float clipMinX = (props.clipRect.x - clipOffX) * clipScaleX;
				final float clipMinY = (props.clipRect.y - clipOffY) * clipScaleY;
				final float clipMaxX = (props.clipRect.z - clipOffX) * clipScaleX;
				final float clipMaxY = (props.clipRect.w - clipOffY) * clipScaleY;

				if (clipMaxX <= clipMinX || clipMaxY <= clipMinY) {
					continue;
				}

				if (renderingSecondaryViewport) {
					glScissor((int) clipMinX, (int) (fbHeight - clipMaxY), (int) (clipMaxX - clipMinX), (int) (clipMaxY - clipMinY));
				} else {
					GlStateManager._scissorBox((int) clipMinX, (int) (fbHeight - clipMaxY), (int) (clipMaxX - clipMinX), (int) (clipMaxY - clipMinY));
				}

				final long textureId = drawData.getCmdListCmdBufferTextureId(n, cmdIdx);
				final int elemCount = drawData.getCmdListCmdBufferElemCount(n, cmdIdx);
				final int idxOffset = drawData.getCmdListCmdBufferIdxOffset(n, cmdIdx);
				final int vtxOffset = drawData.getCmdListCmdBufferVtxOffset(n, cmdIdx);
				final long indices = idxOffset * (long) ImDrawData.sizeOfImDrawIdx();
				final int type = ImDrawData.sizeOfImDrawIdx() == 2 ? GL_UNSIGNED_SHORT : GL_UNSIGNED_INT;

				if (renderingSecondaryViewport) {
					glBindTexture(GL_TEXTURE_2D, (int) textureId);
				} else {
					GlStateManager._bindTexture((int) textureId);
				}

				glDrawElementsBaseVertex(GL_TRIANGLES, elemCount, type, indices, vtxOffset);
			}
		}

		glDeleteVertexArrays(vertexArrayObject);

		// Restore modified GL state
		if (renderingSecondaryViewport) {
			if (props.lastProgram[0] == 0 || glIsProgram(props.lastProgram[0])) {
				glUseProgram(props.lastProgram[0]);
			}
			glBindTexture(GL_TEXTURE_2D, props.lastTexture[0]);
			glBindSampler(0, props.lastSampler[0]);
			glActiveTexture(props.lastActiveTexture[0]);
			glBindVertexArray(props.lastVertexArrayObject[0]);
			glBindBuffer(GL_ARRAY_BUFFER, props.lastArrayBuffer[0]);
			glBlendEquationSeparate(props.lastBlendEquationRgb[0], props.lastBlendEquationAlpha[0]);
			glBlendFuncSeparate(props.lastBlendSrcRgb[0], props.lastBlendDstRgb[0], props.lastBlendSrcAlpha[0], props.lastBlendDstAlpha[0]);
			if (props.lastEnableBlend) {
				glEnable(GL_BLEND);
			} else {
				glDisable(GL_BLEND);
			}
			if (props.lastEnableCullFace) {
				glEnable(GL_CULL_FACE);
			} else {
				glDisable(GL_CULL_FACE);
			}
			if (props.lastEnableDepthTest) {
				glEnable(GL_DEPTH_TEST);
			} else {
				glDisable(GL_DEPTH_TEST);
			}
			if (props.lastEnableStencilTest) {
				glEnable(GL_STENCIL_TEST);
			} else {
				glDisable(GL_STENCIL_TEST);
			}
			if (props.lastEnableScissorTest) {
				glEnable(GL_SCISSOR_TEST);
			} else {
				glDisable(GL_SCISSOR_TEST);
			}
		} else {
			if (props.lastProgram[0] == 0 || glIsProgram(props.lastProgram[0])) {
				GlStateManager._glUseProgram(props.lastProgram[0]);
			}
			GlStateManager._bindTexture(props.lastTexture[0]);
			glBindSampler(0, props.lastSampler[0]);
			GlStateManager._activeTexture(props.lastActiveTexture[0]);
			GlStateManager._glBindVertexArray(props.lastVertexArrayObject[0]);
			GlStateManager._glBindBuffer(GlConst.GL_ARRAY_BUFFER, props.lastArrayBuffer[0]);
			glBlendEquationSeparate(props.lastBlendEquationRgb[0], props.lastBlendEquationAlpha[0]);
			GlStateManager._blendFuncSeparate(props.lastBlendSrcRgb[0], props.lastBlendDstRgb[0], props.lastBlendSrcAlpha[0], props.lastBlendDstAlpha[0]);
			if (props.lastEnableBlend) {
				GlStateManager._enableBlend();
			} else {
				GlStateManager._disableBlend();
			}
			if (props.lastEnableCullFace) {
				GlStateManager._enableCull();
			} else {
				GlStateManager._disableCull();
			}
			if (props.lastEnableDepthTest) {
				GlStateManager._enableDepthTest();
			} else {
				GlStateManager._disableDepthTest();
			}
			if (props.lastEnableStencilTest) {
				GlStateManager._enableStencilTest();
			} else {
				GlStateManager._disableStencilTest();
			}
			if (props.lastEnableScissorTest) {
				GlStateManager._enableScissorTest();
			} else {
				GlStateManager._disableScissorTest();
			}
		}

		if (!data.glProfileIsES3) {
			if (props.lastEnablePrimitiveRestart) {
				glEnable(GL_PRIMITIVE_RESTART);
			} else {
				glDisable(GL_PRIMITIVE_RESTART);
			}
		}

		if (data.hasPolygonMode) {
			if (renderingSecondaryViewport) {
				if (data.glProfileIsCompat) {
					glPolygonMode(GL_FRONT, props.lastPolygonMode[0]);
					glPolygonMode(GL_BACK, props.lastPolygonMode[1]);
				} else {
					glPolygonMode(GL_FRONT_AND_BACK, props.lastPolygonMode[0]);
				}
			} else {
				if (data.glProfileIsCompat) {
					GlStateManager._polygonMode(GlConst.GL_FRONT, props.lastPolygonMode[0]);
					GlStateManager._polygonMode(GL11.GL_BACK, props.lastPolygonMode[1]); // WHY MOJANG WHY
				} else {
					GlStateManager._polygonMode(GlConst.GL_FRONT_AND_BACK, props.lastPolygonMode[0]);
				}
			}
		}

		if (renderingSecondaryViewport) {
			glViewport(props.lastViewport[0], props.lastViewport[1], props.lastViewport[2], props.lastViewport[3]);
			glScissor(props.lastScissorBox[0], props.lastScissorBox[1], props.lastScissorBox[2], props.lastScissorBox[3]);
		} else {
			GlStateManager._viewport(props.lastViewport[0], props.lastViewport[1], props.lastViewport[2], props.lastViewport[3]);
			GlStateManager._scissorBox(props.lastScissorBox[0], props.lastScissorBox[1], props.lastScissorBox[2], props.lastScissorBox[3]);
		}
	}

	public void newFrame() {
		if (data.shaderHandle == 0) {
			createDeviceObjects();
		}
	}

	public boolean createFontsTexture() {
		if (data == null) {
			return false;
		}

		final ImFontAtlas fontAtlas = ImGui.getIO().getFonts();
		if (fontAtlas == null || !fontAtlas.isBuilt()) {
			return false;
		}

		final ImInt width = new ImInt();
		final ImInt height = new ImInt();
		final ByteBuffer pixels = fontAtlas.getTexDataAsRGBA32(width, height);

		if (pixels == null || width.get() <= 0 || height.get() <= 0) {
			return false;
		}

		final int[] lastTexture = new int[1];
		glGetIntegerv(GL_TEXTURE_BINDING_2D, lastTexture);

		try {
			data.fontTexture = GlStateManager._genTexture();
			GlStateManager._bindTexture(data.fontTexture);
			GlStateManager._texParameter(GlConst.GL_TEXTURE_2D, GlConst.GL_TEXTURE_MIN_FILTER, GlConst.GL_LINEAR);
			GlStateManager._texParameter(GlConst.GL_TEXTURE_2D, GlConst.GL_TEXTURE_MAG_FILTER, GlConst.GL_LINEAR);
			GlStateManager._texParameter(GlConst.GL_TEXTURE_2D, GlConst.GL_TEXTURE_WRAP_S, GlConst.GL_CLAMP_TO_EDGE);
			GlStateManager._texParameter(GlConst.GL_TEXTURE_2D, GlConst.GL_TEXTURE_WRAP_T, GlConst.GL_CLAMP_TO_EDGE);
			GlStateManager._pixelStore(GlConst.GL_UNPACK_ALIGNMENT, 1);
			GlStateManager._pixelStore(GlConst.GL_UNPACK_SKIP_PIXELS, 0);
			GlStateManager._pixelStore(GlConst.GL_UNPACK_SKIP_ROWS, 0);
			GlStateManager._pixelStore(GlConst.GL_UNPACK_ROW_LENGTH, 0);
			GlStateManager._texImage2D(GlConst.GL_TEXTURE_2D, 0, GlConst.GL_RGBA, width.get(), height.get(), 0, GlConst.GL_RGBA, GlConst.GL_UNSIGNED_BYTE, pixels);
		} catch (Exception e) {
			if (data.fontTexture != 0) {
				GlStateManager._deleteTexture(data.fontTexture);
				data.fontTexture = 0;
			}
			GlStateManager._bindTexture(lastTexture[0]);
			return false;
		}

		fontAtlas.setTexID(data.fontTexture);

		GlStateManager._bindTexture(lastTexture[0]);

		return true;
	}

	public void destroyFontsTexture() {
		if (data == null) {
			return;
		}
		final ImGuiIO io = ImGui.getIO();
		if (data.fontTexture != 0) {
			GlStateManager._deleteTexture(data.fontTexture);
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
		glGetIntegerv(GL_PIXEL_UNPACK_BUFFER_BINDING, lastPixelUnpackBuffer);
		GlStateManager._glBindBuffer(GlConst.GL_PIXEL_UNPACK_BUFFER, 0);
		glGetIntegerv(GL_VERTEX_ARRAY_BINDING, lastVertexArray);

		final int glslVersionValue = parseGlslVersionString(data.glslVersion);

		final CharSequence vertexShader;
		final CharSequence fragmentShader;

		if (glslVersionValue >= 410) {
			vertexShader = vertexShaderGlsl410Core();
			fragmentShader = fragmentShaderGlsl410Core();
		} else if (glslVersionValue == 300) {
			vertexShader = vertexShaderGlsl300es();
			fragmentShader = fragmentShaderGlsl300es();
		} else {
			vertexShader = vertexShaderGlsl330Core();
			fragmentShader = fragmentShaderGlsl330Core();
		}

		final int vertHandle = GlStateManager.glCreateShader(GlConst.GL_VERTEX_SHADER);
		GlStateManager.glShaderSource(vertHandle, vertexShader.toString());
		GlStateManager.glCompileShader(vertHandle);
		checkShader(vertHandle, "vertex shader");

		final int fragHandle = GlStateManager.glCreateShader(GlConst.GL_FRAGMENT_SHADER);
		GlStateManager.glShaderSource(fragHandle, fragmentShader.toString());
		GlStateManager.glCompileShader(fragHandle);
		checkShader(fragHandle, "fragment shader");

		data.shaderHandle = GlStateManager.glCreateProgram();
		GlStateManager.glAttachShader(data.shaderHandle, vertHandle);
		GlStateManager.glAttachShader(data.shaderHandle, fragHandle);
		GlStateManager.glLinkProgram(data.shaderHandle);
		checkProgram(data.shaderHandle, "shader program");

		glDetachShader(data.shaderHandle, vertHandle);
		glDetachShader(data.shaderHandle, fragHandle);
		GlStateManager.glDeleteShader(vertHandle);
		GlStateManager.glDeleteShader(fragHandle);

		data.attribLocationTex = GlStateManager._glGetUniformLocation(data.shaderHandle, "Texture");
		data.attribLocationProjMtx = GlStateManager._glGetUniformLocation(data.shaderHandle, "ProjMtx");
		data.attribLocationVtxPos = glGetAttribLocation(data.shaderHandle, "Position");
		data.attribLocationVtxUV = glGetAttribLocation(data.shaderHandle, "UV");
		data.attribLocationVtxColor = glGetAttribLocation(data.shaderHandle, "Color");

		data.vboHandle = GlStateManager._glGenBuffers();
		data.elementsHandle = GlStateManager._glGenBuffers();

		GlStateManager._bindTexture(lastTexture[0]);
		GlStateManager._glBindBuffer(GlConst.GL_ARRAY_BUFFER, lastArrayBuffer[0]);
		GlStateManager._glBindBuffer(GlConst.GL_PIXEL_UNPACK_BUFFER, lastPixelUnpackBuffer[0]);
		GlStateManager._glBindVertexArray(lastVertexArray[0]);

		return true;
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
			final String log = GlStateManager.glGetShaderInfoLog(handle, logLength[0]);
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
			final String log = GlStateManager.glGetProgramInfoLog(handle, logLength[0]);
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

		return 330;
	}

	public void destroyDeviceObjects() {
		if (data.vboHandle != 0) {
			GlStateManager._glDeleteBuffers(data.vboHandle);
			data.vboHandle = 0;
		}
		if (data.elementsHandle != 0) {
			GlStateManager._glDeleteBuffers(data.elementsHandle);
			data.elementsHandle = 0;
		}
		if (data.shaderHandle != 0) {
			GlStateManager.glDeleteProgram(data.shaderHandle);
			data.shaderHandle = 0;
		}
		destroyFontsTexture();
	}

	protected void initPlatformInterface() {
		ImGui.getPlatformIO().setRendererRenderWindow(new RendererRenderWindowFunction());
	}

	protected void shutdownPlatformInterface() {
		ImGui.destroyPlatformWindows();
	}

	protected String vertexShaderGlsl330Core() {
		return data.glslVersion + "\n" + """
			layout (location = 0) in vec2 Position;
			layout (location = 1) in vec2 UV;
			layout (location = 2) in vec4 Color;
			uniform mat4 ProjMtx;
			out vec2 Frag_UV;
			out vec4 Frag_Color;
			void main()
			{
			    Frag_UV = UV;
			    Frag_Color = Color;
			    gl_Position = ProjMtx * vec4(Position.xy,0,1);
			}
			""";
	}

	protected String fragmentShaderGlsl330Core() {
		return data.glslVersion + "\n" + """
			uniform sampler2D Texture;
			in vec2 Frag_UV;
			in vec4 Frag_Color;
			layout (location = 0) out vec4 Out_Color;
			void main()
			{
			    Out_Color = Frag_Color * texture(Texture, Frag_UV.st);
			}
			""";
	}

	private String vertexShaderGlsl300es() {
		return data.glslVersion + "\n" + """
			precision highp float;
			layout (location = 0) in vec2 Position;
			layout (location = 1) in vec2 UV;
			layout (location = 2) in vec4 Color;
			uniform mat4 ProjMtx;
			out vec2 Frag_UV;
			out vec4 Frag_Color;
			void main()
			{
			    Frag_UV = UV;
			    Frag_Color = Color;
			    gl_Position = ProjMtx * vec4(Position.xy,0,1);
			}
			""";
	}

	protected String fragmentShaderGlsl300es() {
		return data.glslVersion + "\n" + """
			precision mediump float;
			uniform sampler2D Texture;
			in vec2 Frag_UV;
			in vec4 Frag_Color;
			layout (location = 0) out vec4 Out_Color;
			void main()
			{
			    Out_Color = Frag_Color * texture(Texture, Frag_UV.st);
			}
			""";
	}

	protected String vertexShaderGlsl410Core() {
		return data.glslVersion + "\n" + """
			layout (location = 0) in vec2 Position;
			layout (location = 1) in vec2 UV;
			layout (location = 2) in vec4 Color;
			uniform mat4 ProjMtx;
			out vec2 Frag_UV;
			out vec4 Frag_Color;
			void main()
			{
			    Frag_UV = UV;
			    Frag_Color = Color;
			    gl_Position = ProjMtx * vec4(Position.xy,0,1);
			}
			""";
	}

	protected String fragmentShaderGlsl410Core() {
		return data.glslVersion + "\n" + """
			in vec2 Frag_UV;
			in vec4 Frag_Color;
			uniform sampler2D Texture;
			layout (location = 0) out vec4 Out_Color;
			void main()
			{
			    Out_Color = Frag_Color * texture(Texture, Frag_UV.st);
			}
			""";
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

	private final class RendererRenderWindowFunction extends ImPlatformFuncViewport {
		@Override
		public void accept(final ImGuiViewport vp) {
			if (!vp.hasFlags(ImGuiViewportFlags.NoRendererClear)) {
				glClearColor(0, 0, 0, 0);
				glClear(GL_COLOR_BUFFER_BIT);
			}
			renderingSecondaryViewport = true;
			try {
				renderDrawData(vp.getDrawData());
			} finally {
				renderingSecondaryViewport = false;
			}
		}
	}
}