package de.luckymcdev.foundryengine.common.exceptions;

import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;

import static org.junit.jupiter.api.Assertions.*;

class UtilityClassExceptionTest {

    @Test
    void constructor_SetsCorrectMessage() {
        var ex = new UtilityClassException();
        assertEquals("This is a Utility Class and should not be Instantiated", ex.getMessage());
    }

    @Test
    void exception_IsUnsupportedOperationException() {
        assertTrue(new UtilityClassException() instanceof UnsupportedOperationException);
    }

    @Test
    void utilityClass_ConstructorThrows() throws NoSuchMethodException {
        var constructor = Common.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        var ex = assertThrows(InvocationTargetException.class, () -> constructor.newInstance());
        assertTrue(ex.getCause() instanceof UtilityClassException);
        assertEquals("This is a Utility Class and should not be Instantiated", ex.getCause().getMessage());
    }

    private static final class Common {
        private Common() {
            throw new UtilityClassException();
        }
    }
}
