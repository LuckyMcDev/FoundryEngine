package de.luckymcdev.foundryengine.common.registry;

import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class GenericRegistryTest {

    private static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath("test", path);
    }

    @Test
    void register_Get_SingleEntry() {
        GenericRegistry<Identifier, String> reg = new GenericRegistry<>();
        Identifier key = id("entry");
        reg.register(key, "value");
        assertEquals("value", reg.get(key));
    }

    @Test
    void register_MultipleEntries() {
        GenericRegistry<Identifier, Integer> reg = new GenericRegistry<>();
        reg.register(id("a"), 1);
        reg.register(id("b"), 2);
        assertEquals(1, reg.get(id("a")));
        assertEquals(2, reg.get(id("b")));
        assertEquals(2, reg.size());
    }

    @Test
    void get_NonExistent_ReturnsNull() {
        GenericRegistry<Identifier, String> reg = new GenericRegistry<>();
        assertNull(reg.get(id("nonexistent")));
    }

    @Test
    void get_WithDefaultValue_ReturnsDefault() {
        GenericRegistry<Identifier, String> reg = new GenericRegistry<>();
        reg.setDefaultValue("default");
        assertEquals("default", reg.get(id("missing")));
    }

    @Test
    void remove_Existing_RemovesEntry() {
        GenericRegistry<Identifier, String> reg = new GenericRegistry<>();
        Identifier key = id("remove_me");
        reg.register(key, "value");
        reg.remove(key);
        assertNull(reg.get(key));
        assertEquals(0, reg.size());
    }

    @Test
    void remove_NonExistent_DoesNothing() {
        GenericRegistry<Identifier, String> reg = new GenericRegistry<>();
        reg.register(id("a"), "v");
        reg.remove(id("nonexistent"));
        assertEquals(1, reg.size());
    }

    @Test
    void register_AfterFreeze_Throws() {
        GenericRegistry<Identifier, String> reg = new GenericRegistry<>();
        reg.freeze();
        assertThrows(IllegalStateException.class, () -> reg.register(id("x"), "y"));
    }

    @Test
    void remove_AfterFreeze_Throws() {
        GenericRegistry<Identifier, String> reg = new GenericRegistry<>();
        reg.freeze();
        assertThrows(IllegalStateException.class, () -> reg.remove(id("x")));
    }

    @Test
    void isFrozen_Frozen_True() {
        GenericRegistry<Identifier, String> reg = new GenericRegistry<>();
        assertFalse(reg.isFrozen());
        reg.freeze();
        assertTrue(reg.isFrozen());
    }

    @Test
    void unfreeze_AllowsModification() {
        GenericRegistry<Identifier, String> reg = new GenericRegistry<>();
        reg.freeze();
        reg.unfreeze();
        assertDoesNotThrow(() -> reg.register(id("x"), "y"));
        assertEquals("y", reg.get(id("x")));
    }

    @Test
    void freeze_Idempotent() {
        GenericRegistry<Identifier, String> reg = new GenericRegistry<>();
        reg.freeze();
        assertDoesNotThrow(reg::freeze);
        assertTrue(reg.isFrozen());
    }

    @Test
    void getKey_ReverseLookup() {
        GenericRegistry<Identifier, String> reg = new GenericRegistry<>();
        Identifier key = id("mykey");
        reg.register(key, "myvalue");
        assertEquals(key, reg.getKey("myvalue"));
    }

    @Test
    void getKey_NonExistentValue_ReturnsNull() {
        GenericRegistry<Identifier, String> reg = new GenericRegistry<>();
        assertNull(reg.getKey("unknown"));
    }

    @Test
    void tryGet_Existing_ReturnsOptional() {
        GenericRegistry<Identifier, String> reg = new GenericRegistry<>();
        reg.register(id("x"), "val");
        Optional<String> result = reg.tryGet(id("x"));
        assertTrue(result.isPresent());
        assertEquals("val", result.get());
    }

    @Test
    void tryGet_Missing_ReturnsEmpty() {
        GenericRegistry<Identifier, String> reg = new GenericRegistry<>();
        assertTrue(reg.tryGet(id("missing")).isEmpty());
    }

    @Test
    void contains_Existing_True() {
        GenericRegistry<Identifier, String> reg = new GenericRegistry<>();
        reg.register(id("x"), "v");
        assertTrue(reg.contains(id("x")));
    }

    @Test
    void contains_Missing_False() {
        GenericRegistry<Identifier, String> reg = new GenericRegistry<>();
        assertFalse(reg.contains(id("x")));
    }

    @Test
    void values_ReturnsAll() {
        GenericRegistry<Identifier, String> reg = new GenericRegistry<>();
        reg.register(id("a"), "alpha");
        reg.register(id("b"), "beta");
        assertTrue(reg.values().contains("alpha"));
        assertTrue(reg.values().contains("beta"));
        assertEquals(2, reg.values().size());
    }

    @Test
    void values_Unmodifiable() {
        GenericRegistry<Identifier, String> reg = new GenericRegistry<>();
        reg.register(id("x"), "v");
        assertThrows(UnsupportedOperationException.class, () -> reg.values().add("y"));
    }

    @Test
    void keys_ReturnsAll() {
        GenericRegistry<Identifier, String> reg = new GenericRegistry<>();
        reg.register(id("a"), "v1");
        reg.register(id("b"), "v2");
        assertTrue(reg.keys().contains(id("a")));
        assertTrue(reg.keys().contains(id("b")));
    }

    @Test
    void keys_Unmodifiable() {
        GenericRegistry<Identifier, String> reg = new GenericRegistry<>();
        assertThrows(UnsupportedOperationException.class, () -> reg.keys().add(id("x")));
    }

    @Test
    void size_Empty_Zero() {
        assertEquals(0, new GenericRegistry<>().size());
    }

    @Test
    void clear_RemovesAll() {
        GenericRegistry<Identifier, String> reg = new GenericRegistry<>();
        reg.register(id("a"), "v1");
        reg.register(id("b"), "v2");
        reg.clear();
        assertEquals(0, reg.size());
        assertNull(reg.get(id("a")));
        assertFalse(reg.isFrozen());
    }

    @Test
    void forEach_BiConsumer() {
        GenericRegistry<Identifier, String> reg = new GenericRegistry<>();
        reg.register(id("x"), "1");
        reg.register(id("y"), "2");
        List<String> pairs = new ArrayList<>();
        reg.forEach((k, v) -> pairs.add(k + "=" + v));
        assertTrue(pairs.contains("test:x=1"));
        assertTrue(pairs.contains("test:y=2"));
    }

    @Test
    void forEach_Consumer() {
        GenericRegistry<Identifier, String> reg = new GenericRegistry<>();
        reg.register(id("a"), "val");
        List<String> collected = new ArrayList<>();
        java.util.function.Consumer<String> consumer = collected::add;
        reg.forEach(consumer);
        assertEquals(List.of("val"), collected);
    }

    @Test
    void stream_ReturnsValues() {
        GenericRegistry<Identifier, String> reg = new GenericRegistry<>();
        reg.register(id("a"), "1");
        reg.register(id("b"), "2");
        var list = reg.stream().collect(Collectors.toList());
        assertTrue(list.contains("1"));
        assertTrue(list.contains("2"));
    }

    @Test
    void getRandom_ReturnsExistingValue() {
        GenericRegistry<Identifier, String> reg = new GenericRegistry<>();
        reg.register(id("x"), "only");
        String val = reg.getRandom(RandomSource.create());
        assertEquals("only", val);
    }

    @Test
    void getRandom_Empty_ReturnsDefault() {
        GenericRegistry<Identifier, String> reg = new GenericRegistry<>();
        reg.setDefaultValue("def");
        assertEquals("def", reg.getRandom(RandomSource.create()));
    }

    @Test
    void onFreeze_BeforeFreeze_CallbackAdded() {
        GenericRegistry<Identifier, String> reg = new GenericRegistry<>();
        AtomicBoolean called = new AtomicBoolean(false);
        assertTrue(reg.onFreeze(() -> called.set(true)));
        assertFalse(called.get());
        reg.freeze();
        assertTrue(called.get());
    }

    @Test
    void onFreeze_AfterFreeze_CallbackRunsImmediately() {
        GenericRegistry<Identifier, String> reg = new GenericRegistry<>();
        reg.freeze();
        AtomicBoolean called = new AtomicBoolean(false);
        assertFalse(reg.onFreeze(() -> called.set(true)));
        assertTrue(called.get());
    }

    @Test
    void onFreeze_MultipleCallbacks_AllRun() {
        GenericRegistry<Identifier, String> reg = new GenericRegistry<>();
        AtomicInteger counter = new AtomicInteger();
        reg.onFreeze(counter::incrementAndGet);
        reg.onFreeze(counter::incrementAndGet);
        reg.freeze();
        assertEquals(2, counter.get());
    }

    @Test
    void getRef_Caches() {
        GenericRegistry<Identifier, String> reg = new GenericRegistry<>();
        reg.register(id("x"), "val");
        RegistryRef<Identifier, String> ref1 = reg.getRef(id("x"));
        RegistryRef<Identifier, String> ref2 = reg.getRef(id("x"));
        assertSame(ref1, ref2);
    }

    @Test
    void register_DuplicateKey_ReplacesValue() {
        GenericRegistry<Identifier, String> reg = new GenericRegistry<>();
        Identifier key = id("dup");
        reg.register(key, "first");
        reg.register(key, "second");
        assertEquals("second", reg.get(key));
        assertEquals(1, reg.size());
    }

    @Test
    void clear_AfterFreeze_StillWorks() {
        GenericRegistry<Identifier, String> reg = new GenericRegistry<>();
        reg.register(id("x"), "v");
        reg.freeze();
        reg.clear();
        assertEquals(0, reg.size());
    }
}
