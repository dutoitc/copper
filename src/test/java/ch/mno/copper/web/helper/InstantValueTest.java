package ch.mno.copper.web.helper;

import ch.mno.copper.store.data.InstantValue;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertEquals;

class InstantValueTest {

    @Test
    void test1() {
        Instant instant1 = LocalDateTime.parse("2021-07-12T12:34:00").toInstant(ZoneOffset.UTC);
        var obj = new InstantValue(42l, "aKey", "aValue", instant1);
        assertEquals(42l, obj.getId());
        assertEquals("aKey", obj.getKey());
        assertEquals("aValue", obj.getValue());
        assertEquals(instant1, obj.getTimestamp());
        assertEquals("StoreValue{key='aKey', value='aValue', timestamp=2021-07-12T12:34:00Z}", obj.toString());
    }

}
