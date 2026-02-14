package io.github.luckymcdev.client.opengl;

import org.lwjgl.opengl.GL43C;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Utility class for saving and restoring OpenGL state.
 * Uses a stack-based approach to handle nested state changes.
 */
public class OpenGlStack {
    private final Deque<State> stateStack = new ArrayDeque<>();
    private static final int NUM_TEXTURE_UNITS = 16; // Most systems support at least 16

    /**
     * Push the current OpenGL state onto the stack
     */
    public void push() {
        stateStack.push(new State());
    }

    /**
     * Pop and restore the most recent OpenGL state from the stack
     */
    public void pop() {
        if (stateStack.isEmpty()) {
            throw new IllegalStateException("OpenGL state stack is empty");
        }
        State state = stateStack.pop();
        state.restore();
    }

    /**
     * Check if the stack is empty
     */
    public boolean isEmpty() {
        return stateStack.isEmpty();
    }

    /**
     * Clear the entire stack (useful for cleanup)
     */
    public void clear() {
        stateStack.clear();
    }

    /**
     * Stores a snapshot of OpenGL state
     */
    private static class State {
        // Texture state - save ALL texture units and ALL targets
        private final int activeTexture;
        private final int[][] boundTextures; // [unit][target]

        // Framebuffer state
        private final int boundFramebuffer;
        private final int boundReadFramebuffer;
        private final int boundDrawFramebuffer;

        // Shader state
        private final int currentProgram;

        // Viewport state
        private final int[] viewport = new int[4];

        // Enable/disable state
        private final boolean depthTestEnabled;
        private final boolean blendEnabled;
        private final boolean cullFaceEnabled;
        private final boolean scissorTestEnabled;
        private final boolean stencilTestEnabled;

        // Depth state
        private final boolean depthMask;

        // Blend state
        private final int blendSrcRgb;
        private final int blendDstRgb;
        private final int blendSrcAlpha;
        private final int blendDstAlpha;

        // Color mask
        private final boolean[] colorMask = new boolean[4];

        // Texture targets to save
        private static final int[] TEXTURE_TARGETS = {
                GL43C.GL_TEXTURE_2D,
                GL43C.GL_TEXTURE_1D,
                GL43C.GL_TEXTURE_3D,
                GL43C.GL_TEXTURE_CUBE_MAP,
                GL43C.GL_TEXTURE_1D_ARRAY,
                GL43C.GL_TEXTURE_2D_ARRAY,
                GL43C.GL_TEXTURE_BUFFER,
                GL43C.GL_TEXTURE_2D_MULTISAMPLE,
                GL43C.GL_TEXTURE_2D_MULTISAMPLE_ARRAY
        };

        /**
         * Capture the current OpenGL state
         */
        public State() {
            // Save active texture unit
            this.activeTexture = GlDispatch.glGetInteger(GL43C.GL_ACTIVE_TEXTURE);

            // Save bindings for all texture units and targets
            this.boundTextures = new int[NUM_TEXTURE_UNITS][TEXTURE_TARGETS.length];
            for (int unit = 0; unit < NUM_TEXTURE_UNITS; unit++) {
                GlDispatch.glActiveTexture(GL43C.GL_TEXTURE0 + unit);
                for (int targetIdx = 0; targetIdx < TEXTURE_TARGETS.length; targetIdx++) {
                    int target = TEXTURE_TARGETS[targetIdx];
                    boundTextures[unit][targetIdx] = GlDispatch.glGetInteger(getTextureBindingForTarget(target));
                }
            }

            // Framebuffer state
            this.boundFramebuffer = GlDispatch.glGetInteger(GL43C.GL_FRAMEBUFFER_BINDING);
            this.boundReadFramebuffer = GlDispatch.glGetInteger(GL43C.GL_READ_FRAMEBUFFER_BINDING);
            this.boundDrawFramebuffer = GlDispatch.glGetInteger(GL43C.GL_DRAW_FRAMEBUFFER_BINDING);

            // Shader state
            this.currentProgram = GlDispatch.glGetInteger(GL43C.GL_CURRENT_PROGRAM);

            // Viewport state
            GlDispatch.glGetIntegerv(GL43C.GL_VIEWPORT, viewport);

            // Enable/disable state
            this.depthTestEnabled = GlDispatch.glIsEnabled(GL43C.GL_DEPTH_TEST);
            this.blendEnabled = GlDispatch.glIsEnabled(GL43C.GL_BLEND);
            this.cullFaceEnabled = GlDispatch.glIsEnabled(GL43C.GL_CULL_FACE);
            this.scissorTestEnabled = GlDispatch.glIsEnabled(GL43C.GL_SCISSOR_TEST);
            this.stencilTestEnabled = GlDispatch.glIsEnabled(GL43C.GL_STENCIL_TEST);

            // Depth state
            this.depthMask = GlDispatch.glGetBoolean(GL43C.GL_DEPTH_WRITEMASK);

            // Blend state
            this.blendSrcRgb = GlDispatch.glGetInteger(GL43C.GL_BLEND_SRC_RGB);
            this.blendDstRgb = GlDispatch.glGetInteger(GL43C.GL_BLEND_DST_RGB);
            this.blendSrcAlpha = GlDispatch.glGetInteger(GL43C.GL_BLEND_SRC_ALPHA);
            this.blendDstAlpha = GlDispatch.glGetInteger(GL43C.GL_BLEND_DST_ALPHA);
        }

