# Getting Started

This guide will help you get started with Foundry Engine.
It will cover the basic steps to set up your development environment and create your first Bundle.
It will teach you how to then build that bundle and distribute it to others.

For more detailed information about specific Parts of the API, please refer to the api documentation.

## Setting Up your Development Environment.

First, clone or download the Bundle Template from GitHub.

```bash
git clone https://github.com/LuckyMcDev/FoundryEngineBundleTemplate.git
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

That should create a new folder in your ./build/libs/ directory with the name of your Bundle and version number.

## Distributing your Bundle

You can distribute your Bundle by sharing the Folder that was created in the previous step.
Sharing .zip or .tar.gz will be supported in future updates, but for now you can just share the folder as is.