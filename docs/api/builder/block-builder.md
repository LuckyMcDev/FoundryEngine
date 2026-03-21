# Block Builder

The `BlockBuilder` is a utility class designed to streamline the creation of Blocks using a fluent builder pattern.

## Usage

To start, create a new instance using a unique `Identifier`.

```java{3}
import de.luckymcdev.foundryengine.api.builder.item.BlockBuilder;

BlockBuilder block = BlockBuilder.create(Identifier.fromNamespaceAndPath("bundleid", "example_block"));
```

## Properties

The `BlockBuilder` manages both the physical world properties of the block and its corresponding item representation.

### Block Properties

Define physical attributes like blast resistance or mining speed.

```java{3}
block.properties(properties -> {
    
    properties.destroyTime(3.0);

});
```

### Item Properties

Since every block has an associated `Item`, you can configure item-specific behavior (like max stack size) directly on
the block builder.

```java{3}
block.itemProperties(properties -> {
    
    properties.stacksTo(16);

});
```

## Registration

To register your blocks, listen for the RegistryEvent on the bundle bus.

```java{5,9}
public static void onRegister(RegistryEvent event) {

    // Single registration

    event.items(item);

    // Bulk registration

    event.items(item1, item2, item3);

}
````

::: tip Organization
You can call event.blocks() multiple times or pass an infinite number of arguments in a single call. Choose the style
that best fits your project's organization.
:::