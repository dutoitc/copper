package ch.mno.copper.store;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class StoreValueTest {

    @Test
    void testAll() {
        Instant instant = Instant.ofEpochSecond(1000000);
        var value = new StoreValue(1l, "key", "10", instant, null, instant, 1);
        assertEquals(1, value.getId());
        assertEquals("key", value.getKey());
        assertEquals("10", value.getValue());
        assertEquals("10", value.getValue());
        assertEquals(1, value.getNbValues());
        assertNull(value.getTimestampTo());
        assertEquals("StoreValue{key='key', value='10', timestampFrom=1970-01-12T13:46:40Z, timestampTo=null, timestampLast=1970-01-12T13:46:40Z, nbValues=1}", value.toString());
    }

}
