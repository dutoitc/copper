package ch.mno.copper.helpers;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNull;

class NotImplementedExceptionTest {

    @Test
    void testAll() {
        assertNull(new NotImplementedException().getMessage());
    }

}
