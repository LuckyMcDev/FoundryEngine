package de.luckymcdev.foundryengine.client.imgui;

public interface EngineImGui {
    void create(final long handle);

    void enable();

    void disable();

    void toggle();

    boolean isEnabled();

    void begin();

    void end();
}
