package de.luckymcdev.foundryengine.common.registry;

import net.minecraft.util.RandomSource;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Tests using the Registry interface contract via a mock implementation.
 */
class RegistryTest {

    @Test
    void registry_InterfaceDefaultValue() {
        Registry<String, Integer> reg = new TestRegistry();
        reg.register("a", 1);
        assertEquals(1, reg.get("a"));
        assertNull(reg.get("missing"));
    }

    private static class TestRegistry implements Registry<String, Integer> {
        private final java.util.Map<String, Integer> map = new java.util.HashMap<>();

        @Override public void register(String key, Integer value) { map.put(key, value); }
        @Override public void remove(String key) { map.remove(key); }
        @Override public Integer get(String key) { return map.get(key); }
        @Override public RegistryRef<String, Integer> getRef(String key) { return null; }
        @Override public String getKey(Integer value) { return null; }
        @Override public Integer getRandom(RandomSource random) { return null; }
        @Override public Optional<Integer> tryGet(String key) { return Optional.ofNullable(map.get(key)); }
        @Override public boolean contains(String key) { return map.containsKey(key); }
        @Override public Collection<Integer> values() { return map.values(); }
        @Override public Collection<String> keys() { return map.keySet(); }
        @Override public boolean isFrozen() { return false; }
        @Override public void freeze() { }
        @Override public void unfreeze() { }
        @Override public boolean onFreeze(Runnable callback) { return false; }
        @Override public void forEach(BiConsumer<String, Integer> kvConsumer) { map.forEach(kvConsumer); }
        @Override public void forEach(Consumer<Integer> action) { map.values().forEach(action); }
        @Override public Stream<Integer> stream() { return map.values().stream(); }
        @Override public void clear() throws IllegalStateException { map.clear(); }

		@Override
	    public int size() {
		    return map.size();
	    }

		@Override
	    public boolean isEmpty() {
		    return map.isEmpty();
	    }
	}
}
