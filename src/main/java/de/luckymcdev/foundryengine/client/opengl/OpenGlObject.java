package de.luckymcdev.foundryengine.client.opengl;

import de.luckymcdev.foundryengine.client.opengl.framebuffer.FrameBuffer;
import de.luckymcdev.foundryengine.client.opengl.program.ShaderProgram;
import de.luckymcdev.foundryengine.client.opengl.shaders.Shader;
import org.lwjgl.system.NativeResource;

/**
 * A low level OpenGlObject, which mimics how opengl handles its stuff.
 * All OpenGlObject Wrappers extend this. {@link FrameBuffer}
 * {@link Shader} {@link ShaderProgram}
 */
public abstract class OpenGlObject implements NativeResource {
    protected int pointer;
    protected int[] pointers;

    public void set(int pointer) {
        this.pointer = pointer;
        this.pointers = null;
    }

    public void set(int[] pointers) {
        this.pointers = pointers;
        this.pointer = 0;
    }

    public int pointer() {
        if (hasMultiple()) {
            return pointers[0];
        }
        return pointer;
    }

    public int[] pointers() {
        if (pointers != null) {
            return pointers;
        }
        return new int[]{pointer};
    }

    public boolean hasMultiple() {
        return pointers != null && pointers.length > 0;
    }
}