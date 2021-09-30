package ch.mno.copper.store.data;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertEquals;

class InstantValuesTest {

    @Test
    void testAll() {
        InstantValues values = new InstantValues();
        values.put("key1", buildValue(1, "aKey1", "aValue1", "2021-07-12T12:34:00"));
        values.put("key1", buildValue(2, "aKey1", "aValue2", "2021-07-12T12:35:00"));
        values.put("key2", buildValue(3, "aKey2", "aValue3", "2021-07-12T12:36:00"));
        values.put("key1", buildValue(4, "aKey1", "aValue4", "2021-07-12T12:37:00"));
        assertEquals("{key1=StoreValue{key='aKey1', value='aValue4', timestamp=2021-07-12T12:37:00Z}, key2=StoreValue{key='aKey2', value='aValue3', timestamp=2021-07-12T12:36:00Z}}", values.getValues().toString());
        Instant instant = Instant.now();
        values.setTimestamp(instant);
        assertEquals(instant, values.getTimestamp());
    }

    private InstantValue buildValue(long id, String key, String value, String date) {
        Instant instant = LocalDateTime.parse(date).toInstant(ZoneOffset.UTC);
        return new InstantValue(id, key, value, instant);
    }

}
