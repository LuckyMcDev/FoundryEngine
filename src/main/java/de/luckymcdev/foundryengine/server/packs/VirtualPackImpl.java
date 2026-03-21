package de.luckymcdev.foundryengine.server.packs;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import de.luckymcdev.foundryengine.common.exeptions.EngineException;
import de.luckymcdev.foundryengine.common.util.CountingInputStream;
import de.luckymcdev.foundryengine.common.util.UnsafeByteArrayOutputStream;
import de.luckymcdev.foundryengine.common.vpacks.VirtualResourcePack;
import de.luckymcdev.foundryengine.common.vpacks.json.JCondition;
import de.luckymcdev.foundryengine.common.vpacks.json.JLang;
import de.luckymcdev.foundryengine.common.vpacks.json.JTag;
import de.luckymcdev.foundryengine.common.vpacks.json.advancement.JAdvancement;
import de.luckymcdev.foundryengine.common.vpacks.json.animation.JAnimation;
import de.luckymcdev.foundryengine.common.vpacks.json.loot.JLootTable;
import de.luckymcdev.foundryengine.common.vpacks.json.loot.JPool;
import de.luckymcdev.foundryengine.common.vpacks.json.model.JModel;
import de.luckymcdev.foundryengine.common.vpacks.json.model.JTextures;
import de.luckymcdev.foundryengine.common.vpacks.json.recipe.AbstractJRecipe;
import de.luckymcdev.foundryengine.common.vpacks.json.recipe.JIngredient;
import de.luckymcdev.foundryengine.common.vpacks.json.recipe.banner.JBannerPatternType;
import de.luckymcdev.foundryengine.common.vpacks.json.recipe.component.*;
import de.luckymcdev.foundryengine.common.vpacks.json.recipe.component.consumable.JSound;
import de.luckymcdev.foundryengine.common.vpacks.json.serializers.*;
import de.luckymcdev.foundryengine.common.vpacks.json.state.JMultipart;
import de.luckymcdev.foundryengine.common.vpacks.json.state.JState;
import de.luckymcdev.foundryengine.common.vpacks.json.state.JWhen;
import de.luckymcdev.foundryengine.config.CommonConfig;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.DisplayInfo;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.MetadataSectionType;
import net.minecraft.server.packs.metadata.pack.PackFormat;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.server.packs.resources.IoSupplier;
import net.minecraft.server.packs.resources.ResourceMetadata;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.BiFunction;
import java.util.function.IntUnaryOperator;
import java.util.function.Supplier;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class VirtualPackImpl implements VirtualResourcePack {
    public static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(AdvancementRewards.class, new AdvancementRewardsSerializer())
            .registerTypeAdapter(Component.class, new CodecSerializer<>(ComponentSerialization.CODEC))
            .registerTypeAdapter(Criterion.class, new CriterionSerializer())
            .registerTypeAdapter(DisplayInfo.class, new DisplayInfoSerializer())
            .registerTypeAdapter(JAnimation.class, new JAnimation.Serializer())
            .registerTypeAdapter(JBannerPatternType.class, new JBannerPatternType.Serializer())
            .registerTypeAdapter(JBannerPatternsComponent.class, new JBannerPatternsComponent.Serializer())
            .registerTypeAdapter(JBeesComponent.BeeData.class, new JBeesComponent.BeeData.Serializer())
            .registerTypeAdapter(JBeesComponent.class, new JBeesComponent.Serializer())
            .registerTypeAdapter(JCodecBuilderComponent.class, new JCodecBuilderComponent.Serializer<>())
            .registerTypeAdapter(JCodecComponent.class, new JCodecComponent.Serializer<>())
            .registerTypeAdapter(JColorComponent.class, new JColorComponent.Serializer())
            .registerTypeAdapter(JCondition.class, new JCondition.Serializer())
            .registerTypeAdapter(JContainerComponent.class, new JContainerComponent.Serializer())
            .registerTypeAdapter(JCustomDataComponent.class, new JCustomDataComponent.Serializer())
            .registerTypeAdapter(JDyedColorComponent.class, new JDyedColorComponent.Serializer())
            .registerTypeAdapter(JEnchantmentsComponent.class, new JEnchantmentsComponent.Serializer())
            .registerTypeAdapter(JIngredient.class, new JIngredient.Serializer())
            .registerTypeAdapter(JItemListComponent.class, new JItemListComponent.Serializer())
            .registerTypeAdapter(JListComponent.class, new JListComponent.Serializer())
            .registerTypeAdapter(JLootTable.class, new JLootTable.Serializer())
            .registerTypeAdapter(JMultipart.class, new JMultipart.Serializer())
            .registerTypeAdapter(JMultitypeComponent.class, new JMultitypeComponent.Serializer())
            .registerTypeAdapter(JPool.class, new JPool.Serializer())
            .registerTypeAdapter(JSimpleComponent.class, new JSimpleComponent.Serializer())
            .registerTypeAdapter(JSound.class, new JSound.Serializer())
            .registerTypeAdapter(JState.class, new JState.Serializer())
            .registerTypeAdapter(JStringMapComponent.class, new JStringMapComponent.Serializer())
            .registerTypeAdapter(JTextures.class, new JTextures.Serializer())
            .registerTypeAdapter(JWhen.class, new JWhen.Serializer())
            .registerTypeAdapter(JWritableBookContentComponent.class, new JWritableBookContentComponent.Serializer())
            .registerTypeAdapter(MutableComponent.class, new CodecSerializer<>(ComponentSerialization.CODEC))
            .registerTypeAdapter(Identifier.class, new IdSerializer())
            .registerTypeAdapter(ItemAttributeModifiers.class, new CodecSerializer<>(ItemAttributeModifiers.CODEC))
            .setPrettyPrinting()
            .disableHtmlEscaping()
            .create();
    private static final Logger LOGGER = LogUtils.getLogger();
    private final Set<String> KEY_WARNINGS = Collections.newSetFromMap(new ConcurrentHashMap<>());

    private final Identifier id;
    private final PackLocationInfo info;
    private final Lock lock = new ReentrantLock();
    private final Map<Identifier, Supplier<byte[]>> assets = new ConcurrentHashMap<>();
    private final Map<Identifier, Supplier<byte[]>> data = new ConcurrentHashMap<>();
    private final Map<List<String>, Supplier<byte[]>> root = new ConcurrentHashMap<>();
    private final Map<Identifier, JLang> langMergeable = new ConcurrentHashMap<>();

    public VirtualPackImpl(Identifier resourceLocation) {
        this.id = resourceLocation;
        this.info = new PackLocationInfo(
                this.id.getNamespace() + ";" + this.id.getPath(),
                Component.literal(this.id.toString()),
                PackSource.DEFAULT,
                Optional.empty());
    }

    private static JsonObject createPackMeta(PackFormat packFormat) {
        JsonObject object = new JsonObject();

        object.addProperty("min_format", packFormat.major());
        object.addProperty("max_format", packFormat.major());

        object.addProperty("description", "virtual resource pack");
        return object;
    }

    public static byte[] serialize(Object object) {
        UnsafeByteArrayOutputStream byteArrayOutputStream = new UnsafeByteArrayOutputStream();
        OutputStreamWriter writer = new OutputStreamWriter(byteArrayOutputStream, StandardCharsets.UTF_8);
        GSON.toJson(object, writer);
        try {
            writer.close();
        } catch (IOException e) {
            throw new EngineException(e);
        }
        return byteArrayOutputStream.getBytes();
    }

    private static Identifier formatIdentifier(Identifier resourceLocation, String prefix, String append) {
        return Identifier.fromNamespaceAndPath(resourceLocation.getNamespace(), prefix + "/" + resourceLocation.getPath() + "." + append);
    }

    @Override
    public @Nullable IoSupplier<@NotNull InputStream> getRootResource(String @NotNull ... pElements) {
        this.lock();
        Supplier<byte[]> byteArraySupplier = this.root.get(Arrays.asList(pElements));
        if (byteArraySupplier == null) {
            this.lock.unlock();
            return null;
        }
        this.lock.unlock();
        return () -> new ByteArrayInputStream(byteArraySupplier.get());
    }

    @Override
    public @Nullable IoSupplier<@NotNull InputStream> getResource(@NotNull PackType pPackType, @NotNull Identifier pLocation) {
        this.lock();
        Supplier<byte[]> byteArraySupplier = this.getSys(pPackType).get(pLocation);
        if (byteArraySupplier == null) {
            this.lock.unlock();
            return null;
        }
        this.lock.unlock();
        return () -> new ByteArrayInputStream(byteArraySupplier.get());
    }

    @Override
    public void listResources(@NotNull PackType pPackType, @NotNull String pNamespace, @NotNull String pPath, @NotNull ResourceOutput pResourceOutput) {
        this.lock();
        for (Identifier resourceLocation : this.getSys(pPackType).keySet()) {
            Supplier<byte[]> byteArraySupplier = this.getSys(pPackType).get(resourceLocation);
            if (byteArraySupplier == null) {
                this.lock.unlock();
                continue;
            }
            IoSupplier<@NotNull InputStream> ioSupplier = () -> new ByteArrayInputStream(byteArraySupplier.get());
            if (resourceLocation.getNamespace().equals(pNamespace) && resourceLocation.getPath().startsWith(pPath)) {
                pResourceOutput.accept(resourceLocation, ioSupplier);
            }
        }
        this.lock.unlock();
    }

    @Override
    public @NotNull Set<String> getNamespaces(@NotNull PackType pType) {
        this.lock();
        Set<String> namespaces = new HashSet<>();
        for (Identifier resourceLocation : this.getSys(pType).keySet()) {
            namespaces.add(resourceLocation.getNamespace());
        }
        this.lock.unlock();
        return namespaces;
    }

    @Override
    public @Nullable <T> T getMetadataSection(@NotNull MetadataSectionType<@NotNull T> pSectionType) {
        return this.getMetadataSection(pSectionType, null);
    }

    public @Nullable <T> T getMetadataSection(@NotNull MetadataSectionType<@NotNull T> pSectionType, @Nullable PackFormat packFormat) {
        try {
            IoSupplier<@NotNull InputStream> ioSupplier = this.getRootResource("pack.mcmeta");
            if (ioSupplier != null) {
                try (InputStream inputStream = ioSupplier.get()) {
                    ResourceMetadata metadata = ResourceMetadata.fromJsonStream(inputStream);
                    return metadata.getSection(pSectionType).orElse(null);
                }
            }
        } catch (IOException exception) {
            throw new EngineException(exception);
        }

        if (pSectionType.name().equals("pack")) {
            DataResult<T> result = pSectionType.codec().parse(JsonOps.INSTANCE, createPackMeta(packFormat));
            return result.getOrThrow(s -> new EngineException("Resource Pack information could not be parsed: " + s));
        }

        if (KEY_WARNINGS.add(pSectionType.name())) {
            LOGGER.debug("\"{}\" is an unsupported metadata key", pSectionType.name());
        }
        return null;
    }

    @Override
    public @NotNull PackLocationInfo location() {
        return this.info;
    }

    @Override
    public void close() {
        LOGGER.info("Closing Runtime Resource Pack {}", this.id);

        if (CommonConfig.DUMP.getAsBoolean()) {
            this.lock();
            this.dump();
            this.lock.unlock();
        }
    }

    private void write(Path dir, Identifier identifier, byte[] data) {
        try {
            String namespace = identifier.getNamespace();
            String path = identifier.getPath();
            Path file = dir.resolve(namespace).resolve(path);
            if (file.toAbsolutePath().startsWith(dir.toAbsolutePath())) {
                Files.createDirectories(file.getParent());
                try (OutputStream output = Files.newOutputStream(file)) {
                    output.write(data);
                }
            } else {
                LOGGER.error("Virtual Pack contains out-of-directory location! \"{}/{}\"", namespace, path);
            }

        } catch (IOException e) {
            throw new EngineException(e);
        }
    }

    private void dump() {
        LOGGER.info("Dumping generated resources for \"{}\"", this.id);

        Path output = Path.of(CommonConfig.DUMP_DIRECTORY.get());
        try {
            for (Map.Entry<List<String>, Supplier<byte[]>> e : this.root.entrySet()) {
                String pathStr = String.join("/", e.getKey());
                Path path = output.resolve(pathStr);
                if (path.toAbsolutePath().startsWith(output.toAbsolutePath())) {
                    Files.createDirectories(path.getParent());
                    Files.write(path, e.getValue().get());
                } else {
                    LOGGER.error("RRP contains out-of-directory path! \"{}\"", pathStr);
                }
            }

            Path assets = output.resolve("assets");
            Files.createDirectories(assets);
            for (Map.Entry<Identifier, Supplier<byte[]>> entry : this.assets.entrySet()) {
                this.write(assets, entry.getKey(), entry.getValue().get());
            }

            Path data = output.resolve("data");
            Files.createDirectories(data);
            for (Map.Entry<Identifier, Supplier<byte[]>> entry : this.data.entrySet()) {
                this.write(data, entry.getKey(), entry.getValue().get());
            }
        } catch (IOException exception) {
            throw new EngineException(exception);

        }
    }

    private Map<Identifier, Supplier<byte[]>> getSys(PackType packType) {
        return packType == PackType.CLIENT_RESOURCES ? this.assets : this.data;
    }

    private void lock() {
        if (!this.lock.tryLock()) {
            this.lock.lock();
        }
    }

    @Override
    public void addRecoloredImage(Identifier resourceLocation, InputStream original, IntUnaryOperator pixel) {
        this.addLazyResource(
                PackType.CLIENT_RESOURCES,
                formatIdentifier(resourceLocation, "textures", "png"),
                (runtimeResourcePack, resourceLocation1) -> {
                    try {
                        CountingInputStream countingInputStream = new CountingInputStream(original);
                        BufferedImage base = ImageIO.read(original);
                        BufferedImage recolored = new BufferedImage(base.getWidth(), base.getHeight(), BufferedImage.TYPE_INT_ARGB);
                        for (int y = 0; y < base.getHeight(); y++) {
                            for (int x = 0; x < base.getWidth(); x++) {
                                recolored.setRGB(x, y, pixel.applyAsInt(base.getRGB(x, y)));
                            }
                        }

                        UnsafeByteArrayOutputStream arrayOutputStream = new UnsafeByteArrayOutputStream(countingInputStream.bytes());
                        ImageIO.write(recolored, "png", arrayOutputStream);
                        return arrayOutputStream.getBytes();
                    } catch (Throwable e) {
                        LOGGER.error("Error adding recolored image to runtime resource pack", e);
                        throw new EngineException(e);
                    }
                });
    }

    @Override
    public byte[] addLang(Identifier resourceLocation, JLang lang) {
        return this.addAsset(formatIdentifier(resourceLocation, "lang", "json"), serialize(lang.getLang()));
    }

    @Override
    public void mergeLang(Identifier resourceLocation, JLang lang) {
        this.langMergeable.compute(resourceLocation, (location, language) -> {
            if (language == null) {
                language = new JLang();
                JLang result = language;
                this.addLazyResource(
                        PackType.CLIENT_RESOURCES,
                        resourceLocation,
                        (resourcePack, packLocation) -> resourcePack.addLang(resourceLocation, result));
            }
            language.getLang().putAll(lang.getLang());
            return language;
        });
    }

    @Override
    public byte[] addLootTable(Identifier resourceLocation, JLootTable lootTable) {
        return this.addData(formatIdentifier(resourceLocation, "loot_table", "json"), serialize(lootTable));
    }

    @Override
    public byte[] addAdvancement(Identifier resourceLocation, JAdvancement advancement) {
        return this.addData(formatIdentifier(resourceLocation, "advancement", "json"), serialize(advancement));
    }

    @Override
    public void addLazyResource(PackType packType, Identifier path, BiFunction<VirtualResourcePack, Identifier, byte[]> data) {
        this.getSys(packType).put(path, new Memoized<>(data, path));
    }

    @Override
    public byte[] addResource(PackType packType, Identifier path, byte[] data) {
        this.getSys(packType).put(path, () -> data);
        return data;
    }

    @Override
    public byte[] addRootResource(String path, byte[] data) {
        this.root.put(List.of(path.split("/")), () -> data);
        return data;
    }

    @Override
    public byte[] addAsset(Identifier path, byte[] data) {
        return this.addResource(PackType.CLIENT_RESOURCES, path, data);
    }

    @Override
    public byte[] addData(Identifier path, byte[] data) {
        return this.addResource(PackType.SERVER_DATA, path, data);
    }

    @Override
    public byte[] addModel(Identifier path, JModel model) {
        return this.addAsset(formatIdentifier(path, "models", "json"), serialize(model));
    }

    @Override
    public byte[] addBlockSate(Identifier path, JState state) {
        return this.addAsset(formatIdentifier(path, "blockstates", "json"), serialize(state));
    }

    @Override
    public byte[] addTexture(Identifier path, BufferedImage image) {
        UnsafeByteArrayOutputStream byteArrayOutputStream = new UnsafeByteArrayOutputStream();
        try {
            ImageIO.write(image, "png", byteArrayOutputStream);
        } catch (IOException e) {
            throw new EngineException(e);
        }
        return this.addAsset(formatIdentifier(path, "textures", "png"), byteArrayOutputStream.getBytes());
    }

    @Override
    public byte[] addAnimation(Identifier path, JAnimation animation) {
        return this.addAsset(formatIdentifier(path, "textures", "png.mcmeta"), serialize(animation));
    }

    @Override
    public byte[] addTag(Identifier path, JTag tag) {
        return this.addData(formatIdentifier(path, "tags", "json"), serialize(tag));
    }

    @Override
    public byte[] addRecipe(Identifier path, AbstractJRecipe recipe) {
        return this.addData(formatIdentifier(path, "recipe", "json"), serialize(recipe));
    }

    @Override
    public byte[] addItemModifier(Identifier path, ItemAttributeModifiers modifiers) {
        return this.addData(formatIdentifier(path, "item_modifier", "json"), serialize(modifiers));
    }

    @Override
    public byte[] addItemModifier(Identifier path, ItemAttributeModifiers.Builder modifiers) {
        return this.addItemModifier(path, modifiers.build());
    }

    @Override
    public byte[] addItemDefinition(Identifier itemId, String modelReference) {
        JsonObject modelObj = new JsonObject();
        modelObj.addProperty("type", "minecraft:model");
        modelObj.addProperty("model", modelReference);

        JsonObject root = new JsonObject();
        root.add("model", modelObj);

        byte[] bytes = GSON.toJson(root).getBytes(StandardCharsets.UTF_8);

        Identifier assetPath = Identifier.fromNamespaceAndPath(
                itemId.getNamespace(),
                "items/" + itemId.getPath() + ".json"
        );
        return this.addAsset(assetPath, bytes);
    }

    @Override
    public void load(Path directory) throws IOException {
        try (Stream<Path> stream = Files.walk(directory)) {
            for (Path file : (Iterable<Path>) () -> stream.filter(Files::isRegularFile).map(directory::relativize).iterator()) {
                String string = file.toString();
                if (string.startsWith("assets")) {
                    String path = string.substring("assets".length() + 1);
                    this.load(path, this.assets, Files.readAllBytes(file));
                } else if (string.startsWith("data")) {
                    String path = string.substring("data".length() + 1);
                    this.load(path, this.data, Files.readAllBytes(file));
                } else {
                    byte[] data = Files.readAllBytes(file);
                    this.root.put(List.of(string.split("/")), () -> data);
                }
            }
        }
    }

    @Override
    public void load(ZipInputStream zipStream) throws IOException {
        ZipEntry entry;
        while ((entry = zipStream.getNextEntry()) != null) {
            String string = entry.toString();
            if (string.startsWith("assets")) {
                String path = string.substring("assets".length() + 1);
                this.load(path, this.assets, this.read(entry, zipStream));
            } else if (string.startsWith("data")) {
                String path = string.substring("data".length() + 1);
                this.load(path, this.data, this.read(entry, zipStream));
            } else {
                byte[] data = this.read(entry, zipStream);
                this.root.put(Arrays.asList(string.split("/")), () -> data);
            }
        }
    }

    private void load(String fullPath, Map<Identifier, Supplier<byte[]>> map, byte[] data) {
        int separatorIndex = fullPath.indexOf("/");
        String namespace = fullPath.substring(0, separatorIndex);
        String path = fullPath.substring(separatorIndex + 1);
        map.put(Identifier.fromNamespaceAndPath(namespace, path), () -> data);
    }

    private byte[] read(ZipEntry entry, InputStream inputStream) throws IOException {
        byte[] data = new byte[Math.toIntExact(entry.getSize())];
        if (inputStream.read(data) != data.length) {
            throw new IOException("Zip stream was cut off.");
        }
        return data;
    }

    private class Memoized<T> implements Supplier<byte[]> {
        private final BiFunction<VirtualResourcePack, T, byte[]> function;
        private final T path;
        private byte @Nullable [] data;

        public Memoized(BiFunction<VirtualResourcePack, T, byte[]> function, T path) {
            this.function = function;
            this.path = path;
            this.data = null;
        }

        @Override
        public byte[] get() {
            if (this.data == null) {
                this.data = this.function.apply(VirtualPackImpl.this, path);
            }
            return this.data;
        }
    }
}