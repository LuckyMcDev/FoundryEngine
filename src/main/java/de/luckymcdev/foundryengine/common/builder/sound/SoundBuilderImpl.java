package de.luckymcdev.foundryengine.common.builder.sound;

import de.luckymcdev.foundryengine.api.builder.sound.SoundBuilder;
import de.luckymcdev.foundryengine.common.builder.BuilderState;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.registries.RegisterEvent;
import org.jetbrains.annotations.ApiStatus;

public class SoundBuilderImpl implements SoundBuilder {
    public final BuilderState<SoundEvent> state;
    private float fixedRange = -1f;

    public SoundBuilderImpl(Identifier id) {
        this.state = new BuilderState<>(id);
        this.state.registryKey = Registries.SOUND_EVENT;
    }

    @Override
    public SoundBuilder range(float distance) {
        this.fixedRange = distance;
        return this;
    }

    @Override
    @ApiStatus.Internal
    public SoundEvent register(RegisterEvent.RegisterHelper<SoundEvent> helper) {
        SoundEvent event = build();
        helper.register(state.id, event);
        state.setObject(event);
        return event;
    }

    @Override
    public SoundEvent build() {
        if (fixedRange > 0f) {
            return SoundEvent.createFixedRangeEvent(state.id, fixedRange);
        }
        return SoundEvent.createVariableRangeEvent(state.id);
    }

    @Override
    public SoundEvent get() {
        return state.get();
    }

    @Override
    public SoundEvent getOrCreate() {
        return state.getOrCreate();
    }

    @Override
    public Identifier newID(String pre, String post) {
        return state.newID(pre, post);
    }
}