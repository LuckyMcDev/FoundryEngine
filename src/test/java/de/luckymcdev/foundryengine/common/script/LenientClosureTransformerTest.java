package de.luckymcdev.foundryengine.common.script;

import groovy.lang.GroovyClassLoader;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LenientClosureTransformerTest {

	private static final String SUPPORT = """
		package testsupport.builder.block
		interface UseCallback {
		    void run(Object a, Object b, Object c, Object d, Object e)
		}
		interface ShortCallback {
		    void run(Object a, Object b)
		}
		class BlockBuilder {
		    static BlockBuilder create(String id) { new BlockBuilder(id) }
		    private final String id
		    BlockBuilder(String id) { this.id = id }
		    UseCallback cb
		    ShortCallback cb2
		    BlockBuilder use(UseCallback callback) {
		        this.cb = callback
		        return this
		    }
		    BlockBuilder use2(ShortCallback callback) {
		        this.cb2 = callback
		        return this
		    }
		    String toString() { "BlockBuilder($id)" }
		}
		""";

	private static final String SCRIPT = """
		package common.testbundle
		import testsupport.builder.block.BlockBuilder
		class CommonEntrypoint {
		    public static List<String> out = []
		
		    public static final BlockBuilder IMPLICIT = BlockBuilder.create("implicit")
		            .use { out << "implicit" }
		
		    public static final BlockBuilder EXPLICIT = BlockBuilder.create("explicit")
		            .use { a, b, c, d, e -> out << "explicit $a $b $c $d $e" }
		
		    public static final BlockBuilder IT = BlockBuilder.create("it")
		            .use { it -> out << "it " + it }
		
		    public static final BlockBuilder NO_ARGS = BlockBuilder.create("noargs")
		            .use { -> out << "noargs" }
		
		    public static final BlockBuilder DEFAULTS = BlockBuilder.create("defaults")
		            .use2 { a, b, c, d = 99 -> out << "defaults $a $b $c $d" }
		
		    public static void runAll() {
		        IMPLICIT.cb.run('s', 'l', 'p', 'pl', 'h')
		        EXPLICIT.cb.run('a1', 'b2', 'c3', 'd4', 'e5')
		        IT.cb.run('STATE', 'L', 'P', 'PL', 'H')
		        NO_ARGS.cb.run('X', 'Y', 'Z', 'W', 'V')
		        DEFAULTS.cb2.run('x', 'y')
		        [1, 2, 3].each { v -> out << "each:" + v }
		        [1, 2, 3].each { out << "implicitIt:" + it }
		        [1, 2].eachWithIndex { v, i -> out << "index:" + v + "@" + i }
		        [1, 2].each {
		            [10, 20].each { x ->
		                out << "nested:" + it + "/" + x
		            }
		        }
		    }
		}
		""";

	@Test
	void closuresAreLenientAgainstWiderSAMs() throws Exception {
		GroovyClassLoader loader = new GroovyClassLoader(
			LenientClosureTransformerTest.class.getClassLoader(), ScriptConfig.createCompilerConfig());
		try {
			loader.parseClass(SUPPORT);
			Class<?> script = loader.parseClass(SCRIPT);
			Class<?> entry = script.getClassLoader().loadClass("common.testbundle.CommonEntrypoint");
			entry.getMethod("runAll").invoke(null);

			@SuppressWarnings("unchecked")
			List<Object> out = (List<Object>) entry.getField("out").get(null);
			List<String> lines = out.stream().map(String::valueOf).toList();

			assertEquals("implicit", lines.get(0));
			assertEquals("explicit a1 b2 c3 d4 e5", lines.get(1));
			assertEquals("it STATE", lines.get(2));
			assertEquals("noargs", lines.get(3));
			assertEquals("defaults x y null 99", lines.get(4));
			assertEquals(List.of("each:1", "each:2", "each:3"), lines.subList(5, 8));
			assertEquals(List.of("implicitIt:1", "implicitIt:2", "implicitIt:3"), lines.subList(8, 11));
			assertEquals(List.of("index:1@0", "index:2@1"), lines.subList(11, 13));
			assertEquals(List.of("nested:1/10", "nested:1/20", "nested:2/10", "nested:2/20"), lines.subList(13, 17));
			assertEquals(17, lines.size());
		} finally {
			loader.close();
		}
	}
}
