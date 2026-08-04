## [unreleased]

### Features

- *(suggestions)* Implement NBT command suggestions
- Feat (suggestions): update NBT suggestions also move all suggestion mixins
- Feat (editor): implement more advanced code editor stuff error highlighting workspace state

working towards ide like behavior
- *(editor)* Script editor, outline panel, imgui downgrade
- *(command)* Implement binding commands to item stacks.
- *(gizmo)* Add GizmoBuffer and GizmoRenderer for improved rendering of gizmos
- *(network)* Add CustomDataPacket and CustomDataReceivedEvent for handling custom data.
- *(crash-report)* Add crash report callables for bundles and game sessions
- *(io)* Add JsonIO and NBTIO classes for JSON and NBT file handling
- Feat (script): add ScriptSandbox improvements to script security.
- Feat (builder): add barrier block type functionality add methods in block builder for most block types (stairs slabs etc)
- *(docs)* Document block variants and ghost blocks
- Feat (imgui): replace ImGuiShortcut with HotKeyManager add java port of ImHotKey (https://github.com/CedricGuillemet/ImHotKey)
- *(builder)* Add support for block and item tags in respective builders
- *(utils)* Add ReflectionHelper
- *(builder)* Add block entity support
- *(submodule)* Add TestBundle as submodule
- *(ui)* Replace custom UI constraints with Taffy layout engine
- *(ui)* Add vanilla-style buttons, sliders, and checkboxes
- Feat (dialogue): add stack-based styling and typewriter controls closes #42
- *(testbundle)* Test utility groovy scripts
- *(level)* Add config option to skip experimental warning
- *(sound)* Add mp3 support to the sound engine
- *(sound)* Add flac support to the sound engine and add test sounds
- Feat (public): Update logo and render logo in menu bar, full readme rewrite also add excalidraw idea thingie sheet. idk if i will keep it in the git but it may be updated. fix vitepress
- *(gradle)* Add credits property for mod toml generation

### Bug Fixes

- *(docs)* Update public text
- *(stage)* Fix wrong cast in GameStageHandler
- *(package)* Delete unused package
- *(editor)* Fix a bug where notifications cause the menu bar to be empty.
- *(bundle)* Fix resource packs not being enabled on startup.
- *(sides)* Fix some client-server-common discrepancies
- *(resources)* Fix a parsing bug where an _ in a directory name was misinterpreted.
- *(lang)* Add missing lang entry
- *(events)* Fix some things in event bus usages and event classes
- Fix indenting in the crash report callables
- *(dependencies)* Update Sodium and Iris dependencies to latest versions
- *(bundle)* Fix bundle config management to not double register on reload causing an error
- *(docs)* Fix indenting in index file
- *(docs)* Fix indenting again. add missing constructor to VoidChunkGenerator
- *(common)* Fix registry access during frozen registry.
- Fix (script) rewrite scripting v1
- *(script)* Rewrite scripting v2
- *(script)* Fix a bug with closures in scripts.
- *(stage)* Fix forward reference
- *(gradle)* Fuck intellij code cleanup
- *(script)* Fix another issue with closure transformer
- *(docs)* Fix image link in readme

### Other

- Merge branch 'master' of https://github.com/LuckyMcDev/FoundryEngine
- *(session)* Had to rework it again. "should" be better now
- *(build)* Migrate full build to use a custom plugin

### Refactor

- *(network)* Remove TestPacket and streamline action handling
- *(game)* Rework game session management to be per world and auto start.
- *(stage)* Rework stage system
- *(editor)* Remove unused MainEditor panel.
- Refactor (script): rework scripting system to be easier to work with and finally fix eval command to use actual groovy shell. for now removes the extensibility of other languages.
- *(gizmo)* Rework some of the world gizmo logic and increase the size of which it activates hovers.
- *(managers)* Fix manager calls in other managers for weird dependencies
- *(bundle)* Rework bundle logic in modlist screen so that it uses mod containers for display.
- Refactor (bundle): switch to neoforge config system for easier management. Lang file generation for configs needs to be figured out. Not sure how yet. Also added an event bus to the bundle mod config.
- *(imgui)* Use GlStateManager for main window rendering in ImGuiImplGl3
- *(io)* Replace simple path validation with more advanced resolution logic.
- *(imgui)* Remove opengl support for less than version 330
- *(fonts)* Transition to minecraft fonts for font management.
- *(docs)* Reformat code in docs
- *(code-review)* Fix lots of quodana issues.
- *(main, test)* Run reformat code
- *(docs)* Rework docs and add showcase bundle
- *(docs)* Simplify event examples and explanations
- *(showcase)* Update showcase bundle
- *(showcase)* Uniform formatting in entrypoint scripts
- *(gradle)* Include TestBundle module
- *(docs)* Re-read and rewrite some parts of docs
- *(cleanup)* Remove obsolete test files, fix lots of code review issues.
- *(ai)* Refactor the way ai works in this project.
- *(build)* Migrate modular gradle files into buildSrc
- *(testbundle)* Disable transitive dependencies
- Refactor (gradlePlugin): Create full gradle plugin for development.
- auto mixin json generation
- easy mincraft setup
- use libs.versions.toml

### Documentation

- Update and expand various guides with corrections

### Testing

- Test deepwiki thingy

### Miscellaneous Tasks

- *(reformat)* Run reformat code
- *(reformat)* Run Code Cleanup
- *(package)* Add package info files
- *(info)* Add missing package info file
- *(gradle)* Bump version to 0.1.3
- *(nullability)* Make signature nullable in AutocompleteItem
- Bump mod_version to 0.1.4
- Delete obsolete Adapter class
- Chore (deps): bump NeoForge to 26.1.2.78 and ImGui to 1.92.7.1 forgot to push this, is relevant for last 3 or 4 commits.
- *(cleanup)* Remove obsolete scripts, configs, and example code
- *(changelog)* Update changelog
- *(todo)* For todo go to github issues / project
- *(docs)* Ignore TypeScript error in custom.css import
- *(gradle)* Update Gradle wrapper to v9.6.1
- *(docs)* Word some things differently.
## [0.1.2] - 2026-07-06

### Features

- *(network)* Replace direct command execution with packet-based system
- *(dialogue)* Add dialogue system
- *(dialogue)* Add speaker and text formatting support
- *(dialogue)* Add style support and improve editor UI
- *(docs)* Add dialogue system documentation and other doc updates
- *(dialogue)* Add typewriter effect to Screen, migrate DialogueStyle to use Color class
- *(dialogue)* Add typewriter sound effect
- *(dialogue)* Add dynamic panel height calculation
- *(render)* Implement OBJ MTL parsing and rendering.
- Add custom text editor and scuffed input blocking
- *(editor)* Implement theme selection and JSON/TOML support in text editor
- *(node)* Update display names to use translatable components
- *(gizmo)* Add GizmoTestPanel and related classes for enhanced Gizmo functionality
- *(gizmo)* Update editor tool / feature system to now use new WorldGizmo class for rendering and hovering
- *(builder)* Add Tag and Tool Material Builders.
- *(builder)* Update tag builder for easier use
- *(ai)* Some ai stuffs.

### Bug Fixes

- Update neo compatability
- *(dialogue)* Fix event registration
- *(dialogue)* Resolve incorrect tree replacement during load
- *(dialogue)* Fix typewriter resetting on resize, fix button sizes
- *(render)* Fix OBJ model Transparency rendering and texture fallback
- *(events)* Fix EntityEvents having the same event three times, remove one. Still needs updating
- Fix null safety in Common and FoundryEngineMod Mod bus
- *(javadoc)* Fix javadoc that was causing workflow to fail
- *(javadoc)* Update implNote to reason in BlockEntityRendererMixin
- *(build)* Skip tests during build due to unknown reason for failure
- *(resources)* Prevent duplicate resource IDs in ExplorerPanel and update zip file access method
- *(editor)* Fix a bug with status codes

### Other

- *(gradle)* Add sources jar to publication
- Modify build command to skip tests
- Merge branch 'master' of https://github.com/LuckyMcDev/FoundryEngine

### Refactor

- *(imgui)* Remove deprecated OpenGL backend, replace with mc rendering. update texture drawing system, add component drawing
- *(code + javadoc)* Update some code and javadoc
- *(dialogue)* Move editor panel and adjust text input size
- *(dialogue)* Update some stuff
- *(key)* Remove key binding system and move shortcut class
- Run reformat code
- *(savedata)* Rework saved data.
- *(icons)* Replace ScreenIconExporter with IconExporterLayer and switch to off-screen rendering
- *(icons)* Make icon exporting just command and icons in imgui render into offscreen buffer dynamically
- *(icons)* Optimize icon cache handling with pending keys
- *(node)* Make x button be an icon and add padding
- *(savedata)* Rename GAME_DATA to ENGINE_DATA, compress I/O
- *(imgui)* Big ImGui update.
- *(imgui)* Update ImGui to version 1.92.0
- *(imgui)* Centralize context mgmt via ImGuiContextStack
- *(editor)* Remove RegisterPanelEvent and its documentation
- *(docs)* Update documentation
- *(registry)* Replace RegistryEvent with RegistryCollector for easier management
- Replace hardcoded strings with translatable components in various places
- Remove unused waypoint keybinds, fix key category translation key
- *(editor)* Rework texture viewer panel and fix dynmic texture leak in ImGraphicsExtractor
- *(node)* Improve pin list handling in Node and NodeEditorInstance
- *(bundle)* Rework registering and recipes
- *(builder)* Rework recipe builder to delegate to mc builders.
- Refactor (icon): update Icons to be normal names. no more differentiation between FAE and FA
- *(explorer)* Merge File and Resource explorer into one.

### Documentation

- Big documentation update on multiple fronts
- Add images

### Testing

- Add unit tests for a lot of stuff.

### Miscellaneous Tasks

- Updates before release
- *(editorconfig)* Add .editorconfig file
- Remove RegisterRenderingStuffEvent
- *(qodana)* Update configuration for Qodana analysis
- *(dependencies)* Update mod version to 0.1.2 and add and comment out sodium and iris
- *(ci)* Update action versions in workflow files to latest stable releases

### Revert

- Revert since it somehow broke? i dont even understand why
## [0.1.0] - 2026-06-21

### Features

- *(panel)* Introduce builder pattern for panel creation
- *(area)* Extend the area system with different area types, fix bugs, update renderer, update testing
- *(shaders)* Add skybox item with custom shader effects
- *(skybox)* Add dynamic skybox rendering system
- *(skybox)* Implement better skybox and add toggle option

### Bug Fixes

- Remove broken ci things?
- *(docs)* Update waypoint example to use ChatIcons enum
- Update item modification to use updated method

### Other

- Merge remote-tracking branch 'origin/master'
- I hate ci
- Add some utils to post processing and ids

### Refactor

- *(area)* Full rework of area system based on modules.
- *(area)* Rework area rendering and implement area rendering module
- *(server/client)* Make foundryengine work server only, and client only
- *(config)* Simplify config structure by removing EngineConfig abstraction
- *(post)* Rewrite post-processing system
- *(shaders)* Extract skybox logic into reusable include file users can override
- *(items)* Remove editor item and related assets

### Miscellaneous Tasks

- Go back to old ci, add publish task which gets run on tag
- Bump version
- *(deps)* Update neoforge version to 26.1.2.76
- Allow manual trigger for publish workflow
## [0.0.67] - 2026-06-19

### Features

- Add slot modification system
- *(gradle, ci)* Add manual release workflow and changelog task

### Bug Fixes

- *(ci)* Update secrets

### Other

- Refactor GitHub Actions workflows and add changelog generation [skip ci]

### Refactor

- *(events)* TitleScreenModifyEvent to TitleScreenModificationEvent and move to modification package
- Self-registering event clear pattern

### Miscellaneous Tasks

- Fix bundle scripts, update docs
- Add ai stuff
- Update AGENTS.md
- Bump version to 0.0.67 [skip ci]
## [0.0.66] - 2026-06-19

### Features

- Add slot modification system
- *(gradle, ci)* Add manual release workflow and changelog task
- *(panel)* Introduce builder pattern for panel creation
- *(area)* Extend the area system with different area types, fix bugs, update renderer, update testing
- *(shaders)* Add skybox item with custom shader effects
- *(skybox)* Add dynamic skybox rendering system
- *(skybox)* Implement better skybox and add toggle option
- *(network)* Replace direct command execution with packet-based system
- *(dialogue)* Add dialogue system
- *(dialogue)* Add speaker and text formatting support
- *(dialogue)* Add style support and improve editor UI
- *(docs)* Add dialogue system documentation and other doc updates
- *(dialogue)* Add typewriter effect to Screen, migrate DialogueStyle to use Color class
- *(dialogue)* Add typewriter sound effect
- *(dialogue)* Add dynamic panel height calculation
- *(render)* Implement OBJ MTL parsing and rendering.
- Add custom text editor and scuffed input blocking
- *(editor)* Implement theme selection and JSON/TOML support in text editor
- *(node)* Update display names to use translatable components
- *(gizmo)* Add GizmoTestPanel and related classes for enhanced Gizmo functionality
- *(gizmo)* Update editor tool / feature system to now use new WorldGizmo class for rendering and hovering
- *(builder)* Add Tag and Tool Material Builders.
- *(builder)* Update tag builder for easier use
- *(ai)* Some ai stuffs.
- *(suggestions)* Implement NBT command suggestions
- Feat (suggestions): update NBT suggestions also move all suggestion mixins
- Feat (editor): implement more advanced code editor stuff error highlighting workspace state

working towards ide like behavior
- *(editor)* Script editor, outline panel, imgui downgrade

### Bug Fixes

- Fix issues with game management
- Fix gradle bump version task
- *(ci)* Update secrets
- Remove broken ci things?
- *(docs)* Update waypoint example to use ChatIcons enum
- Update item modification to use updated method
- Update neo compatability
- *(dialogue)* Fix event registration
- *(dialogue)* Resolve incorrect tree replacement during load
- *(dialogue)* Fix typewriter resetting on resize, fix button sizes
- *(render)* Fix OBJ model Transparency rendering and texture fallback
- *(events)* Fix EntityEvents having the same event three times, remove one. Still needs updating
- Fix null safety in Common and FoundryEngineMod Mod bus
- *(javadoc)* Fix javadoc that was causing workflow to fail
- *(javadoc)* Update implNote to reason in BlockEntityRendererMixin
- *(build)* Skip tests during build due to unknown reason for failure
- *(resources)* Prevent duplicate resource IDs in ExplorerPanel and update zip file access method
- *(editor)* Fix a bug with status codes
- *(docs)* Update public text
- *(stage)* Fix wrong cast in GameStageHandler
- *(package)* Delete unused package
- *(editor)* Fix a bug where notifications cause the menu bar to be empty.
- Fix issues with game management
- Fix gradle bump version task

### Other

- Setup + imgui
- Move imgui to client package
- Update imgui with ContextType thing. Its for having the ImGuiContextStack which can make it easier to create multiple windows with different imgui contexts.
- Lots of ImGui changes including:
  Node system. Font managment. Context and graphics stack update / addition Commons and Instances for hard coded things
- Forgot to commit the font file
- Delete TestImguiImpl.java
- Update icons. All Icons are now available.
- Add simple registry things.
- Update gl stuff, unwrapping texture is now done inside of Client.java
- Try to do the like imgui makes minecraft smaller thing, that did NOT work!
  I may or may not do it again, but the next commit will remove this again just to make it cleaner.
- Removed the docking stuff.
- Run reformat code.
- Remove unused frame tracking and dpiScale from ImGuiImpl
- Rename ImGuiImpl to ImGuiHandler
- Add ResourceRegistry and Commons.id () helper method
- Add NoMixinException for when i want to implement a method in a mixin, but that method then didnt get implemented, that exeption is thrown.
- Remove EditorWindow and add top info bar to ImGuiHandler
- Add panel system and built-in editor with test and node editor panels
- Remove NodeEditorExample and add DELETE key node removal
- Add Easing class with Robert Penner's easing functions
- Move Client methods to Instances and update references
- Add OpenGL shader and vertex system with dispatch and utilities
- Add ImGuiGraphics class and refactor style setup in ImGuiHandler
- Refactor TestRender to use custom FrameBuffer and simplify rendering pipeline

- Replace quad with triangle vertices
- Use FrameBuffer for custom rendering target
- Simplify shader to solid red color
- Remove unnecessary state management
- Update uniform class to use record syntax
- Remove GlConst and use Mojang's GlConst instead
- Add dependency for createMinecraftArtifacts task on generateModMetadata
- Refactor TestRender with improved post-processing pipeline

- Add OpenGlStack for state management
- Implement proper framebuffer resizing and validation
- Add FrameBufferManager for centralized framebuffer handling
- Update shader to apply grayscale effect
- Add texture coordinates to vertex data
- Improve error handling with try-finally blocks
- Add new shader programs and vertex layouts
- Update mixin configuration and add RenderPipelinesInvoker
- Add glObjectLabel method to GlDispatch for OpenGL object labeling
- Enhance FrameBuffer with depth/stencil support, debug labels, and improved error handling

- Add configurable depth and stencil attachments
- Implement debug labeling for OpenGL objects
- Add comprehensive error messages for framebuffer status
- Introduce clear color management and texture filtering
- Refactor binding methods with viewport control options
- Improve attachment creation and validation logic
- Update Panel to not instantly dissallow Collapsing of windows
- Remove deprecated shader programs fragmentv1 and vertexv1
- Use other ImGui Theme
- Refactor TestRender with improved framebuffer blitting and state management

- Add TbRenderer and TbRenderSystem for centralized rendering control
- Implement FrameBufferManager.blit () for efficient framebuffer copying
- Remove manual framebuffer binding and restore OpenGL state
- Add MinecraftMixin to initialize TbRenderSystem on game startup
- Update mixin configuration to include MinecraftMixin
- Add disable method to ShaderProgram to reset active program
- Disable access transformers configuration
- Add ClientMatrices interface for matrix operations
- Move ImGui mixins to main package and add LevelRendererMixin

- Merge imgui.MinecraftMixin into MinecraftMixin
- Rename imgui input mixins to input package
- Add render.LevelRendererMixin for matrix operations
- Update mixin configuration paths
- Add uniform buffer object support and camera-related uniforms

- Introduce `Uniforms` class with camera and projection matrix uniforms
- Add `UniformBufferObject` for efficient uniform data handling
- Extend `ShaderProgram` with uniform block binding support
- Update `TbRenderer` to handle client matrix updates via `FrameGraphSetupEvent`
- Add camera access method to `Instances`
- Include new OpenGL uniform block functions in `GlDispatch`
- Fix updateClientMatrices not being static
- Add LWJGL extensions

- meshoptimizer
- opencl
- par
- shaderc
- Update Mesh class to support mesh optimization and indexed rendering

- Add mesh optimization using meshoptimizer library
- Refactor Mesh constructors to use indices and optimization flag
- Update VertexBufferObject and ElementBufferObject to support buffer data upload
- Modify Vertices class to include indices in VertexMesh record
- Update TestRender to use indices for fullscreen quad rendering
- Add sphere mesh generation using ParShapes library
- Run Refactor Code
- Refactor code 2
- Add OpenCL compute framework with Perlin noise example

- Introduce OpenCL context, program, kernel, and buffer management
- Add compute task builder with argument reflection
- Implement Perlin noise generation kernel
- Include performance benchmarking and visualization utilities
- Add worker thread for asynchronous OpenCL operations
- Refactor Commons class imports and move getRlSource method
- Add BuiltInEditor instance management via Instances
- Add client tick handling and refactor BuiltInEditor panel management
- Add Mixin interfaces, refactor when MinecraftMixin calls things.
- Rename package "cl" to "opencl" and "gl" to "opengl"
- Add OpenCL worker shutdown on Minecraft close
- Move RenderPipelinesInvoker to invoker subpackage
- Rename `getGlTexture` to `getGlColTexture` and add `getGlDepthTexture` method
- Refactor OpenCL dispatch system and introduce thread safety checks
- Add resource reload logging and server reload listener registration
- Try add RenderNurse to minecraft Make lwjgl bom read from gradle.properties#lwjgl_version RenderNurse does NOT work yet.
- Move Config class to config package and make SPEC public
- Remove TbRenderSystem and refactor renderer access via Instances Also fix Imgui font not loading by making it a ReloadListener Change TTFFile constructor
- Remove RenderNurse agent and wrap dependencies with jarJar
- Add .idea and .run to .gitignore and create RenderDoc.cmd
- Refactor texture and device access with instance checks
- Add /repo to .gitignore
- Update LWJGL dependencies to use version from gradle.properties and fix repo URL
- Add RenderDoc run configuration and rename script to .bat
- Add safe wrapper for glObjectLabel to handle unsupported cases
- Add Label support for shaders, mostly for RenderDoc viewing
- Add RenderDoc capture file
- Refactor post-processing system and add grayscale pipeline
- Refactor FrameBuffer to simplify initialization and remove debug label handling
- Refactor texture access methods
- Switch from RenderGuiEvent.Post to RenderLevelStageEvent.AfterLevel
- Add post-processing panel and refactor pipeline management
- Register shaders and programs with manager and enable hot-reload
- Remove redundant initialization flag in PostProcessManager
- Add Javadoc to Editor and Panels.
- Update IDs to snake case
- Add key binding system and manager
- Add Default Uniforms to all Post Process Pipelines.
- Add GLSL preprocessor system with include support
- Refactor post-processing system and add depth visualization

- Move grayscale shader to new location and update weights
- Add depth visualization pipeline with new shader
- Replace renderbuffer with texture for depth attachment
- Improve framebuffer validation and error handling
- Restructure pipeline execution with proper texture binding
- Remove redundant code and simplify buffer management
- Add automatic buffer resizing when main target changes
- Add grayscale vertex shader and rename depth visualize shader
- Remove unused FrameBufferManager import
- Add Javadoc to OpenGL classes and methods
- Update to 1.21.11

- replace ResourceLocation with Identifier
- Change Name from ToolboxLib to Foundry Engine and add unit testing
- Refactor package structure to use foundryengine prefix.
- Add staged post-processing pipeline system with execution at specific render stages
- Update Registry Functionality and refactor usages.
- Update RegistryRef to follow updated Registry
- Add SQLite-JDBC dependency and version property
- Add OpenGL debug group support for post-processing pipelines
- Update RenderDoc run configuration to singleton mode
- Add main menu bar and refactor panel management
- Add MODNAME constant and config paths to Commons
- Fix ImGui demo and metrics window toggle behavior
- Add getter method for shader list in ShaderProgram
- Add ASCII post-processing pipeline and refactor shader registration
- Refactor post-processing pipeline system and improve ASCII shader

- Restructure pipeline classes into `pipeline` package hierarchy
- Replace direct program management with event-based registration
- Add `PostProcessPipelinePass` for multi-pass pipeline support
- Update ASCII shader to handle dark upstream passes better
- Improve depth visualization with proper inversion
- Add stage selection UI for staged pipelines
- Add ORMLite dependency for future data management
- Update RenderDoc configuration to use FoundryEngine module
- Update shader paths and refactor imports
- Add framebuffer blit methods for RenderTarget and FrameBuffer
- Remove RenderNurse
- Add pipeline parameter system and refactor post-process panel
- Add Groovy dependency and refactor build.gradle dependency management
- Remove broken import.
- Remove TbRenderer and rename ImGuiHandler.java to ImGuiManager.java
- Fix removed TbRenderer reference
- Remove PassTarget enum and refactor post-processing pipeline system

- Replace PassTarget with TargetRef and TemporaryTarget
- Add built-in GrayscalePipeline and DepthVisualizePipeline
- Update PipelineParam to use Vector2f and Vector3f without full package names
- Fix ImGui tree node rendering in PostProcessPanel
- Change cellSize uniform type from float to int in ASCII shader
- Simplify TestRender by using new built-in pipelines
- Move onClosed () call to close () method and remove redundant check
- Refactor ImGui managment system and add custom Gl3 and Glfw Impl classes.
- Refactor panel system and move panels to builtin package

- Move panels to `io.github.luckymcdev.foundryengine.client.editor.builtin`
- Add `isOpen()` method to `Panel` class
- Move `onClosed()` call to after panel is closed
- Add `RegisterPanelEvent` for panel registration
- Refactor `MainMenu` to dynamically render panel menu items
- Simplify panel management in `BuiltInEditor`
- Add `PopUp` class for popup management
- Fix wrong open function call in BuiltInEditor
- Update Node Editor with testing shader editor thing
- Simplify PostProcessPipeline
- Remove redundant comments and unused GlStateSnapshot class
- Refactor event handling and panel registration

- Add event posting methods to `Instances` class
- Move event posting to `FMLClientSetupEvent` enqueueWork
- Register panels and post-processing pipelines during client setup
- Remove redundant event handlers and cleanup imports
- Add FileManager WIP
- Call RegisterRenderingStuffEvent.
- Update KeyBindingManager to register Keys with Minecraft
- Remove ClWorker and replace with ThreadManager system

- Introduce new `ThreadManager` and `EngineThread` classes for centralized thread management
- Replace OpenCL-specific `ClWorker` with generic thread management system
- Update `ClDispatch` to use new thread assertion mechanism
- Refactor `InstancesInternal` to initialize `ThreadManager` and use static initialization block
- Clean up redundant key binding registration and update key name format
- Move OpenCL worker thread shutdown to centralized `ThreadManager.shutdownAll()` call
- Remove unused glDevice field from GlDispatch
- Add preCommit Task

Signed-off-by: LuckyMcDev <lucky.dev@myyahoo.com>
- Refactor everything + add Javadoc.
- Add .idea/ and .run/ to .gitignore
- Remove duplicate vertex shaders and consolidate into single vert.vsh
- More Javadoc + package-info for main packages.
- Javadoc for FoundryEngineMod
- Add Doxygen configuration and ignore .docs/ directory
- Update Doxyfile
- Fix OnlyIn, since it throws some sort of Error?
- Update Doxyfile
- Add `supOf` utility method to Commons
- Refactor uniform handling to use dynamic suppliers and type-based dispatch
- Reformat build.gradle and increase jvm heap size from 2G to 4G
- Update key binding category to use translatable component
- Add editor toggle key binding
- Make `InstancesInternal` public and add constructor access control
- Make RenderPipelinesInvoker use the NoMixinException
- Update Doxyfile
- Fix Slogan
- Add ImGui utilities and color class, refactor font handling
- Add full Bundle System, which is like a Mod, but using Groovy scripts.
- Test Gse Dependency managment?
- Add client-side architecture and refactor logging levels, also bump version to 1.0.0 for release
- Fix more Client Server Common stuff, now works fully on both client and server.
- Delete Unused ClientMatrices.java
- Add more builtin Post-Processing Effects.
- Add documentation for PostProcessPipeline class
- Add keyboard shortcuts to panels and main menu
- Update Shortcut label building.
- Add support for bundles to be ZIP files.
- Refactor editor panels and add main editor panel

allow for docking in central node. if a window is docked in central node, cancel minecraft input.
- Remove old NodeEditorPanel and introduce new node language system
- Remove tick print from example bundle because its too annoying
- Add FileExplorerPanel and CodeEditor with bundle reloading support
- Improve File Explorere and CodeEditor, fix bundle resource loading.

i did an oopsie with the resource loading from bundles, should be fixed now. atleast the shader works.

Added test shader from bundle testbundle. so now custom shader + registry from in bundle works.
- Add BrowserPanel with MCEF integration and update dependencies from java-cef to mcef
- Refactor node editor packages to imnodes and add NodeEditor context type
- Remove shortcut from Code editor.
- Make browser display Javadoc + add toggle button for javadoc / doxygen
- Remove basically unused script factory things
- Add Foundry helper commands. Hand, which prints info about your hand Dump, which dumps all things in all / a specific registry.
- Fix Typo in HandCommand.
- Add Codecs to BundleInfo and BundleFiles
- Add client connection and packet sending utilities
- Fix HandCommand to display correct value in hover text
- Add InGame Console Viewer and LogAppender
- Move TestClass to foundryengine.test
- Update Doxygen to add .docs/wiki to the documentation.
- Delete kernels because opencl is no longer supported
- Try to add groovy doc to doxygen, its a bit weird now.
- Remove testPost.fsh
- Add ScriptEngineModifyEvent for custom script engine configuration
- Update default browser URL to example.com
- Update file explorer and code editor UI improvements
- Comment out debug print statements in Dependency and TestBundle
- Add engine generator and prevent data generator from purging stale files
- Move BundleGenerator to data package and enhance functionality
- Update EngineLogAppender to use non deprecated constructor
- Add bundle registry system and event handling improvements
- Refactor BundleRegistry to BundleRegistryQuery, add more registries. Add All Config types in Config and make Config#STARTUP_SPEC be responsible for script and resource enabling / disabling.
- Update run configurations to enable ANSI terminal support
- Bump Version
- Add data mixin config and move DataGeneratorMixin to dedicated config
- Add ANSI terminal support in run configureEach, so it doesn't get reset
- Update dump command to use BuiltInRegistries Update dump command to require Admin Permissions.
- Dump command refactor look v1
- Update dump command output format to Markdown
- Add Builder System with implementations for Blocks and Item. Update TestBundle to use this ItemBuilder to create a test Item.
- Remove BundleGenerator and refactor resource pack loading

- Replace BundleGenerator with separate client and server generators
- Implement aggregate pack loading for generated and manual resources
- Add support for multiple bundle paths in BundlePackResources
- Update BundleFiles to include generated path
- Add new data providers for server-side generation
- Update test bundle with new items and models
- Add reload command to reload scripts and refactor BundleManager

- Add new ReloadCommand to reload scripts via command
- Extract reload logic from onResourceManagerReload to separate reload method
- Make BundleEntrypoint.onUnload non-final
- Register ReloadCommand in FoundryCommands
- Add Sub-Classes to Config for organization
- Remove unused method in BundlePackResources
- Change id and pack position in EngineRepositorySource
- Revert "Change id and pack position in EngineRepositorySource"

This reverts commit 3f272c60970b01a55b5f2a199184dea5aeabf9cc.
- Revert "Remove unused method in BundlePackResources"

This reverts commit 7c33dd653fc4264d09db778d220e4ad7d03c3d2f.
- Make BrowserPanel registration conditional on mcef mod presence and remove browser-related test buttons
- Update GroovyScriptEngine roots to include generated path Now i can generate scripts, and they're loaded as well.
- Make mcef compileOnly
- Change pack position and id in EngineRepositorySource
- Add Adapter utility class
- Add Priority enum
- Make the Bundle Scripts Secure via Imports- and SecureASTCustomizer
- Add game behavior system with direct world load support
- Remove TemporaryTarget and replace with TargetRef
- Refactor Bundle Management into a less centralized System.
- Change editor keybinding from F6 to F7
- Remove StagedPostProcessPipeline and integrate staging into PostProcessPipeline
- Update PostProcessPanel
- Add debug menu entries for Bundles and PostProcessing
- Fix bundle manager reload listener ID to use snake_case
- Refactor DirectWorldLoadBehavior to use logger and simplify lambda
- Update directory creation and add first-run util
- Add temp logo
- Move game behaviors to game/behavior pkg instead of just game
- Add Game Stage System.
- Being able to edit Server Files from client even with no operator status
- Same from before but make it more extreme
- Add more singleplayer checks.
- Make FileExplorerPanel same as all other singleplayer check messages.
- Refactor OpenGL shader packages to simplify structure
- Add game stage predicate system and clear stages command
- Update GameStageHandler registration log message
- Set DirectWorldLoadBehavior enabled to false by default
- Refactor post process pipelines to use abstract methods
- Try and fix font loading error stated in README as TODO
- Fix PostProcessStage doc reference
- Improve Shader source collection and add Caching.
- Add multiple consumers for easy usage (tri - penta)
- Update Client Debug stuff
- Add jfr profiler stuff
- Move docs to root directory and add VitePress configuration
- Remove package-lock.json + add to .gitignore
- Update index.md
- Update docs:dev script to include --host flag
- Add Dokka documentation generation and fix typo in task group
- Remove example pages and disable navigation links
- Add VitePress run configuration and update RenderDoc script path
- Clean up .gitignore
- Add documentation comments to debug classes
- Documentation + Cleanup
- Rename StageAdditionPredicate to StageAdditionCondition
- Replace Minecraft Pair usage with Apache Lang Pair
- Remove `forRemoval = true` from deprecated ImGui context classes
- Add stage addon system with built-in implementations
- Add GitHub Pages deployment workflow and update VitePress config
- Rename README.md to README.txt
- Revert package-lock.json from .gitignore and regenerate
- Add base path for GitHub Pages deployment
- Fix base path typo in VitePress config
- Update GitHub Pages deployment workflow to run only on wiki-related commits
- Revert "Rename README.md to README.txt"

This reverts commit d15bf8bfe40bece57ad89afe5a8266e956d2bcc4.
- Add test command implementation
- Add commonmark dependencies and update gradle.properties
- Add setScreen method to Client
- Add markdown parser and viewer implementation
- Update markdown viewer with scrollable layout and refactor parser
- Move FileExplorerPanel to editor subpackage and extend EditorPanel
- Refactor MainMenu to be more abstract.
- Rename Tb* and Fe* interfaces to Engine*
- Implement EngineDataGenerator interface and conditional purging logic
- Add ReloadCommandMixin and EngineReloadCommand interface
- Remove Ctrl+T shortcut from TestPanel and add empty shortcut method
- Move key-related classes to `client.util.key` package
- Mark post-processing classes as experimental
- Update gh workflows and add Javadoc to pages.
- Update index and deploy conditions
- Fix vitepress config
- Fix vitepress config not really
- Add Gradle wrapper scripts and update wrapper configuration
- Update javdoc workflow
- Try and fix actions
- I forgot to un-ignore the wrapper.
- Fix javadoc location
- Fix build-javadoc
- Update deploy.yml
- Update index.md
- Try fix config.mts to use correct javadoc link
- Restrict pages deploy and remove Gradle runner
- Update sourceLink remoteUrl to point to specific source directory
- Add API documentation and Getting Started guide
- Remove render doc file from git
- Refactor TestPostProcessPipeline to use overridden methods
- Remove obsolete run configurations and update RenderDoc script path
- Update Documentation to add basic explanations.
- Add easy register method to Item and Block Builder.
- Bump Version
- Update Example Bundle to use new registration api.
- Remove unused plugin from vitepress config
- Add Freezable Utility class.
- Update some Panel stuff Includes Javadoc typo updates and moving of packages
- Add ModPathBroadcaster so that dev envs can get all the files they need.
- Update bundle documentation.
- Add documentation for mod dependency setup in dev environment
- Update modPathDaemon to modPathListener in developer guide
- Update broadcaster port to 56656
- Update command registration to use CommandBuildContext
- Add EvalCommand for server-side Groovy script evaluation
- Add config option to enable/disable EvalCommand
- Update EvalCommand permission check to use configurable permission level
- Update version to 0.0.3!

Will from now on be correctly updated.
- Add Javadoc to permission helpers and change color of succes
- Refactor build configuration into modular Gradle files
- Run SonarQube analysis and fix most issues.

Went from 600+ to 209
- Idk what happened here, but its fixed now
- Refactor and improve code quality bump to 0.0.4
- Add first version of RecipeBuilder bump 0.0.5
- Refactor EngineRegistry.java to EngineRegistries.java and add RecipeBuilder usage.

bump 0.0.6
- Add caution notes to GameBehavior and related classes.
- Remove all the generated stuff from the ExampleBundle
- Change error logs to warnings in ModPathBroadcaster
- Add id (String) Util method to Bundle
- Remove old Data gen stuff in anticipation for Virtual Packs
- Add full Virtual Pack system. SHOULD be working
- Bump
- Remove old Datagen leftovers
- Remove unused FileManager class
- Add @NullMarkded to all packages.

bump 0.0.10
- Update tag-dispatch.yaml

fix typo. :publish
- Remove testing actions
- Update publish.yaml
- Update Client and Server run jvm args, update game run directory
- Update .gitignore
- Refactor client init, and fix server only crash
- Please work
- Update tasks.gradle
- Update game test server directory
- Add game test server bundle copy task
- Refactor builder package structure. bump .11
- Re-add RecipeBuilder functionality for Virtual Resource Packs. bump .12
- Update BuilderBase javadoc
- Update Minecraft to 26.1-pre3
- Refactor all builders to use api & impl system.

bump .14

- Remove unused "generated" path from BundleFiles bump 0.0.15
- Oops
- Refactor Markdown parsing system

add new MdScreen constructors change to just one Markdown visitor. supports both commonmark + gfm bump .16

- Add block and item model generation to virtual packs bump .17
- Fix virtual pack title
- Move zipFileSystem from Bundle to BundleFiles bump .18
- Add RegistryEvent API for cleaner registration inside of bundles.

bump .19
- Update package from io.github.* to de.*
- Mark internal API methods with @ApiStatus.Internal
- Ensure object is created before retrieval in BuilderBaseImpl
- Update Builder documentation
- Fix missing comma in config.mts
- Refactor config system into separate classes

bump .20

- Add DebugUtils class for game pausing useful for debugging?
- Move consumer util classes to dedicated package

bump .21
- Add testing particle
- Change particle spawns to just 1, and test color
- Create engine_particle.png
- Move mixin configs to dedicated mixins directory
- Refactor engine interfaces + mixins.
- Update builders and add EngineItem and EngineBlock for more functionality per block / item.

bump .22

- Remove unnecessary @SuppressWarnings annotation from DataComponentWrapper.wrap ()
- Add feature system, needs to be implemented for more than just the editor.
- Refactor DataComponentWrapper to accept input parameter directly
- Try fix the action hanging
- Lots of javadoc changes
- Move javadoc custom.css to new location and update reference
- Disable testing stuff from inside of mod. Should've been using examplebundle
- Update to 26.1 release. bump to .23
- Remove "templates" folder, and move neoforge.mods.toml and accesstransformer.cfg to just "resources"
- Add funny offhand rendering, togglable via config.
- Update Gradle wrapper to 9.4.0
- Add package-info.java files for recipe, item, and block builder packages
- Refactor `ItemBuilderImpl` callback handling and clean up redundant methods
- Add creative mode tab support for bundles
- Refactor `BundleCreativeModeTab` to use `Supplier` instead of `DeferredHolder`
- Refactor BundleCreativeModeTab to check for empty registries
- Add Config System for Bundles. Uses Toml for configs. May need to rework how you create the config, but its good enough for now. bump .24
- Fix typo in exceptions package name. Bump .25
- Add Javadoc comments to package-info.java files
- This check actually broke things so it shall be removed
- Refactor A lot of ImGui stuff. bump .26

New style system. Rename FeImGuiImplGlfw to EngineImGuiImplGlfw remove now unused ImGuiGraphics

Add themes:
BessDarkTheme CatpuccinMochaTheme CherryTheme DarkTheme ModernDarkTheme VeilTheme VidlibTheme
- Update themes to use Color class and fix import path in EngineLogAppender
- Add MinecraftToolsPanel for game mode, time, and metrics management
- Enable keyboard navigation and FreeType renderer in ImGuiManager
- Replace EngineImGuiImplGlfw with full ImGuiImplGlfw implementation bump .27
- Remove CodeEditor and replace with TextEditor extension

- Added `FileEndings` utility class for mapping file extensions to language definitions and icons
- Added `CodeEditorLanguageDefinitions` for syntax highlighting support in various languages (GLSL, JSON, TOML, Groovy, Java)
- Updated `TestPanel` to demonstrate the new `TextEditor` functionality
- Downgraded ImGui version from 1.90.0 to 1.88.0
- Bumped mod version to 0.0.28
- Removed now non-existent F13-F24 key mappings in `ImGuiImplGlfw`
- Try fix bug where imgui captures all input even if its disabled
- Update Code Editor + Lang Defs also update shortcuts for all panels. bump .29
- Move FileEndings and CodeEditorLanguageDefinitions to common package bump .30
- Fully rework exlorer stuff, now uses abstract base. Add texture viewer for both files and identifiers. Also improve code editor to have a forceReadOnly bump .31
- Make texture rendering use nearest neighbor sampling for pixel perfect rendering
- Refactor Menu System to use Categories bump .32
- Refactor panels to follow their categories
- Add weather selector to Minecraft tools panel
- Add custom networking system. Idk how good it is / how well it will work but its def easier to create packets than vanilla. bump .33
- Make MinecraftToolsPanel use new networking system.
- Fix crash on Server loading client classes.
- Add permission checks and update packets to use them.
- Clean up NetworkManager
- Add @Nullable annotation to MODBUS field in FoundryEngineMod
- Add more color constants and RGB constructor to Color util
- Add Custom UI System.
- Change some log levels and imgui theme packges
- Add time and weather lock buttons to Minecraft tools panel
- Add random stuff to TestPanel
- Wrap ImGui rendering calls in a try catch
- Full Documentation rework
- Refactor Example Bundle, And add documentation
- Add permission checks to editor panels
- Refactor NetworkManager and add Remote File editing bump .34
- Add icons to panels and refactor permission checks
- Update to use new Post Processing stages from RenderLevelStageEvent.
- Tried adding after gui post-processing, doesn't work.
- Update version ranges and bump to 0.0.35
- Remove feature management system
- Add simple formatting to panel and change manual formatting to this method.
- Refactor PanelCategory and update some Icons. Also rename EngineImGuiUtils.java to ImGuiUtils.java
- Add mod-publish-plugin and configure publishing settings

havent yet made it publish, only added basic config.
- Fix documentation links and update sidebar structure
- Move ConsolePanel to Tools category and add StopwatchPanel
- Update StopwatchPanel constructor to handle display name formatting
- Remove deprecated ImGui context management system
- Add InfoPanel to display system and mod information
- Update InfoPanel Category.
- Refactor Explorer things again.
- Improve resource explorer with server-side pack scanning and error handling
- Improve dump directory path handling using Path API
- Split config registration into separate methods for common, server, and startup configs
- Add dedicated server mod entrypoint
- Remove unused tickCount field
- Refactor particle system. Is not yet good enough.
- Update particle system 2
- Update Particles. I have to rework this a bit more, its not yet good enough
- Add particle color data and example particle builder
- Add particle position and velocity support to ParticleBuilder
- Refactor particle system with typed data modifiers and easing support
- Run Reformat Code
- Remove EngineParticleSpec and refactor particle system to use direct data lists
- Remove deprecated `data` method from ParticleBuilder API and implementation
- Add parent category support to PanelCategory and refactor CategoryMenuSection
- Add scene management system with entity tracking and editor panel
- Add scene zone filtering and player follow mode to SceneManager
- Group scene nodes by type and improve node rendering with collapsible categories
- Add CataloguePanel with drag-and-drop support for blocks and items
- Update TestPanel to use logging placeholders
- Add icon generation system. Needs more improvements
- Update Icons
- Add item icon rendering to Catalogue
- Remove all custom opengl / shader stuff. I will port Velvet soon and will be using that.
- Add entity, fluid, and tag tabs to CataloguePanel with custom icon providers
- Update ImGuiManager font reload to skip Linux platform TEMPORARY FIX
- Bump version, fix server, add a few issues to readme
- Change ScreenIconExporter to use Item Registry instead of Creative mod tabs
- Change Icon export Size to be a config value.
- Update CataloguePanel to support text overlays on icons and refactor tag rendering
- Add right-click actions to CataloguePanel grid items
- Update EntityMixin to use kill method on server level for entity removal
- Add ThemeSelectorPanel for dynamic theme switching
- Add package-info.java for code editor with NullMarked annotation
- Add EditorContext and centered helper, implement MainEditor UI for bundle creation
- Update ImGuiManager font reload to only load fonts on Windows
- Remove SQLite and ORMLite dependencies
- Make themes be saved in config.
- Update configs, update Log levels
- Update Particle things.
- Add BbModelReader dependency and version property
- Update Client command registration to be closer to Common Commands
- Remove BbModelReader dependency
- Add BlockEntityRendererMixin for configurable block entity render distance
- Remove unused LevelRenderer import and variable
- Update widget colors to use Color constants
- Add hand subcommand to generate_icons for exporting held item
- Update the Minecraft postChain system to allow for custom ones, and also multiple active at the same time.
- Update EffectManager to be instance-based and accessed via Client
- Move post-processing effects to dedicated EffectPanel still need to add dynamic shader stuffs.
- Make Effect Manager registry based
- Refactor EffectManager to just have one list of active effects tracked in the GameRenderer
- Make ReloadCommandMixin work. Now sends Done! after its done
- Add working bloom and broken depth visualization
- Add post-processing effect reloading functionality currently doesnt work.
- Update bloom threshold value to 0.7
- Update Panel to use a try-finally statement
- Add OBJ model rendering classes and parsing functionality This is not yet implemented correctly. it is just pasted from before. bump .37
- Add getter to EngineMinecraft to access perTickGizmos
- Run reformat code
- Remove per bundle event bus, turn into more kjs oriented event approach.
- Add error to readme
- Funny set camera entity thing.
- Add a build workflow
- Fix copy paste error in build.yaml
- Add path req to build task
- Update Builder System
- Fix missed update.
- Bump version 0.0.38
- Add TODO
- Remove test particles
- Bump version to 0.0.39
- Remove Threads, Update Events.
- Add bundle list entries to mod list screen, remove broken event
- Add RenderDoc support dependency and repository
- Rework mod page inject
- Fix Minimal and add bundle icons
- Update ExampleBundle
- Completely rework documentation
- Update documentation hero section and fix registries example
- Remove EditorContext for now. Add ability to bundles to declare Dependencies.
- Update wiki
- Add Blueprint System For ImGui. The main editor will use this at some point.
- Fix ImGui tree node flag import
- Add no-op screen for editor, open it when crtl is held + editor is toggled via keybind
- Update README and GameRendererMixin
- Add SoundBuilder with impl
- Refactor sound system to support complex sound definitions
- Move BlueprintsPanel. and Fix bug where loading a saved one will not reset the ID
- Fix bug in node editor, where large selection of nodes doesnt delete all of them
- Rework built in nodes, Make Search menu autofocus
- Fix ImGui tree node flag logic in BlueprintsPanel
- Remove redundant System.out.println from Blueprint Print node
- Move event listeners from client and server classes to main mod class
- Test some of the events
- Make Blueprints Tick Event work
- Make the Minimap in Blueprints slightly bigger
- Refactor blueprint stuff. Add simple Event execution.
- Refactor script loading to be environment specific
- Add missing getter for Bundle
- Update Bundle unloading to use deticated method inside of Bundle
- Add server data and configure game directory in neoforge.gradle
- Update docs, add entrypoint and sides concepts, and bump version to 0.0.43
- Refactor Scene Management
- Fix SelectionManager
- Fix SelectionManager
- Fix PropertiesPanel category
- Update ScenePanel and remove PropertiesPanel
- Fix ScenePanel#iconForType to use corrected return type
- Refactor InfoPanel
- Add recipe tab to CataloguePanel and refactor Client.getMinecraft () to Client.getMc ()
- Add engine display entities and renderers
- Update display entity hitbox calculation and tracking
- Implement Attackable and Targeting interfaces for display entities
- Refactor script engine system to support multiple languages
- Refactor BundleScriptEngineRegistry to be managed by Common class
- Revert the Global BundleScriptEngineRegistry, make bundle specific.
- Add Spark dependency and Modrinth repository
- Update TODO
- Add Bezier things
- Add cutscene and screen effect systems
- Improve catalogue panel drag-and-drop functionality and test panel display
- Update catalogue panel and test panel to use ImGuiUtils.Image for texture handling
- Update the engine displays to use commands as callbacks, which are serializable. Update the packages aswell. Update AT to add all getters + organize it
- Update TODO.md
- Add BBModel data classes and reader
- Update eval command to support player interaction and improve output handling
- Remove all Raw opengl other than ImGuiImpl
- Update cutscene editor item hover text
- Replace `EngineRepositorySource` and `BundlePackResources` with `DynamicPackRepository` for unified and extensible resource pack handling.
- Make getBundles return a Collection instead of Iterable.
- Unify temp directory path to Common
- Update docs (pray)
- Comment out `BundleDataGenerator.runAll()` during bundle loading
- Dont fail on javadoc error
- Implement some of Pies feedback on GameRendererMixin
- Remove Javadoc from documentation
- Make Client Logger public.
- Remove Cutscene input handling from mixin due to concerns mentioned by Pie
- Refactor render distance calculation logic to be done on config reload, to save method calls
- Fix nullpointer by calculating effective render distance on config reload
- Update blueprint system to be client common aware. Also run reformat code so a lot of stuff. Implement some more of Pies suggestions.
- [NOT IMPLEMENTED] Data generators, replacement for vpacks at some point.
- Remove anonymous class from PackResourcesSupplier
- Update Blueprint system.
- Add client server bundles hashing and checking.
- Refactor event posting logic, grouping internal methods into nested Internal classes.
- Add Blueprints documentation and link it in the concepts index.
- Update dependencies and version
- Add a CommandEvents class for registering Commands in bundles.
- Revert switch to mc render pass, it broke some stuff, have to figure out a better way
- Improve TextureViewerPanel
- Big cutscene System overhaul.
- Make CutsceneCommand work with default cutscene values.
- Make windows movable from not only the title bar. Dont know if this will stay, i just like it from trackmania plugin thing, so it will exist here for now as well.
- Big Blueprint system update.
- Forgot to commit these
- Make CutscenePanel use default / saved values instead of hardcoded ones.
- Add registry event context and undo/redo support to blueprint editor
- Remove unused TestClass file
- Remove BlueprintEventBridge and improve BlueprintEngine some more.
- Add level storage source mixin to allow for additional save paths.

Still need to implement the bundle save paths to be auto added.
- Make network payload registration optional
- Rename entity creation method to `createDisplay` for clarity
- Move server tick handling from FoundryEngineModServer to FoundryEngineMod
- Refactor screen effect durations to use normalized values
- Update Timeline panel some more, improve usage
- Try to achieve in some parts KubeJs event parity.
- Forgot to commit this
- Add missing event categories
- Add JEI dependency
- Bump version, update wiki index
- Update TODO.md
- Merge remote-tracking branch 'origin/master'
- Update BlueprintGraph and add Tests for multiple things.
- Refactor cutscene effects with "attachments".
- Split commands from screen effects.
- Update Mixin compatability to 25
- Add inifile to common, update imgui ini filename and add imgui log filename
- Remove deprecation from CutsceneTimelinePanel since the mention doesnt exist yet.
- Add INI file support with IniFile and IniSection classes
- Fix file extensions in ImGuiManager
- Add IniFileManager and update the rest of the ini system.
- Update todo formatting.
- Update icon?
- Idk if it works but vulkan backend ? need to test once 26.2 comes out / once neoforge has patches
- Refactor scene system
- Fix error.
- Add funny ASCII to startup
- Add Area System
- Add area testing
- Add Qodana configuration and fix some of the issues it found
- Remove unused import for ServerLevel
- Change network handling in TestPanel
- Add chat icons and font resources
- Update ImGuiManager with theme initialization and import cleanup
- Fix some null stuff in Area System
- Add runtime level support.
- Update issue templates
- Merge remote-tracking branch 'origin/master'
- Update Issue template for feature
- Move some packages around
- Make LevelStorageSourceMixin less harsh
- Remove broken BlockEntityRendererMixin overwrite
- Fix `onGameRuleChanged` method signature in `MinecraftServerMixin`
- Remove GameBehavior system and replace with TitleScreenModifyEvent
- Update EngineImGui with more methods, make client return EngineImGui instead of ImGuiManager.

I think i figured out font issues. It might be the glyph ranges
- Im an idiot, forgor this
- Add ImGuiFontManager, Update ImGuiManager, remove most Icons bump version
- Update Gradle wrapper to 9.5.0
- Update font stuff some more
- Remove NodeBuilder and add event nodes system
- Update font handling and config options

currently, minimal is a bit weird, but diabled should work
- Im actually done with Fonts
- Add BlueprintContexts to event group holders
- Add Minecraft-specific pin types and auto-derive colors
- Add control flow and loop nodes to blueprint system
- Update Java version to 25 in workflows
- Holy Node editor
- Improve node editor styling and context menu positioning
- Remove bugged node outline
- Add vector nodes and refactor position handling
- Simple waypoint system.
- Change some formatting
- Idk what i even did. rendering basically. Also waypoints
- Implement Unified SavedDataManager
- Update Waypoint System.
- Some task is messing up the shader files.
- Remove old Waypoint files, update renderer
- Update README
- Move editor tools to TOOLS category
- Remove scene system, change locations of some panels
- Add area editing and status message system
- Replace area position drag inputs with direct input fields
- Add WaypointPanel for waypoint management and update ChatIcons
- Big Cutscene Area and Waypoint update
- Clean slate the Node system.
- Panel Updates, move packages and other things
- Forgot this
- ImGui Update
- Refactor Editor item to be a general pourpose editor. area system now uses it aswell.
- Update some things
- Move Packets and update saved data
- Refactor test command structure
- Remove position_color_tex_lightmap shader and related pipeline update position only pipeline
- Remove virtual resource pack system Switch to default minecraft data generation
- Update mod version and fix code block line numbers in docs
- Fix typo in docs
- Move Commands around. Add Server class, add and run task to generate package-info.java files
- Fix Event system
- Fixed the Fix for the event system
- Remove BuilderState and inline its functionality into builders
- Bump version
- Refactor block entity render distance and add camera depth handling
- Refactor test command structure and add fake entity spawning
- Remove unused utility classes and disable suzanne
- Remove unnecessary dep
- Improve error handling and reporting for script engine and bundle loading
- Try fix IllegalStateException in registry when erroring
- Improve script error handling by broadcasting error
- Another script error update
- Change AreaEvents formatting
- Refactor data providers to use Bundle instead of bundleId
- Refactor DynamicPackRepository to use CompositePackResources and add fixed position support
- Add MODRINTH.md with project overview and feature list
- Refactor Data generation system to use builders instead of registry
- Add data cache clearing, update config to remove unused, bump .55
- Move BundleDataGenerator.runAll () outside of lambda
- Remove Composite Pack resources, i think it was causing crashes.
- Expose data generator to bundles via event.
- Add block and item modification events with mixin support
- Fix issue with data generators
- Rework some bundle stuff, implement save folders per bundle
- Data Generation update
- Change pack ids and add description
- Add world instancing system
- Add HolderLookup.Provider support to BundleDataGenEvent
- Add custom event support to BundleEvents
- Update mod version to 0.0.64
- Update Docs
- Make it so that i can hotswap mixins and other jvm stuff

uses the intellij jrr
- Holy documentation update
- Replace int color with Color class in lots of files
- Remove BBModel data classes and related utilities
- Fix logo in docs
- Add per-anchor hold ticks support to cutscenes
- Improve documentation formatting and clarity
- Add StageEvents api
- Fix Block and Item Modification
- Add Recipe Editor Panel
- Some refactoring
- Add game session management system
- All the things, but also fixed?
- Run Reformat Code
- Refactor Panel Code
- Rewrite blueprint system
- More Blueprint work
- Update gitignore
- Add mesh rendering pipeline and depth capture system
- Remove MeshPipeline class
- Add game session docs
- Remove blueprint editor and related classes this is due to me wanting to fully rework it into something more capable and change the library its using.
- Bare minimum node editor
- Remove api package
- Update some stuff.
- Add workflow_dispatch trigger to build workflows
- Refactor GitHub Actions workflows and add changelog generation [skip ci]
- I hate ci
- Add some utils to post processing and ids
- *(gradle)* Add sources jar to publication
- Make it so that i can hotswap mixins and other jvm stuff

uses the intellij jrr
- Holy documentation update
- Replace int color with Color class in lots of files
- Remove BBModel data classes and related utilities
- Fix logo in docs
- Add per-anchor hold ticks support to cutscenes
- Improve documentation formatting and clarity
- Add StageEvents api
- Fix Block and Item Modification
- Add Recipe Editor Panel
- Some refactoring
- Add game session management system
- All the things, but also fixed?
- Run Reformat Code
- Refactor Panel Code
- Rewrite blueprint system
- More Blueprint work
- Update gitignore
- Add mesh rendering pipeline and depth capture system
- Remove MeshPipeline class
- Add game session docs
- Remove blueprint editor and related classes this is due to me wanting to fully rework it into something more capable and change the library its using.
- Bare minimum node editor
- Remove api package
- Update some stuff.
- Add workflow_dispatch trigger to build workflows

### Refactor

- Refactor EngineModelProvider to be custom. Reload Resources after generating data.
- *(events)* TitleScreenModifyEvent to TitleScreenModificationEvent and move to modification package
- Self-registering event clear pattern
- *(area)* Full rework of area system based on modules.
- *(area)* Rework area rendering and implement area rendering module
- *(server/client)* Make foundryengine work server only, and client only
- *(config)* Simplify config structure by removing EngineConfig abstraction
- *(post)* Rewrite post-processing system
- *(shaders)* Extract skybox logic into reusable include file users can override
- *(items)* Remove editor item and related assets
- *(imgui)* Remove deprecated OpenGL backend, replace with mc rendering. update texture drawing system, add component drawing
- *(code + javadoc)* Update some code and javadoc
- *(dialogue)* Move editor panel and adjust text input size
- *(dialogue)* Update some stuff
- *(key)* Remove key binding system and move shortcut class
- Run reformat code
- *(savedata)* Rework saved data.
- *(icons)* Replace ScreenIconExporter with IconExporterLayer and switch to off-screen rendering
- *(icons)* Make icon exporting just command and icons in imgui render into offscreen buffer dynamically
- *(icons)* Optimize icon cache handling with pending keys
- *(node)* Make x button be an icon and add padding
- *(savedata)* Rename GAME_DATA to ENGINE_DATA, compress I/O
- *(imgui)* Big ImGui update.
- *(imgui)* Update ImGui to version 1.92.0
- *(imgui)* Centralize context mgmt via ImGuiContextStack
- *(editor)* Remove RegisterPanelEvent and its documentation
- *(docs)* Update documentation
- *(registry)* Replace RegistryEvent with RegistryCollector for easier management
- Replace hardcoded strings with translatable components in various places
- Remove unused waypoint keybinds, fix key category translation key
- *(editor)* Rework texture viewer panel and fix dynmic texture leak in ImGraphicsExtractor
- *(node)* Improve pin list handling in Node and NodeEditorInstance
- *(bundle)* Rework registering and recipes
- *(builder)* Rework recipe builder to delegate to mc builders.
- Refactor (icon): update Icons to be normal names. no more differentiation between FAE and FA
- *(explorer)* Merge File and Resource explorer into one.
- *(network)* Remove TestPacket and streamline action handling
- *(game)* Rework game session management to be per world and auto start.
- *(stage)* Rework stage system
- *(editor)* Remove unused MainEditor panel.
- Refactor (script): rework scripting system to be easier to work with and finally fix eval command to use actual groovy shell. for now removes the extensibility of other languages.

### Documentation

- Docs fix api index dead link
- Big documentation update on multiple fronts
- Add images

### Testing

- Test some actions

:publish
- Add unit tests for a lot of stuff.
- Test deepwiki thingy

### Miscellaneous Tasks

- Bump version to 0.0.66 [skip ci]
- Fix bundle scripts, update docs
- Add ai stuff
- Update AGENTS.md
- Bump version to 0.0.67 [skip ci]
- Go back to old ci, add publish task which gets run on tag
- Bump version
- *(deps)* Update neoforge version to 26.1.2.76
- Allow manual trigger for publish workflow
- Updates before release
- *(editorconfig)* Add .editorconfig file
- Remove RegisterRenderingStuffEvent
- *(qodana)* Update configuration for Qodana analysis
- *(dependencies)* Update mod version to 0.1.2 and add and comment out sodium and iris
- *(ci)* Update action versions in workflow files to latest stable releases
- *(reformat)* Run reformat code
- *(reformat)* Run Code Cleanup
- *(package)* Add package info files
- *(info)* Add missing package info file
- *(gradle)* Bump version to 0.1.3
- *(nullability)* Make signature nullable in AutocompleteItem
- Bump version to 0.0.66 [skip ci]

### Revert

- Revert last, due to missing braincells from my part.
- Revert since it somehow broke? i dont even understand why