        /**
         * Get the appropriate GL_TEXTURE_BINDING constant for a given texture target
         */
        private static int getTextureBindingForTarget(int target) {
            return switch (target) {
                case GL43C.GL_TEXTURE_1D -> GL43C.GL_TEXTURE_BINDING_1D;
                case GL43C.GL_TEXTURE_2D -> GL43C.GL_TEXTURE_BINDING_2D;
                case GL43C.GL_TEXTURE_3D -> GL43C.GL_TEXTURE_BINDING_3D;
                case GL43C.GL_TEXTURE_CUBE_MAP -> GL43C.GL_TEXTURE_BINDING_CUBE_MAP;
                case GL43C.GL_TEXTURE_1D_ARRAY -> GL43C.GL_TEXTURE_BINDING_1D_ARRAY;
                case GL43C.GL_TEXTURE_2D_ARRAY -> GL43C.GL_TEXTURE_BINDING_2D_ARRAY;
                case GL43C.GL_TEXTURE_BUFFER -> GL43C.GL_TEXTURE_BINDING_BUFFER;
                case GL43C.GL_TEXTURE_2D_MULTISAMPLE -> GL43C.GL_TEXTURE_BINDING_2D_MULTISAMPLE;
                case GL43C.GL_TEXTURE_2D_MULTISAMPLE_ARRAY -> GL43C.GL_TEXTURE_BINDING_2D_MULTISAMPLE_ARRAY;
                default -> GL43C.GL_TEXTURE_BINDING_2D;
            };
        }

        /**
         * Restore this OpenGL state
         */
        public void restore() {
            // Restore texture bindings for all units and targets
            for (int unit = 0; unit < NUM_TEXTURE_UNITS; unit++) {
                GlDispatch.glActiveTexture(GL43C.GL_TEXTURE0 + unit);
                for (int targetIdx = 0; targetIdx < TEXTURE_TARGETS.length; targetIdx++) {
                    int target = TEXTURE_TARGETS[targetIdx];
                    GlDispatch.glBindTexture(target, boundTextures[unit][targetIdx]);
                }
            }
            // Restore active texture unit last
            GlDispatch.glActiveTexture(activeTexture);

            // Restore framebuffer state
            GlDispatch.glBindFramebuffer(GL43C.GL_FRAMEBUFFER, boundFramebuffer);
            GlDispatch.glBindFramebuffer(GL43C.GL_READ_FRAMEBUFFER, boundReadFramebuffer);
            GlDispatch.glBindFramebuffer(GL43C.GL_DRAW_FRAMEBUFFER, boundDrawFramebuffer);

            // Restore shader state
            GlDispatch.glUseProgram(currentProgram);

            // Restore viewport
            GlDispatch.glViewport(viewport[0], viewport[1], viewport[2], viewport[3]);

            // Restore enable/disable state
            if (depthTestEnabled) GlDispatch.glEnable(GL43C.GL_DEPTH_TEST);
            else GlDispatch.glDisable(GL43C.GL_DEPTH_TEST);

            if (blendEnabled) GlDispatch.glEnable(GL43C.GL_BLEND);
            else GlDispatch.glDisable(GL43C.GL_BLEND);

            if (cullFaceEnabled) GlDispatch.glEnable(GL43C.GL_CULL_FACE);
            else GlDispatch.glDisable(GL43C.GL_CULL_FACE);

            if (scissorTestEnabled) GlDispatch.glEnable(GL43C.GL_SCISSOR_TEST);
            else GlDispatch.glDisable(GL43C.GL_SCISSOR_TEST);

            if (stencilTestEnabled) GlDispatch.glEnable(GL43C.GL_STENCIL_TEST);
            else GlDispatch.glDisable(GL43C.GL_STENCIL_TEST);

            // Restore depth state
            GlDispatch.glDepthMask(depthMask);

            // Restore blend state
            GlDispatch.glBlendFuncSeparate(blendSrcRgb, blendDstRgb, blendSrcAlpha, blendDstAlpha);
        }
    }
}