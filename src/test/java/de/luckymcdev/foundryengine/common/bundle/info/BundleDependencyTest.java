package de.luckymcdev.foundryengine.common.bundle.info;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class BundleDependencyTest {

    @Test
    void constructor_SetsFields() {
        var dep = new BundleDependency("testmod", "1.0.0", BundleDependency.Type.MOD);
        assertEquals("testmod", dep.id());
        assertEquals("1.0.0", dep.version());
        assertEquals(BundleDependency.Type.MOD, dep.type());
    }

    @Test
    void constructor_BundleType() {
        var dep = new BundleDependency("other_bundle", "2.0.0", BundleDependency.Type.BUNDLE);
        assertEquals("other_bundle", dep.id());
        assertEquals("2.0.0", dep.version());
        assertEquals(BundleDependency.Type.BUNDLE, dep.type());
    }

    @ParameterizedTest
    @CsvSource({
            "mod:jei@1.21, jei, 1.21, MOD",
            "bundle:other@2.0, other, 2.0, BUNDLE",
            "mod:test_only, test_only, any, MOD"
    })
    void parse_ValidInput(String input, String id, String version, String type) {
        BundleDependency dep = BundleDependency.parse(input);
        assertEquals(id, dep.id());
        assertEquals(version, dep.version());
        assertEquals(BundleDependency.Type.valueOf(type), dep.type());
    }

    @Test
    void parse_ModWithoutVersion_UsesAny() {
        BundleDependency dep = BundleDependency.parse("mod:some_mod");
        assertEquals("some_mod", dep.id());
        assertEquals("any", dep.version());
        assertEquals(BundleDependency.Type.MOD, dep.type());
    }

    @Test
    void parse_BundleWithVersion() {
        BundleDependency dep = BundleDependency.parse("bundle:my_bundle@3.0.0");
        assertEquals("my_bundle", dep.id());
        assertEquals("3.0.0", dep.version());
        assertEquals(BundleDependency.Type.BUNDLE, dep.type());
    }

    @Test
    void toString_ModDependency() {
        var dep = new BundleDependency("jei", "1.21", BundleDependency.Type.MOD);
        assertEquals("mod:jei@1.21", dep.toString());
    }

    @Test
    void toString_BundleDependency() {
        var dep = new BundleDependency("other", "2.0.0", BundleDependency.Type.BUNDLE);
        assertEquals("bundle:other@2.0.0", dep.toString());
    }

    @Test
    void equality_SameValues_Equal() {
        var a = new BundleDependency("x", "1.0", BundleDependency.Type.MOD);
        var b = new BundleDependency("x", "1.0", BundleDependency.Type.MOD);
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void equality_DifferentValues_NotEqual() {
        var a = new BundleDependency("x", "1.0", BundleDependency.Type.MOD);
        var b = new BundleDependency("y", "1.0", BundleDependency.Type.MOD);
        assertNotEquals(a, b);
    }

    @Test
    void type_MOD_EnumConstant() {
        assertEquals("MOD", BundleDependency.Type.MOD.name());
    }

    @Test
    void type_BUNDLE_EnumConstant() {
        assertEquals("BUNDLE", BundleDependency.Type.BUNDLE.name());
    }
}
