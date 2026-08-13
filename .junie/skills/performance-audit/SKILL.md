---
name: performance-audit
description: Audits code for performance bottlenecks, GC pressure, and inefficient patterns in game engine development.
---

# Performance Audit Skill

Use this skill when analyzing hot paths (rendering, ticking, networking) or when requested to "audit performance".

## Key Principles
- **Minimize Allocations**: Every `new` in a hot loop is a potential GC spike.
- **Avoid Boxing**: Use primitive types and primitive-specialized collections (`fastutil`).
- **Cache Locality**: Access data in a way that is friendly to the CPU cache (contiguous arrays).
- **Branch Prediction**: Minimize branching in tight loops.

## Audit Checklist
- [ ] Check for `new` keyword in loops or high-frequency methods.
- [ ] Identify `Iterator` creation (e.g., enhanced for-loop on a `Collection`).
- [ ] Look for boxing/unboxing (e.g., `List<Integer>` instead of `IntList`).
- [ ] Verify `JOML` operations use `.set()` or `.into()` instead of creating new instances.
- [ ] Check for redundant calculations that can be cached.

## Examples

### Inefficient
```java
for (Entity entity : entities) {
    Vector3f pos = new Vector3f(entity.getX(), entity.getY(), entity.getZ()); // Allocation!
    process(pos);
}
```

### Efficient
```java
Vector3f tempPos = new Vector3f(); // Pooled/Field
for (int i = 0; i < entities.size(); i++) {
    Entity entity = entities.get(i);
    tempPos.set(entity.getX(), entity.getY(), entity.getZ());
    process(tempPos);
}
```
