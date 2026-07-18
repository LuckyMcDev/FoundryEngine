package de.luckymcdev.foundryengine.common.event;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

class EventGroupHolderTest {

	@Test
	void register_Post_CallbackExecuted() {
		EventGroupHolder<String> holder = new EventGroupHolder<>();
		List<String> received = new ArrayList<>();
		holder.register(received::add);
		holder.post("data");
		assertEquals(List.of("data"), received);
	}

	@Test
	void register_MultipleCallbacks_AllExecuted() {
		EventGroupHolder<Integer> holder = new EventGroupHolder<>();
		AtomicInteger sum = new AtomicInteger();
		holder.register(e -> sum.addAndGet(e));
		holder.register(e -> sum.addAndGet(e));
		holder.post(5);
		assertEquals(10, sum.get());
	}

	@Test
	void clear_RemovesAll() {
		EventGroupHolder<String> holder = new EventGroupHolder<>();
		AtomicInteger counter = new AtomicInteger();
		holder.register(e -> counter.incrementAndGet());
		holder.clear();
		holder.post("x");
		assertEquals(0, counter.get());
	}

	@Test
	void clear_ThenRegisterNew_NewCallbacksWork() {
		EventGroupHolder<String> holder = new EventGroupHolder<>();
		holder.register(e -> {
		});
		holder.clear();
		AtomicInteger counter = new AtomicInteger();
		holder.register(e -> counter.incrementAndGet());
		holder.post("x");
		assertEquals(1, counter.get());
	}

	@Test
	void post_Exception_DoesNotPropagate() {
		EventGroupHolder<String> holder = new EventGroupHolder<>();
		holder.register(e -> {
			throw new RuntimeException();
		});
		assertDoesNotThrow(() -> holder.post("x"));
	}

	@Test
	void multiplePosts_EachCallbackFiresEachTime() {
		EventGroupHolder<String> holder = new EventGroupHolder<>();
		AtomicInteger counter = new AtomicInteger();
		holder.register(e -> counter.incrementAndGet());
		holder.post("a");
		holder.post("b");
		assertEquals(2, counter.get());
	}
}
