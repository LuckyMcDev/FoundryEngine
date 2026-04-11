# Concepts Overview

Welcome to the **Foundry Engine** concepts documentation. These articles cover the core architectural pillars of the
engine.

- **[Bundles](bundles.md)**
    - Learn how the engine packages
      a [Resource-Pack](https://minecraft.wiki/w/Resource_pack), [Data-Pack](https://minecraft.wiki/w/Data_pack),
      and [Scripts](scripts.md) into a single functional unit.
- **[Scripts](scripts.md)**
    - Detailed look at the Groovy-based scripting system, including **Entrypoint Scripts** (loaded on start/reload) and
      **Helper Scripts** (reusable logic).
- **[Registries](registries.md)**
    - Understand the system for registering custom content like Items and Blocks using Foundry's builder-based wrapper
      for the Neoforge Registry.
- **[Workspaces](workspaces.md)**
    - Essential information on workspace directory structures, whether using the standard `.minecraft` path or the
      TemplateBundle.