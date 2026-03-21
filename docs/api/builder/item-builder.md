# Item Builder

The `ItemBuilder` is a utility class used to create standalone Items (such as tools or resources) using a builder
pattern.

## Usage

Initialize the builder by providing a unique `Identifier`.

```java{3}
import de.luckymcdev.foundryengine.api.builder.item.ItemBuilder;

ItemBuilder item = ItemBuilder.create(Identifier.fromNamespaceAndPath("modid", "example_item"));

```

## Properties

You can modify the item's behavior by passing a consumer to the properties method.

```java{3}
item.properties(properties -> {

    properties.stacksTo(16);

});
```

::: info Reference

For a complete list of available methods within the properties consumer, refer to the Minecraft Item.Properties source.

:::

## Registration

To register your items, listen for the RegistryEvent on the bundle bus.

```java{5,9}
public static void onRegister(RegistryEvent event) {

    // Single registration

    event.items(item);

    // Bulk registration

    event.items(item1, item2, item3);

}
````

::: tip Organization
You can call event.items() multiple times or pass an infinite number of arguments in a single call. Choose the style
that best fits your project's organization.
:::