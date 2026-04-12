package de.luckymcdev.foundryengine.common.vpacks;

import de.luckymcdev.foundryengine.common.vpacks.json.JLang;
import de.luckymcdev.foundryengine.common.vpacks.json.JSounds;
import de.luckymcdev.foundryengine.common.vpacks.json.JTag;
import de.luckymcdev.foundryengine.common.vpacks.json.advancement.JAdvancement;
import de.luckymcdev.foundryengine.common.vpacks.json.animation.JAnimation;
import de.luckymcdev.foundryengine.common.vpacks.json.loot.JLootTable;
import de.luckymcdev.foundryengine.common.vpacks.json.model.JModel;
import de.luckymcdev.foundryengine.common.vpacks.json.recipe.AbstractJRecipe;
import de.luckymcdev.foundryengine.common.vpacks.json.state.JState;
import de.luckymcdev.foundryengine.server.packs.VirtualPackImpl;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.world.item.component.ItemAttributeModifiers;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.function.BiFunction;
import java.util.function.IntUnaryOperator;
import java.util.zip.ZipInputStream;

public interface VirtualResourcePack extends PackResources {
    /**
     * Creates a runtime resource pack.
     *
     * @param resourceLocation The ID for the resource pack.
     * @return {@link VirtualResourcePack}
     */
    static VirtualResourcePack create(Identifier resourceLocation) {
        return new VirtualPackImpl(resourceLocation);
    }

    /**
     * Clones and recolors the texture given by the input stream, and puts the
     * creates the image at the given location.
     *
     * @param resourceLocation The location where the new image will be put.
     * @param inputStream      The input stream of the original texture
     * @param pixel            The pixel recolorer.
     */
    void addRecoloredImage(Identifier resourceLocation, InputStream inputStream, IntUnaryOperator pixel);

    /**
     * Adds a language file for the given language.
     * <br />
     * Calling this method multiple times for the same language will override
     * previous calls. Use {@link VirtualResourcePack#mergeLang} if you need to call the method
     * multiple times.
     *
     * @param resourceLocation The namespace of your mod and the path of the language.
     * @param lang             The translation information.
     * @return TODO: Add return information.
     */
    byte[] addLang(Identifier resourceLocation, JLang lang);

    /**
     * Adds a language file for the given language.
     * <br />
     * Calling this method multiple times for the same language will merge the
     * data into one file.
     *
     * @param resourceLocation The namespace of your mod and the path of the language.
     * @param lang             The translation information.
     */
    void mergeLang(Identifier resourceLocation, JLang lang);

    /**
     * Adds a sounds.json file for the given namespace.
     * <br />
     * The sounds.json is placed at assets/{namespace}/sounds.json as per
     * Minecraft's resource pack specification.
     * <br />
     * Calling this method multiple times for the same namespace will override
     * previous calls.
     *
     * @param namespace The namespace to register sounds under
     * @param sounds    The sounds' data.
     * @return The serialized sounds.json bytes.
     */
    byte[] addSounds(String namespace, JSounds sounds);

    /**
     * Adds a loot table.
     *
     * @param resourceLocation The path to the loot table.
     * @param lootTable        The loot table data.
     * @return TODO: Add return information.
     */
    byte[] addLootTable(Identifier resourceLocation, JLootTable lootTable);

    byte[] addAdvancement(Identifier resourceLocation, JAdvancement advancement);

    /**
     * Adds a resource that is lazily evaluated.
     *
     * @param packType Whether the resource is an asset or data.
     * @param path     The path to the resource.
     * @param data     A function that is evaluated at a later time.
     */
    void addLazyResource(PackType packType, Identifier path, BiFunction<VirtualResourcePack, Identifier, byte[]> data);

    /**
     * Adds a raw resource.
     *
     * @param packType Whether the resource is an asset or data.
     * @param path     The path to the resource
     * @param data     The resource data
     * @return TODO: Add return information.
     */
    byte[] addResource(PackType packType, Identifier path, byte[] data);

