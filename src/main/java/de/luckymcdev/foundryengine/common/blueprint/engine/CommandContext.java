package de.luckymcdev.foundryengine.common.blueprint.engine;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class CommandContext {

    public final List<Condition> conditions = new ArrayList<>();
    public @Nullable Entity executor;
    public @Nullable Vec3 position;
    public @Nullable Vec2 rotation;
    public @Nullable Entity anchoredEntity;
    public Anchor anchor = Anchor.FEET;
    public boolean alignX, alignY, alignZ;
    public @Nullable Vec3 facingPos;
    public @Nullable Entity facingEntity;
    public Anchor facingAnchor = Anchor.FEET;
    public @Nullable Level level;

    private static String formatVec(Vec3 v) {
        return v.x + " " + v.y + " " + v.z;
    }

    public CommandContext copy() {
        CommandContext c = new CommandContext();
        c.executor = this.executor;
        c.position = this.position;
        c.rotation = this.rotation;
        c.anchoredEntity = this.anchoredEntity;
        c.anchor = this.anchor;
        c.alignX = this.alignX;
        c.alignY = this.alignY;
        c.alignZ = this.alignZ;
        c.facingPos = this.facingPos;
        c.facingEntity = this.facingEntity;
        c.facingAnchor = this.facingAnchor;
        c.conditions.addAll(this.conditions);
        c.level = this.level;
        return c;
    }

    public CommandSourceStack resolveSource(CommandSourceStack fallback) {
        CommandSourceStack src = fallback;
        if (executor != null) {
            src = src.withEntity(executor);
        }
        if (position != null) {
            src = src.withPosition(position);
        }
        if (rotation != null) {
            src = src.withRotation(rotation);
        }
        if (level instanceof ServerLevel sl) {
            src = src.withLevel(sl);
        }
        return src;
    }

    public String buildExecutePrefix() {
        StringBuilder sb = new StringBuilder("execute");

        if (executor != null) {
            sb.append(" as ").append(executor.getUUID());
        }

        if (position != null) {
            sb.append(" positioned ").append(formatVec(position));
        }

        if (rotation != null) {
            sb.append(" rotated ").append(rotation.x).append(" ").append(rotation.y);
        }

        if (anchoredEntity != null && anchor != Anchor.FEET) {
            sb.append(" anchored ").append(anchor.name().toLowerCase());
        }

        if (alignX || alignY || alignZ) {
            String a = (alignX ? "x" : "") + (alignY ? "y" : "") + (alignZ ? "z" : "");
            sb.append(" align ").append(a);
        }

        if (facingPos != null) {
            sb.append(" facing ").append(formatVec(facingPos));
        } else if (facingEntity != null) {
            sb.append(" facing entity ").append(facingEntity.getUUID()).append(" ").append(facingAnchor.name().toLowerCase());
        }

        for (Condition cond : conditions) {
            sb.append(" ");
            switch (cond.type()) {
                case "if_block" -> {
                    BlockPos p = (BlockPos) cond.args()[0];
                    sb.append("if block ").append(p.getX()).append(" ").append(p.getY()).append(" ").append(p.getZ())
                            .append(" ").append(cond.args()[1]);
                }
                case "unless_block" -> {
                    BlockPos p = (BlockPos) cond.args()[0];
                    sb.append("unless block ").append(p.getX()).append(" ").append(p.getY()).append(" ").append(p.getZ())
                            .append(" ").append(cond.args()[1]);
                }
                case "if_entity" -> sb.append("if entity ").append(cond.args()[0]);
                case "unless_entity" -> sb.append("unless entity ").append(cond.args()[0]);
                case "if_predicate" -> sb.append("if predicate ").append(cond.args()[0]);
            }
        }

        return sb.toString();
    }

    public String run(String command) {
        return buildExecutePrefix() + " run " + command;
    }

    public enum Anchor {
        FEET, EYES
    }

    public record Condition(String type, Object... args) {
    }
}
