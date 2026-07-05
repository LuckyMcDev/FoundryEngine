---
name: intellij-first
description: Enforce IntelliJ MCP tools-first workflow for Java/Minecraft mod development. Strict ordering: search_symbol → search_in_files → read_file → get_symbol_info → find_files → get_file_problems → build_project. Essential for FoundryEngine development.
---

# IntelliJ-First Workflow Skill

## Purpose
Enforce the mandatory IntelliJ MCP tools-first approach for all Java code navigation and analysis in the FoundryEngine project. This is critical for Minecraft mod development where source indexing and cross-referencing are essential.

## Tool Order (MUST FOLLOW)

### 1. Symbol Lookup — `intellij_search_symbol`
Use FIRST for any class, method, field, or interface lookup.
```json
{ "q": "Common.getBundleManager", "projectPath": "C:/Data/Projects/FoundryEngine" }
```

### 2. Text Search — `intellij_search_in_files_by_text` / `intellij_search_in_files_by_regex`
Use SECOND for finding text occurrences across files.
```json
{ "searchText": "registerInternalEvents", "projectPath": "C:/Data/Projects/FoundryEngine" }
```

### 3. File Reading — `intellij_read_file`
Use THIRD to read file contents (reads from jars too).
```json
{ "file_path": "src/main/java/de/luckymcdev/foundryengine/FoundryEngineMod.java", "projectPath": "C:/Data/Projects/FoundryEngine" }
```

### 4. Symbol Info — `intellij_get_symbol_info`
Use FOURTH for quick documentation at cursor position.
```json
{ "filePath": "src/main/java/de/luckymcdev/foundryengine/common/Common.java", "line": 43, "column": 5, "projectPath": "C:/Data/Projects/FoundryEngine" }
```

### 5. File Discovery — `intellij_find_files_by_glob` / `intellij_find_files_by_name_keyword`
Use FIFTH for finding files by pattern or name.
```json
{ "globPattern": "src/main/java/**/*Mixin*.java", "projectPath": "C:/Data/Projects/FoundryEngine" }
```

### 6. Problems Check — `intellij_get_file_problems`
Use SIXTH after edits to verify no errors.
```json
{ "filePath": "src/main/java/de/luckymcdev/foundryengine/FoundryEngineMod.java", "projectPath": "C:/Data/Projects/FoundryEngine" }
```

### 7. Build — `intellij_build_project`
Use LAST to compile/verify after changes.
```json
{ "projectPath": "C:/Data/Projects/FoundryEngine" }
```

## Forbidden Patterns
- ❌ `grep`, `find`, `ls`, `read` (filesystem tools) for Java code
- ❌ Reading files before searching for symbols
- ❌ Skipping problems check after edits
- ❌ Not passing `projectPath` parameter

## Minecraft-Specific Benefits
- Indexes decompiled Minecraft/NeoForge sources automatically
- Finds mixin targets in obfuscated/intermediate mappings
- Handles `@Shadow`, `@Inject`, `@At` annotations correctly
- Resolves Mojang mappings vs official mappings
- Cross-references between mod code and Minecraft internals

## Usage
Trigger this skill whenever starting Java/Minecraft mod work. The AI will automatically follow this workflow.