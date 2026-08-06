# What is FoundryEngine?

FoundryEngine is a Minecraft mod (for NeoForge) that turns Minecraft into a **game engine**. It lets you add new items, blocks, recipes, mechanics, cutscenes, and even entire dimensions — **without writing any Java code**.

## Who is this for?

| You want to...                       | FoundryEngine helps you...                                              |
|--------------------------------------|-------------------------------------------------------------------------|
| Add a custom sword or food item      | Create it with a few lines of [Groovy](https://groovy-lang.org/) script |
| Build a dungeon with special rules   | Create a custom dimension and fill it with areas and stages             |
| Make an NPC that talks to players    | Use the dialogue system with branching conversation trees               |
| Cinematic camera shots               | Use the cutscene system with Bezier paths and screen effects            |
| Gate content behind player progress  | Use the game stages system                                              |
| Learn Minecraft modding without Java | Start here — no Java needed                                             |

## How does it work?

FoundryEngine uses **bundles** — folders containing scripts, textures, models, and data files. You drop a bundle into a folder, run `/engine reload` in-game, and your content appears. No compilation, no build tools.

Each bundle uses [Groovy](https://groovy-lang.org/) (a scripting language that runs on Java) to define items, blocks, recipes, and behaviors. If you already know Java, you can also write Java addons.

## Key concepts at a glance

| Concept                                        | What it does                                      |
|------------------------------------------------|---------------------------------------------------|
| [Bundle](../core-concepts/what-is-a-bundle.md) | A self-contained mod packaged as a folder         |
| [Groovy Script](../core-concepts/scripts.md)   | A `.groovy` file that defines your content        |
| [Builder](../core-concepts/creating-items.md)  | A helper that creates items, blocks, recipes      |
| [Event](../core-concepts/events-guide.md)      | A hook that runs code when something happens      |
| [System](../systems/)                          | A major feature (editor, cutscenes, stages, etc.) |

## First steps

1. [Install FoundryEngine](installation.md)
2. [Create your first bundle](first-bundle.md)
3. [Choose your workspace](workspaces.md)
4. [Explore the core concepts](../core-concepts/)
5. [Check out examples](../examples/)
6. Explore the [Showcase Bundle](../examples/showcase-bundle.md) — a full bundle demonstrating items, blocks, recipes, areas, waypoints, stages, dialogue, commands, and game sessions
