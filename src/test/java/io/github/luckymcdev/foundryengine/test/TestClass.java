package io.github.luckymcdev.foundryengine.test;

import net.minecraft.server.MinecraftServer;
import net.neoforged.testframework.junit.EphemeralTestServerProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(EphemeralTestServerProvider.class)
class TestClass {
    @Test
    void testMethod(MinecraftServer server) {
        assert server != null;
    }
}
