---
layout: home

hero:
  name: "Foundry Engine"
  text: "Turn Minecraft into a Game Engine"
  tagline: An in-game editor, visual scripting, custom dimensions, cutscenes, and more — all without writing a single line of Java.
  image:
    src: /FoundryEngine/logo.svg
    alt: FoundryEngine Logo
  actions:
    - theme: brand
      text: "Get Started"
      link: "/guide"
    - theme: alt
      text: "View on GitHub"
      link: "https://github.com/LuckyMcDev/FoundryEngine"

features:
  - title: "In-Game Editor"
    details: "Dear ImGui-powered dockable editor with panels for blueprints, cutscenes, areas, waypoints, file browsing, code editing, and more — all accessible in-game."
    link: "/concepts/editor"

  - title: "Blueprint Visual Scripting"
    details: "Node-based visual scripting with execution flow, typed pins, and built-in event nodes. Create complex game logic without writing code."
    link: "/concepts/blueprints"

  - title: "Groovy Scripting Engine"
    details: "Full Groovy 5 runtime for bundles. Register items, blocks, recipes, sounds, and particles; listen to events; build game logic — all in a scripting language."
    link: "/concepts/scripts"

  - title: "Cutscene System"
    details: "Bezier-based camera paths with keyframe commands, screen effects (black bars, cinematic bars), rotational anchor points, and full in-world editor."
    link: "/concepts/cutscenes"

  - title: "Instanced Worlds"
    details: "Create runtime dimensions on-the-fly with custom chunk generators, clock controls, game rules, and difficulty — temporary or persistent."
    link: "/concepts/instanced-worlds"

  - title: "Game Stages"
    details: "Progression framework using named stages to gate items, mobs, dimensions, loot tables, and recipes — with cancellable stage events."
    link: "/concepts/stages"

  - title: "Areas & Waypoints"
    details: "Spatial zone triggers (AABB) with enter/leave/tick events and persistent colored in-world markers for navigation."
    link: "/concepts/areas"

  - title: "Bundle System"
    details: "Self-contained mod packaging — combine scripts, resources, data packs, and blueprints in a single bundle. No Java compilation needed."
    link: "/concepts/bundles"

  - title: "Post-Processing Effects"
    details: "Built-in shader effects (bloom, blur, grayscale, invert, creeper vision) with a priority-based effect manager."
    link: "/concepts/post-processing"

  - title: "Custom Particles"
    details: "Keyframe-driven particle system with sequenced color, scale, velocity, position, and rotation over the particle lifetime."
    link: "/concepts/particles"

  - title: "Builder API"
    details: "Fluent builder classes for items, blocks, recipes (all 9 types), sounds, and particles with callback hooks for custom behavior."
    link: "/concepts/builders"

  - title: "Rich Event System"
    details: "Over 50 events across 12 event classes — block, item, player, entity, level, network, area, client, server, command, recipe, and bundle lifecycle."
    link: "/concepts/events"
---
