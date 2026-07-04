package de.luckymcdev.foundryengine.common.registry;

import net.minecraft.resources.Identifier;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

class RegistryRefTest {

    private static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath("test", path);
    }

    @Test
    void get_ExistingValue_Returns() {
        GenericRegistry<Identifier, String> reg = new GenericRegistry<>();
        reg.register(id("x"), "hello");
        RegistryRef<Identifier, String> ref = reg.getRef(id("x"));
        assertEquals("hello", ref.get());
    }

    @Test
    void get_NonExistent_ReturnsNull() {
        GenericRegistry<Identifier, String> reg = new GenericRegistry<>();
        RegistryRef<Identifier, String> ref = reg.getRef(id("missing"));
        assertNull(ref.get());
    }

    @Test
    void get_WithDefaultValue_ReturnsDefault() {
        GenericRegistry<Identifier, String> reg = new GenericRegistry<>();
        reg.setDefaultValue("default");
        RegistryRef<Identifier, String> ref = reg.getRef(id("missing"));
        assertEquals("default", ref.get());
    }

    @Test
    void get_CachesAfterFreeze() {
        GenericRegistry<Identifier, String> reg = new GenericRegistry<>();
        reg.register(id("x"), "before");
        RegistryRef<Identifier, String> ref = reg.getRef(id("x"));
        assertEquals("before", ref.get());
        assertFalse(ref.isCached());
        reg.freeze();
        // After freeze, get() should cache the value
        ref.get();
        assertTrue(ref.isCached());
    }

    @Test
    void invalidateCache_ForcesReload() {
        GenericRegistry<Identifier, String> reg = new GenericRegistry<>();
        reg.register(id("x"), "original");
        reg.freeze();
        RegistryRef<Identifier, String> ref = reg.getRef(id("x"));
        assertEquals("original", ref.get());
        assertTrue(ref.isCached());
        ref.invalidateCache();
        assertFalse(ref.isCached());
    }

    @Test
    void orElse_Existing_ReturnsValue() {
        GenericRegistry<Identifier, String> reg = new GenericRegistry<>();
        reg.register(id("x"), "present");
        assertEquals("present", reg.getRef(id("x")).orElse("fallback"));
    }

    @Test
    void orElse_Missing_ReturnsFallback() {
        GenericRegistry<Identifier, String> reg = new GenericRegistry<>();
        assertEquals("fallback", reg.getRef(id("missing")).orElse("fallback"));
    }

    @Test
    void orElseGet_Existing_ReturnsValue() {
        GenericRegistry<Identifier, String> reg = new GenericRegistry<>();
        reg.register(id("x"), "yes");
        assertEquals("yes", reg.getRef(id("x")).orElseGet(() -> "no"));
    }

    @Test
    void orElseGet_Missing_Computes() {
        GenericRegistry<Identifier, String> reg = new GenericRegistry<>();
        assertEquals("computed", reg.getRef(id("x")).orElseGet(() -> "computed"));
    }

    @Test
    void toOptional_Existing_Present() {
        GenericRegistry<Identifier, String> reg = new GenericRegistry<>();
        reg.register(id("x"), "val");
        Optional<String> opt = reg.getRef(id("x")).toOptional();
        assertTrue(opt.isPresent());
        assertEquals("val", opt.get());
    }

    @Test
    void toOptional_Missing_Empty() {
        GenericRegistry<Identifier, String> reg = new GenericRegistry<>();
        assertTrue(reg.getRef(id("x")).toOptional().isEmpty());
    }

    @Test
    void exists_Existing_True() {
        GenericRegistry<Identifier, String> reg = new GenericRegistry<>();
        reg.register(id("x"), "v");
        assertTrue(reg.getRef(id("x")).exists());
    }

    @Test
    void exists_Missing_False() {
        GenericRegistry<Identifier, String> reg = new GenericRegistry<>();
        assertFalse(reg.getRef(id("x")).exists());
    }

    @Test
    void ifPresent_Existing_Runs() {
        GenericRegistry<Identifier, String> reg = new GenericRegistry<>();
        reg.register(id("x"), "val");
        AtomicBoolean ran = new AtomicBoolean(false);
        reg.getRef(id("x")).ifPresent(v -> ran.set(true));
        assertTrue(ran.get());
    }

    @Test
    void ifPresent_Missing_DoesNotRun() {
        GenericRegistry<Identifier, String> reg = new GenericRegistry<>();
        AtomicBoolean ran = new AtomicBoolean(false);
        reg.getRef(id("x")).ifPresent(v -> ran.set(true));
        assertFalse(ran.get());
    }

    @Test
    void ifPresentOrElse_Existing_RunsAction() {
        GenericRegistry<Identifier, String> reg = new GenericRegistry<>();
        reg.register(id("x"), "val");
        AtomicBoolean actionRan = new AtomicBoolean(false);
        reg.getRef(id("x")).ifPresentOrElse(v -> actionRan.set(true), () -> fail());
        assertTrue(actionRan.get());
    }

    @Test
    void ifPresentOrElse_Missing_RunsEmpty() {
        GenericRegistry<Identifier, String> reg = new GenericRegistry<>();
        AtomicBoolean emptyRan = new AtomicBoolean(false);
        reg.getRef(id("x")).ifPresentOrElse(v -> fail(), () -> emptyRan.set(true));
        assertTrue(emptyRan.get());
    }

    @Test
    void map_Existing_Transforms() {
        GenericRegistry<Identifier, String> reg = new GenericRegistry<>();
        reg.register(id("x"), "hello");
        Optional<Integer> mapped = reg.getRef(id("x")).map(String::length);
        assertEquals(Optional.of(5), mapped);
    }

    @Test
    void map_Missing_ReturnsEmpty() {
        GenericRegistry<Identifier, String> reg = new GenericRegistry<>();
        Optional<Integer> mapped = reg.getRef(id("x")).map(String::length);
        assertTrue(mapped.isEmpty());
    }

    @Test
    void getKey_ReturnsKey() {
        GenericRegistry<Identifier, String> reg = new GenericRegistry<>();
        Identifier key = id("mykey");
        assertEquals(key, reg.getRef(key).getKey());
    }

    @Test
    void equality_SameKeySameRegistry_Equal() {
        GenericRegistry<Identifier, String> reg = new GenericRegistry<>();
        Identifier key = id("key");
        RegistryRef<Identifier, String> a = reg.getRef(key);
        RegistryRef<Identifier, String> b = reg.getRef(key);
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void toString_ContainsKey() {
        GenericRegistry<Identifier, String> reg = new GenericRegistry<>();
        String str = reg.getRef(id("x")).toString();
        assertTrue(str.contains("x"));
        assertTrue(str.contains("exists="));
    }
}
