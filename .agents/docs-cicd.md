# Agent Reference — Docs & CI/CD

Read this file when working on documentation or CI.

## VitePress

- Location: `docs/`
- Scripts: `npm run docs:dev` (dev) / `npm run docs:build` (prod)
- Agent reference docs live in `.agents/` (not part of the published VitePress site)

## Code Documentation

- Use Javadoc for public APIs
- Internal classes can omit javadoc (ApiStatus.Internal)
- Keep docstrings concise and accurate

## CI/CD — Qodana

- Configuration: `qodana.yaml`
- Runs on: CI pipeline
- Analyzes: Code quality, style, potential issues

## Code Analysis

- Disabled: Java linter/formatter
- Enabled: Qodana static analysis
- Focus: Logic errors, potential bugs, best practices

## Dependencies (jarJar)

| Library    | Purpose               |
|------------|-----------------------|
| ImGui      | GUI rendering         |
| Groovy     | Script execution      |
| CommonMark | Markdown parsing      |
| RenderDoc  | Debugging             |
| JEI        | In-game item info     |
| Spark      | Performance profiling |
