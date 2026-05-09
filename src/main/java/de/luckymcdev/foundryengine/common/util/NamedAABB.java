package de.luckymcdev.foundryengine.common.util;

import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

public class NamedAABB extends AABB {
    public final String name;

    public NamedAABB(String name, double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
        super(minX, minY, minZ, maxX, maxY, maxZ);
        this.name = name;
    }

    public NamedAABB(String name, BlockPos pos) {
        super(pos);
        this.name = name;
    }

    public NamedAABB(String name, Vec3 begin, Vec3 end) {
        super(begin, end);
        this.name = name;
    }

    @Override
    public NamedAABB setMinX(double minX) {
        return new NamedAABB(name, minX, this.minY, this.minZ, this.maxX, this.maxY, this.maxZ);
    }

    @Override
    public NamedAABB setMinY(double minY) {
        return new NamedAABB(name, this.minX, minY, this.minZ, this.maxX, this.maxY, this.maxZ);
    }

    @Override
    public NamedAABB setMinZ(double minZ) {
        return new NamedAABB(name, this.minX, this.minY, minZ, this.maxX, this.maxY, this.maxZ);
    }

    @Override
    public NamedAABB setMaxX(double maxX) {
        return new NamedAABB(name, this.minX, this.minY, this.minZ, maxX, this.maxY, this.maxZ);
    }

    @Override
    public NamedAABB setMaxY(double maxY) {
        return new NamedAABB(name, this.minX, this.minY, this.minZ, this.maxX, maxY, this.maxZ);
    }

    @Override
    public NamedAABB setMaxZ(double maxZ) {
        return new NamedAABB(name, this.minX, this.minY, this.minZ, this.maxX, this.maxY, maxZ);
    }

    @Override
    public NamedAABB contract(double xa, double ya, double za) {
        return fromAABB(super.contract(xa, ya, za));
    }

    @Override
    public NamedAABB expandTowards(Vec3 delta) {
        return fromAABB(super.expandTowards(delta));
    }

    @Override
    public NamedAABB expandTowards(double xa, double ya, double za) {
        return fromAABB(super.expandTowards(xa, ya, za));
    }

    @Override
    public NamedAABB inflate(double xAdd, double yAdd, double zAdd) {
        return fromAABB(super.inflate(xAdd, yAdd, zAdd));
    }

    @Override
    public NamedAABB inflate(double amount) {
        return fromAABB(super.inflate(amount));
    }

    @Override
    public NamedAABB deflate(double xSubtract, double ySubtract, double zSubtract) {
        return fromAABB(super.deflate(xSubtract, ySubtract, zSubtract));
    }

    @Override
    public NamedAABB deflate(double amount) {
        return fromAABB(super.deflate(amount));
    }

    @Override
    public NamedAABB intersect(AABB other) {
        return fromAABB(super.intersect(other));
    }

    @Override
    public NamedAABB minmax(AABB other) {
        return fromAABB(super.minmax(other));
    }

    @Override
    public NamedAABB move(double xa, double ya, double za) {
        return fromAABB(super.move(xa, ya, za));
    }

    @Override
    public NamedAABB move(BlockPos pos) {
        return fromAABB(super.move(pos));
    }

    @Override
    public NamedAABB move(Vec3 pos) {
        return fromAABB(super.move(pos));
    }

    @Override
    public NamedAABB move(Vector3f pos) {
        return fromAABB(super.move(pos));
    }

    @Override
    public String toString() {
        return "NamedAABB[" + name + "][" + this.minX + ", " + this.minY + ", " + this.minZ + "] -> [" + this.maxX + ", " + this.maxY + ", " + this.maxZ + "]";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o instanceof NamedAABB other) {
            return name.equals(other.name) && super.equals(o);
        }
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        return 31 * super.hashCode() + name.hashCode();
    }

    /**
     * Wraps a plain AABB into a NamedAABB, preserving this instance's name.
     */
    private NamedAABB fromAABB(AABB aabb) {
        return new NamedAABB(name, aabb.minX, aabb.minY, aabb.minZ, aabb.maxX, aabb.maxY, aabb.maxZ);
    }

    /**
     * Returns a copy of this NamedAABB with a different name.
     */
    public NamedAABB withName(String newName) {
        return new NamedAABB(newName, this.minX, this.minY, this.minZ, this.maxX, this.maxY, this.maxZ);
    }
}