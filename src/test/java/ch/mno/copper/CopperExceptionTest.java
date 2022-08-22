package ch.mno.copper;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CopperExceptionTest {

    @Test
    void testMain() {
        try {
            throw new CopperException("aMessage");
        } catch (RuntimeException e) {
            assertEquals("aMessage", e.getMessage());
        }
    }

    @Test
    void testMainWithException() {
        try {
            throw new CopperException("aMessage", new RuntimeException("Subexception"));
        } catch (RuntimeException e) {
            assertEquals("aMessage", e.getMessage());
        }
    }


}
