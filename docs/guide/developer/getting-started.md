# Getting Started

This guide will help you get started with Foundry Engine.
It will cover the basic steps to set up your development environment and create your first Bundle.
It will teach you how to then build that bundle and distribute it to others.

For more detailed information about specific Parts of the API, please refer to the api documentation.

## Setting Up your Development Environment.

First, clone or download the Bundle Template from GitHub.

```bash
git clone https://github.com/LuckyMcDev/ExampleBundle.git
```

This contains a basic setup for how to write your first Bundle,
and is a great starting point for learning how to use Foundry Engine.

## Building your Bundle

To build your Bundle, in your custom Bundles directory run the following command:

```bash
gradlew buildBundle
```

Or run this gradle task:

```bash
buildBundle
```

That should create a new folder in your ./build/bundles/ directory with the name of your Bundle and version number.

## Getting all your mod dependencies to show up.

To load all the mods that you are using in your development environment, you need to run this Gradle task:

```bash
gradlew modPathDaemon
```

This will start a daemon that will run in the background.

Next, start your minecraft instance with all the mods you need in your mods' folder.
It also needs FoundryEngine installed.

Once you have started your instance, you can close it again and in your development environment, you should have seen a
message in the console that says:
`[Daemon] Received mod path:` followed by the path to your mods' directory.

Now, you need to run this task:

```bash
gradlew build
```

to refresh your dependencies, and you should have autocompletion and all your mod dependencies should be available in
your development environment.

If you keep using the same Minecraft instance, you shouldn't have to run the daemon again, it should automatically
update
when you run the build task again. Just make sure to run this if you change your mods or add new ones.

## Distributing your Bundle

You can distribute your Bundle by sharing the Folder that was created in
the [Building your Bundle](#building-your-bundle) step.
Sharing .zip or .tar.gz will be supported in future updates, but for now you can just share the folder as is.