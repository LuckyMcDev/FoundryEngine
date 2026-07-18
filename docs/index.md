---
layout: home

hero:
    name: "FoundryEngine"
  text: "Turn Minecraft into a Game Engine"
    tagline: Build mods without writing Java. An in-game editor, visual scripting, custom dimensions, cutscenes, mesh rendering, and more.
  image:
    src: /FoundryEngine/logo.png
    alt: FoundryEngine Logo
  actions:
    - theme: brand
      text: "Get Started"
      link: "/getting-started/"
    - theme: alt
      text: "Installation"
      link: "/getting-started/installation"
    - theme: alt
      text: "View on GitHub"
      link: "https://github.com/LuckyMcDev/FoundryEngine"

features:
    -   title: "No Java Required"
        details: "Create mods using Groovy scripts. No compilation, no build tools. Just drop a folder and reload."
        link: "/getting-started/first-bundle"

    - title: "Bundle System"
      details: "Self-contained mod packaging with scripts, textures, models, and data in one folder."
      link: "/core-concepts/what-is-a-bundle"

  - title: "Builder API"
    details: "Simple builders for items, blocks, recipes, sounds, and particles with callback hooks."
    link: "/core-concepts/creating-items"

  - title: "Event System"
    details: "React to block breaks, player joins, mob deaths, and 80+ other game events."
    link: "/core-concepts/events-guide"

  - title: "In-Game Editor"
    details: "Dockable editor with panels for cutscenes, areas, waypoints, file browsing, and more."
    link: "/systems/editor"

  - title: "Cutscene System"
    details: "Bezier camera paths with keyframe commands, screen effects, and in-world editor."
    link: "/systems/cutscenes"

    -   title: "Custom Worlds"
        details: "Create runtime dimensions with custom chunk generators, game rules, and difficulty."
        link: "/systems/instanced-worlds"

  - title: "Game Stages"
    details: "Gate items, mobs, dimensions, and recipes behind progression milestones."
    link: "/systems/stages"

    -   title: "Areas & Waypoints"
        details: "Spatial zones with enter/leave events and colored in-world markers."
        link: "/systems/areas"

  - title: "Custom Particles"
    details: "Keyframe-driven particle system with color, scale, velocity, and rotation over time."
    link: "/systems/particles"

    -   title: "Post-Processing"
        details: "Built-in shader effects (grayscale, sepia, bloom, blur) with fade transitions."
        link: "/systems/post-processing"

    -   title: "Mesh Rendering"
        details: "Custom 3D rendering pipeline with OBJ model support and engine scene depth."
        link: "/systems/mesh-rendering"

    -   title: "Dialogue System"
        details: "Branching NPC conversations with conditions, actions, and display modes."
        link: "/systems/dialogue"

  - title: "Game Sessions"
    details: "Stateful lifecycle system for minigames and custom game modes with persistent data."
    link: "/systems/game-sessions"

  - title: "Java Addon API"
    details: "Public events and builders for Java developers to extend the engine."
    link: "/advanced/addon-api"
---
