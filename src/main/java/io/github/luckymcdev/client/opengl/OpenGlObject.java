package io.github.luckymcdev.client.opengl;

import org.lwjgl.system.NativeResource;

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