    /**
     * Adds a raw resource to the pack's root path.
     *
     * @param path The path to the resource.
     * @param data The resource data
     * @return TODO: Add return information
     */
    byte[] addRootResource(String path, byte[] data);

    /**
     * Adds an asset, which should be clientside.
     *
     * @param path The path to the resource
     * @param data The asset data
     * @return TODO: Add return information.
     */
    byte[] addAsset(Identifier path, byte[] data);

    /**
     * Adds a data resource, which should be serverside.
     *
     * @param path The path to the resource.
     * @param data The data's data
     * @return TODO: Add return information.
     */
    byte[] addData(Identifier path, byte[] data);

    /**
     * Adds a model. Items should go into item/... and blocks should go in block/...
     * <br />
     * ".json" is automatically added to the path.
     *
     * @param path  The path to the model.
     * @param model The model data.
     * @return TODO: Add return information.
     */
    byte[] addModel(Identifier path, JModel model);

    /**
     * Adds a blockstate file.
     *
     * @param path  The path to the blockstate file.
     * @param state The blockstate data.
     * @return TODO: Add return information.
     */
    byte[] addBlockSate(Identifier path, JState state);

    /**
     * Adds a texture file.
     * <br />
     * ".png" is automatically added to the path.
     *
     * @param path  The path to the texture.
     * @param image The image
     * @return TODO: Add return information.
     */
    byte[] addTexture(Identifier path, BufferedImage image);

    /**
     * Adds an animation metadata file for a texture.
     * <br />
     * ".png.mcmeta" is already added to the path.
     *
     * @param path      The path to the texture.
     * @param animation The animation data.
     * @return TODO: Add return information.
     */
    byte[] addAnimation(Identifier path, JAnimation animation);

    /**
     * Adds a tag.
     * <br />
     * ".json" is automatically added to the path.
     *
     * @param path The path to the tag.
     * @param tag  The tag data.
     * @return TODO: Add return information.
     */
    byte[] addTag(Identifier path, JTag tag);

    /**
     * Adds a recipe.
     * <br />
     * ".json" is automatically added to the path.
     *
     * @param path   The location of the recipe that also represents its directory.
     * @param recipe The recipe data.
     * @return TODO: Add return information.
     */
    byte[] addRecipe(Identifier path, AbstractJRecipe recipe);

    /**
     * Adds an item modifier from an {@link ItemAttributeModifiers}
     * <br />
     * ".json" is automatically added to the path.
     *
     * @param path      The location of the item modifier
     * @param modifiers The {@link ItemAttributeModifiers} that is serialized
     *                  JSON.
     * @return TODO: Add return information.
     * @see #addItemModifier(Identifier, ItemAttributeModifiers.Builder)
     */
    byte[] addItemModifier(Identifier path, ItemAttributeModifiers modifiers);

    /**
     * Adds an item modifier from an {@link ItemAttributeModifiers.Builder}.
     * This method calls
     * {@link #addItemModifier(Identifier, ItemAttributeModifiers)} after
     * calling {@link ItemAttributeModifiers.Builder#build()} on the provided
     * builder.
     * <br />
     * ".json" is automatically added to the path.
     *
     * @param path      The location of the item modifier
     * @param modifiers The {@link ItemAttributeModifiers.Builder} that builds
     *                  a {@link ItemAttributeModifiers} which is then passed
     *                  to {@link VirtualResourcePack ::addItemModifier}
     * @return TODO: Add return information.
     * @see #addItemModifier(Identifier, ItemAttributeModifiers)
     */
    byte[] addItemModifier(Identifier path, ItemAttributeModifiers.Builder modifiers);

    /**
     * Adds an item definition.
     * <br />
     * ".json" is automatically added to the path.
     *
     * @param itemId         the id of the item to define
     * @param modelReference the reference to the items model
     * @return TODO: Add return information
     */
    byte[] addItemDefinition(Identifier itemId, String modelReference);

    void load(Path path) throws IOException;

    void load(ZipInputStream zipStream) throws IOException;
}