package de.luckymcdev.foundryengine.common.network;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PacketBoundsTest {

    @Test
    void isClient_ClientBound_True() {
        assertTrue(PacketBounds.CLIENT.isClient());
    }

    @Test
    void isClient_ServerBound_False() {
        assertFalse(PacketBounds.SERVER.isClient());
    }

    @Test
    void isClient_Both_False() {
        assertFalse(PacketBounds.BOTH.isClient());
    }

    @Test
    void isServer_ServerBound_True() {
        assertTrue(PacketBounds.SERVER.isServer());
    }

    @Test
    void isServer_ClientBound_False() {
        assertFalse(PacketBounds.CLIENT.isServer());
    }

    @Test
    void isServer_Both_False() {
        assertFalse(PacketBounds.BOTH.isServer());
    }

    @Test
    void isBoth_Both_True() {
        assertTrue(PacketBounds.BOTH.isBoth());
    }

    @Test
    void isBoth_Client_False() {
        assertFalse(PacketBounds.CLIENT.isBoth());
    }

    @Test
    void isBoth_Server_False() {
        assertFalse(PacketBounds.SERVER.isBoth());
    }

    @Test
    void enumValues_AllPresent() {
        PacketBounds[] values = PacketBounds.values();
        assertEquals(3, values.length);
        assertSame(PacketBounds.CLIENT, values[0]);
        assertSame(PacketBounds.SERVER, values[1]);
        assertSame(PacketBounds.BOTH, values[2]);
    }
}
