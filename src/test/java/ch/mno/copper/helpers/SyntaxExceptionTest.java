package ch.mno.copper.helpers;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SyntaxExceptionTest {

    @Test
    void testAll() {
        assertEquals("test", new SyntaxException("test").getMessage());
    }

}
