# Agent Reference — Bundles & Generated Code

Read this file when working with bundles, `runData`, or the data generator.

## Bundles System

**Structure**:

```
ExampleBundles/
  ├── scripts/           — Groovy entrypoints
  ├── assets/            — Assets to pack
  └── data/              — Data to pack
```

**Packing**:

- Bundles copied to `runs/client`, `runs/server`, `runs/gameTestServer`
- DynamicPackRepository registers bundles at runtime
- Pack order: user bundles → generated bundles

**Processing**:

1. Script discovery in `onConstruct`
2. Groovy execution with script engine hooks
3. Event callbacks: `onConstruct`, `onCommonSetup`, `onClientSetup`, `onDedicatedServerSetup`, `onPostInit`

## Generated Code

**BundleDataGenerator**:

- Runs in: `FMLConstructModEvent` + `commonSetup`
- Outputs to: `src/generated/resources/`
- Uses: `BundleConfig` specs from ExampleBundles
- Always run `runData` before committing (part of `preCommit`)

**Natural Language**:

- `runData` generates JSON from Groovy scripts in ExampleBundles
- Input: Groovy scripts with `@StringDefine` annotations
- Output: JSON files in `generated/resources/`
