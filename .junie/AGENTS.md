# FoundryEngine Project Guidelines

Project baseline for autonomous development in FoundryEngine.

## Tech Stack
- **Java 25**: Utilize modern Java features (Scoped Values, Records, Pattern Matching).
- **NeoForge**: Minecraft mod loader and framework.
- **Stonecutter**: Multi-version compatibility tool.
- **Math**: JOML (`org.joml`) for matrix and vector operations.

## Performance & Architecture
- **Allocation-Free Hot Paths**: Inner loops (rendering, ticking) must minimize allocations. Avoid `new` where possible; use object pooling or primitive collections.
- **GC Pressure**: Avoid frequent object creation in high-frequency methods. Prefer `fastutil` for primitive-specialized collections.
- **Data-Oriented Design**: Prefer arrays and contiguous memory layouts over deep object hierarchies for large-scale data processing.
- **Clean Code Reference**: Follow the `clean-code` skill for general software engineering principles (DRY, KISS, YAGNI, small functions).

## Modding Standards
- **Mixins**: Use `@Unique` for private helper methods in mixins. Always add descriptive comments explaining the reason for an injection or redirection.
- **Network Efficiency**: Optimize packet payloads. Avoid redundant data sync.
- **Stonecutter Blocks**: Strictly maintain `//? if` / `//? elif` / `//? }` blocks. Ensure compatibility logic is correct for all supported versions.

## Development Workflow
- **Code Reviews**: Use the `/review` command before committing.
- **Testing**: Ensure all new features have corresponding tests. Use `gameTestServer` for integration tests.
- **Analysis**: Use the `code-view` skill to understand complex systems before modification.
