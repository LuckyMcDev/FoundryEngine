package de.luckymcdev.foundryengine.common.bundle.info;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BundleInfoVersionInfoTest {

    @Test
    void versionInfo_Constructor_SetsFields() {
        var vi = new BundleInfo.VersionInfo(1, 2, 3);
        assertEquals(1, vi.major());
        assertEquals(2, vi.minor());
        assertEquals(3, vi.patch());
    }

    @Test
    void versionInfo_ToString() {
        var vi = new BundleInfo.VersionInfo(2, 0, 1);
        assertEquals("2.0.1", vi.toString());
    }

    @ParameterizedTest
    @CsvSource({
            "1.0.0, 1, 0, 0",
            "0.0.1, 0, 0, 1",
            "2.5.3, 2, 5, 3",
            "10.20.30, 10, 20, 30"
    })
    void versionInfo_Parse(String input, int major, int minor, int patch) {
        var vi = BundleInfo.VersionInfo.parse(input);
        assertEquals(major, vi.major());
        assertEquals(minor, vi.minor());
        assertEquals(patch, vi.patch());
    }

    @Test
    void versionInfo_Parse_InvalidFormat_Throws() {
        assertThrows(IllegalArgumentException.class, () -> BundleInfo.VersionInfo.parse("1.0"));
        assertThrows(IllegalArgumentException.class, () -> BundleInfo.VersionInfo.parse("1.0.0.0"));
        assertThrows(IllegalArgumentException.class, () -> BundleInfo.VersionInfo.parse("abc"));
        assertThrows(IllegalArgumentException.class, () -> BundleInfo.VersionInfo.parse(""));
    }

    @Test
    void versionInfo_Parse_NonNumeric_Throws() {
        assertThrows(NumberFormatException.class, () -> BundleInfo.VersionInfo.parse("a.b.c"));
    }

    @Test
    void versionInfo_Equality_Same() {
        var a = new BundleInfo.VersionInfo(1, 0, 0);
        var b = new BundleInfo.VersionInfo(1, 0, 0);
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void versionInfo_Equality_Different() {
        assertNotEquals(
                new BundleInfo.VersionInfo(1, 0, 0),
                new BundleInfo.VersionInfo(1, 0, 1)
        );
    }

    @Test
    void bundleInfo_Record() {
        var vi = new BundleInfo.VersionInfo(1, 0, 0);
        var info = new BundleInfo("test_bundle", "Test Bundle", List.of("Author"), vi, List.of());
        assertEquals("test_bundle", info.id());
        assertEquals("Test Bundle", info.displayName());
        assertEquals(List.of("Author"), info.authors());
        assertEquals(vi, info.versionInfo());
        assertTrue(info.dependencies().isEmpty());
    }

    @Test
    void bundleInfo_ToString() {
        var info = new BundleInfo("id", "Name", List.of("A"), new BundleInfo.VersionInfo(1, 0, 0), List.of());
        assertTrue(info.toString().contains("id"));
        assertTrue(info.toString().contains("Name"));
    }
}
