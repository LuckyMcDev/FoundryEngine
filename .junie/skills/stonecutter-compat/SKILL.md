---
name: stonecutter-compat
description: Ensures correct usage and maintenance of Stonecutter multi-version compatibility blocks.
---

# Stonecutter Compatibility Skill

Use this skill when editing files containing `//? if` blocks or when refactoring code across multiple Minecraft versions.

## Guidelines
- **Maintain Syntax**: Never break the `//? if`, `//? elif`, `//? }` structure.
- **Version Isolation**: Ensure that code inside a version block only uses APIs available in that version.
- **Duplicate Logic**: If a change is needed in one version, check if it needs to be mirrored or adapted in other version blocks within the same file.
- **Verification**: If possible, verify the build for all target versions.

## Pattern
```java
//? if 26.1 {
import com.mojang.blaze3d.shaders.UniformType;
//?} elif 26.2 {
import com.mojang.blaze3d.pipeline.UniformType;
//?}
```
