---
layout: home

hero:
  name: "Foundry Engine"
  text: "Turn Minecraft into a Game Engine"
  tagline: An in-game editor, visual scripting, custom dimensions, cutscenes, mesh rendering, and more — all without writing a single line of Java.
  image:
    src: /FoundryEngine/logo.png
    alt: FoundryEngine Logo
  actions:
    - theme: brand
      text: "Get Started"
      link: "/guide/getting-started"
    - theme: alt
      text: "Installation Guide"
      link: "/guide/"
    - theme: alt
      text: "View on GitHub"
      link: "https://github.com/LuckyMcDev/FoundryEngine"

features:
  - title: "Bundle System"
    details: "Self-contained mod packaging — combine scripts, resources, data packs, and blueprints in a single folder. No Java compilation needed."
    link: "/concepts/core/bundles"

  - title: "Groovy Scripting"
    details: "Full Groovy 5 runtime for bundles. Register items, blocks, recipes, sounds, and particles; listen to events; build game logic — all in a scripting language."
    link: "/concepts/core/scripts"

  - title: "Builder API"
    details: "Fluent builder classes for items, blocks, recipes (all 9 types), sounds, and particles with callback hooks for custom behavior."
    link: "/concepts/core/builders"

  - title: "Rich Event System"
    details: "Over 50 events across 12 event classes — block, item, player, entity, level, network, area, client, server, command, recipe, and bundle lifecycle."
    link: "/concepts/core/events"

  - title: "In-Game Editor"
    details: "Dear ImGui-powered dockable editor with panels for blueprints, cutscenes, areas, waypoints, file browsing, code editing, and more — all accessible in-game."
    link: "/concepts/systems/editor"

  - title: "Blueprint Visual Scripting"
    details: "Node-based visual scripting with execution flow, typed pins, and built-in event nodes. Create complex game logic without writing code."
    link: "/concepts/systems/blueprints"

  - title: "Cutscene System"
    details: "Bezier-based camera paths with keyframe commands, screen effects (black bars, cinematic bars), rotational anchor points, and full in-world editor."
    link: "/concepts/systems/cutscenes"

  - title: "Instanced Worlds"
    details: "Create runtime dimensions on-the-fly with custom chunk generators, clock controls, game rules, and difficulty — temporary or persistent."
    link: "/concepts/systems/instanced-worlds"

  - title: "Game Stages"
    details: "Progression framework using named stages to gate items, mobs, dimensions, loot tables, and recipes — with cancellable stage events."
    link: "/concepts/systems/stages"

  - title: "Areas & Waypoints"
    details: "Spatial zone triggers (AABB) with enter/leave/tick events and persistent colored in-world markers for navigation."
    link: "/concepts/systems/areas"

  - title: "Custom Particles"
    details: "Keyframe-driven particle system with sequenced color, scale, velocity, position, and rotation over the particle lifetime."
    link: "/concepts/systems/particles"

  - title: "Post-Processing Effects"
    details: "Built-in shader effects (grayscale, sepia, bloom, blur) with a priority-based effect manager, custom GLSL pipelines, and fade transitions."
    link: "/concepts/systems/post-processing"

  - title: "Mesh Rendering & OBJ"
    details: "Custom 3D rendering pipeline with mesh renderer, OBJ model support, 6 render pipelines, and engine scene depth capture."
    link: "/concepts/systems/mesh-rendering"

  - title: "Game Sessions"
    details: "Managed lifecycle system for game modes and minigames with persistent NBT data and full state machine control."
    link: "/concepts/systems/game-sessions"

  - title: "Java Addon API"
    details: "Public events and builders for addon developers to hook into the engine — register panels, modify blocks/items, add data providers."
    link: "/concepts/advanced/addon-api"

  - title: "Markdown Rendering"
    details: "Render GitHub-Flavored Markdown as formatted Minecraft Component text for in-game documentation and tutorials."
    link: "/concepts/systems/markdown"
---
