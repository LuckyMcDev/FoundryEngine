# Agent Reference — Troubleshooting

Read this file only when debugging the issues described below. For everything else, use the IntelliJ MCP tool ordering (see AGENTS.md).

## Build Fails

- Clean and rebuild: run the `FoundryEngine [build]` Gradle run configuration, or use the terminal: `./gradlew.bat clean build`
- Refresh dependencies: `./gradlew.bat build --refresh-dependencies`
- Always check `get_file_problems` on the failing file before rebuilding — most "build fails" are IDE-visible errors.

## Tests Failing

- Debug output: `./gradlew.bat test --debug`
- Force re-run: `./gradlew.bat test --rerun-tasks`
- Run one test class: `./gradlew.bat test --tests "*PlayerEventsTest"`

## Mixin Not Working

- Use `mixin-writing` skill for detailed guidance.
- Check target class visibility.
- Verify `@Mixin` annotations.
- Use MixinMCP (`mixin_*` tools) to find correct target methods.

## Bundle Not Loading

- Check bundle discovery in `onConstruct`.
- Verify bundle path in `copyExampleBundles`.
- Check bundle dependencies.
- Enable debug logging for bundle loading.

## Network Packet Not Receiving

- Verify packet registration in `onCommonSetup`.
- Check packet bounds compatibility.
- Verify sync in `onServerStarted()` / `onPlayerChangedDimension()`.
- Check NeoForge network registration.
