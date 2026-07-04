package de.luckymcdev.foundryengine.common.event;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class EventGroupTest {

    @Test
    void addAndPost_SingleCallback_ExecutedOnce() {
        EventGroup<String> group = new EventGroup<>();
        List<String> received = new ArrayList<>();
        group.add(received::add);
        group.post("hello");
        assertEquals(List.of("hello"), received);
    }

    @Test
    void addAndPost_MultipleCallbacks_AllExecuted() {
        EventGroup<String> group = new EventGroup<>();
        AtomicInteger counter = new AtomicInteger();
        group.add(e -> counter.incrementAndGet());
        group.add(e -> counter.incrementAndGet());
        group.add(e -> counter.incrementAndGet());
        group.post("test");
        assertEquals(3, counter.get());
    }

    @Test
    void post_NoCallbacks_DoesNothing() {
        EventGroup<String> group = new EventGroup<>();
        assertDoesNotThrow(() -> group.post("test"));
    }

    @Test
    void post_ExceptionInCallback_DoesNotPropagate() {
        EventGroup<String> group = new EventGroup<>();
        group.add(e -> { throw new RuntimeException("Boom"); });
        group.add(e -> { /* should still run */ });
        assertDoesNotThrow(() -> group.post("test"));
    }

    @Test
    void post_ExceptionInFirstCallback_SecondStillExecutes() {
        EventGroup<String> group = new EventGroup<>();
        AtomicBoolean secondRan = new AtomicBoolean(false);
        group.add(e -> { throw new RuntimeException("Boom"); });
        group.add(e -> secondRan.set(true));
        group.post("test");
        assertTrue(secondRan.get());
    }

    @Test
    void clear_RemovesAllCallbacks() {
        EventGroup<String> group = new EventGroup<>();
        AtomicInteger counter = new AtomicInteger();
        group.add(e -> counter.incrementAndGet());
        group.clear();
        group.post("test");
        assertEquals(0, counter.get());
    }

    @Test
    void clear_EmptyGroup_DoesNothing() {
        EventGroup<String> group = new EventGroup<>();
        assertDoesNotThrow(group::clear);
    }

    @Test
    void addAfterClear_NewCallbacksWork() {
        EventGroup<String> group = new EventGroup<>();
        group.add(e -> { throw new RuntimeException(); });
        group.clear();
        AtomicBoolean ran = new AtomicBoolean(false);
        group.add(e -> ran.set(true));
        group.post("test");
        assertTrue(ran.get());
    }

    @Test
    void post_EventObjectPassedToCallbacks() {
        EventGroup<Integer> group = new EventGroup<>();
        List<Integer> received = new ArrayList<>();
        group.add(received::add);
        group.post(42);
        assertEquals(1, received.size());
        assertEquals(42, received.get(0));
    }

    @Test
    void multiplePosts_AllCallbacksCalledEachTime() {
        EventGroup<String> group = new EventGroup<>();
        AtomicInteger counter = new AtomicInteger();
        group.add(e -> counter.incrementAndGet());
        group.post("a");
        group.post("b");
        group.post("c");
        assertEquals(3, counter.get());
    }
}
