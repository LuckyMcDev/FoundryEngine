# Getting Started

This guide will help you get started with Foundry Engine.
It will cover the basic steps to set up your development environment and create your first Bundle.
It will teach you how to then build that bundle and distribute it to others.

For more detailed information about specific Parts of the API, please refer to the api documentation.

I recommend for you to read the Apache groovy documentation to get familiar with the language,
as it is the main language used for writing Bundles. You can find the documentation
here: https://groovy-lang.org/documentation.html
Specifically, The [Syntax](https://groovy-lang.org/syntax.html) section.
If you've never programmed before, It can be beneficial for you to start with a Java tutorial, as Groovy is very similar
to Java,
and it will help you understand the basics of programming.
You can find a good Java tutorial here: https://www.w3schools.com/java/, or if you prefer video tutorials,
you can go here: https://youtu.be/G1ifRRtJm7w?si=EHP7H9-Ks43VDBkw
Although it is 2 years old, it's still pretty good.

Next, Foundry Engine is built on top of Minecraft Neoforge, currently version
26.1.0.1-beta & Minecraft 26.1.
You should maybe read through the NeoForge documentation, although it is not required,
since Foundry Engine changes some of the core concepts.

A good source of what is possible with Foundry Engine is the Example Bundle which you can find on GitHub.

There are 2 Main ways to get started and writing your own Bundle.
One is quite fast but requires you to do a lot of things manually,
The other one requires some setup but will make your life much easier in the long run,
and is the recommended way to get started.

---

## The Fast Way

If you want to get started right away, follow these steps:

### 1. Create a Bundle Folder

Create a new folder in your `.minecraft/FoundryEngine/bundles/` directory with the name of your bundle.

### 2. Configure the Bundle

Inside the bundle folder, create a `yourbundlename.bundle.toml` file and add the following content:

```toml
[[bundles]]
bundleId = "yourbundlename"
version = "1.0.0"
displayName = "Your Bundle Name"
displayURL = "https://github.com/YourName/YourBundleName"
authors = "You"
description = '''This is a Description'''
```

Replace all the fields with your own information.

### 3. Create the Entrypoint

Create a folder named whatever you want inside the bundles directory. Inside this folder, place a
`MainEntrypoint.groovy` file with the following content:

```groovy
package thefolderyoucreated

import de.luckymcdev.foundryengine.common.bundle.config.BundleConfig
import de.luckymcdev.foundryengine.common.script.BundleEntrypoint
import net.neoforged.bus.api.IEventBus

class MainEntrypoint extends BundleEntrypoint {

    MainEntrypoint(IEventBus bundleBus, IEventBus eventBus, BundleConfig bundleConfig) {
        super(bundleBus, eventBus, bundleConfig)
    }

    @Override
    void onLoad() {

    }

    @Override
    void onUnload() {

    }
}
```

### 4. Start Writing Your Bundle

That's all! You can now start writing your bundle code.

### 5. Distribute Your Bundle

To distribute your bundle, simply share the folder you created.

---

## The Recommended Way

### 1. Set Up Your Development Environment

Before you even start writing your bundle, You should set up a development environment.
This will make your Life much easier in the long run.
I recommend using [IntelliJ IDEA](https://www.jetbrains.com/de-de/idea/), as it has great support for Gradle and Groovy,
but you can use any IDE you want.

Now, clone or download the Bundle Template from GitHub:

```shell script
git clone https://github.com/LuckyMcDev/ExampleBundle.git
```

This template provides a basic setup for writing your first Bundle and is a great starting point for learning how to use
Foundry Engine.

### 2. Build Your Bundle

To build your Bundle, navigate to your custom Bundles directory and run the following command:

```shell script
gradlew buildBundle
```

Alternatively, you can run this Gradle task:

```shell script
buildBundle
```

This will create a new folder in your `./build/bundles/` directory with the name of your Bundle and its version number.

### 3. Load Mod Dependencies

To load all the mods used in your development environment, run this Gradle task:

```shell script
gradlew modPathListener
```

This will start a listener that runs in the background.

Next, start your Minecraft instance with all the required mods in your mods' folder. Ensure FoundryEngine is installed.

Once you have started your instance, you can close it again. In your development environment, you should see a message
in the console that says:
`Received mod path:` followed by the path to your mods' directory.

Now, run this task to refresh your dependencies:

```shell script
gradlew build
```

This will enable autocompletion and make all your mod dependencies available in your development environment.

If you continue using the same Minecraft instance, you won't need to run the listener again. It should automatically
update when you run the build task again. Just make sure to run this if you change your mods or add new ones.

### 4. Distribute Your Bundle

You can distribute your Bundle by sharing the folder created in the [Building your Bundle](#building-your-bundle) step.
Support for sharing `.zip` or `.tar.gz` files will be added in future updates. For now, you can share the folder as is.

---