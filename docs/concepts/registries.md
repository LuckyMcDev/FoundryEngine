# Concepts: Registries

A Registry is a way of getting the game to know what you have made.
Foundry Engine wraps the Neoforge Registry System with Builders.
These are classes, which make it easier to do custom logic.

You register these builders and their contents (Like eg. Items / Blocks)
in the `BundleEvents.registry {}` method.
Here you can call an assortment of methods, to register your builders.
You do not register the raw Object accessible in builders via the `#get()`
method, instead you register the full Builder Object.
So something Like this:

```groovy

ItemBuilder builder = ItemBuilder.create(...)

BundleEvents.registry {
    it.items(builder)
}
```

That's it!