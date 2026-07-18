package de.luckymcdev.foundryengine.common.builder;

import net.minecraft.resources.Identifier;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AbstractBuilderTest {

	private static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath("test", path);
	}

	@Test
	void constructor_SetsId() {
		var builder = new TestBuilder(id("my_object"));
		assertEquals(id("my_object"), builder.getId());
	}

	@Test
	void get_BeforeBuild_Throws() {
		var builder = new TestBuilder(id("unbuilt"));
		assertThrows(IllegalStateException.class, builder::get);
	}

	@Test
	void get_AfterRegister_ReturnsObject() {
		var builder = new TestBuilder(id("built"));
		builder.build();
		builder.register();
		assertEquals("constructed", builder.get());
	}

	@Test
	void getOrCreate_CreatesOnFirstCall() {
		var builder = new TestBuilder(id("lazy"));
		String result = builder.getOrCreate();
		assertEquals("constructed", result);
	}

	@Test
	void getOrCreate_ReturnsCached() {
		var builder = new TestBuilder(id("cached"));
		String first = builder.getOrCreate();
		String second = builder.getOrCreate();
		assertSame(first, second);
	}

	@Test
	void shouldGenerateData_Default_True() {
		var builder = new TestBuilder(id("default"));
		assertTrue(builder.shouldGenerateData());
	}

	@Test
	void newID_BothEmpty_ReturnsSame() {
		var builder = new TestBuilder(id("path"));
		assertEquals(id("path"), builder.newID("", ""));
	}

	@Test
	void newID_PrefixOnly() {
		var builder = new TestBuilder(id("path"));
		Identifier result = builder.newID("pre_", "");
		assertEquals(Identifier.fromNamespaceAndPath("test", "pre_path"), result);
	}

	@Test
	void newID_SuffixOnly() {
		var builder = new TestBuilder(id("path"));
		Identifier result = builder.newID("", "_suf");
		assertEquals(Identifier.fromNamespaceAndPath("test", "path_suf"), result);
	}

	@Test
	void newID_BothPrefixAndSuffix() {
		var builder = new TestBuilder(id("path"));
		Identifier result = builder.newID("pre_", "_suf");
		assertEquals(Identifier.fromNamespaceAndPath("test", "pre_path_suf"), result);
	}

	private static class TestBuilder extends AbstractBuilder<String> {
		TestBuilder(Identifier id) {
			super(id);
		}

		@Override
		protected String build() {
			return "constructed";
		}

		void register() {
			setObject(build());
		}
	}
}
