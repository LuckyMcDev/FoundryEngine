package de.luckymcdev.foundryengine.mixin.entity;

import de.luckymcdev.foundryengine.common.scene.EngineSceneNode;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Entity.class)
public abstract class EntityMixin implements EngineSceneNode {

    @Shadow
    protected String stringUUID;
    @Shadow
    private Vec3 position;

    @Shadow
    public abstract Vec2 getRotationVector();

    @Shadow
    public abstract void discard();

    @Shadow
    public abstract Component getName();

    @Shadow
    public abstract EntityType<?> getType();

    @Shadow
    private Level level;

    @Shadow
    public abstract void kill(ServerLevel level);

    @Shadow
    public abstract Level level();

    @Override
    public String getUUID() {
        return this.stringUUID;
    }

    @Override
    public String getTypeName() {
        Entity self = (Entity) (Object) this;
        return BuiltInRegistries.ENTITY_TYPE.getKey(self.getType()).toString();
    }

    @Override
    public String getDisplayName() {
        Entity self = (Entity) (Object) this;
        if (self.hasCustomName() && self.getCustomName() != null) {
            return self.getCustomName().getString();
        }
        return self.getType().getDescription().getString();
    }

    @Override
    public Vector3f getPosition() {
        return this.position.toVector3f();
    }

    @Override
    public Vector2f getRotation() {
        Vec2 rot = this.getRotationVector();
        return new Vector2f(rot.x, rot.y);
    }

    @Override
    public void remove() {
        if (this.level instanceof ServerLevel sLevel) {
            this.kill(sLevel);
        }
    }
}