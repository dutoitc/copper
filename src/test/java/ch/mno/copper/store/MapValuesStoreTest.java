package ch.mno.copper.store;

import ch.mno.copper.helpers.NotImplementedException;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class MapValuesStoreTest {

    @Test
    void testAll() {
        MapValuesStore store = new MapValuesStore();
        store.put("key1", "value1");
        store.put("key1", "value2");
        store.put("key2", "value3");
        store.put("key1", "value4");
        assertEquals("value4", store.getValue("key1"));
        assertNull(store.getValue("key666"));
        assertEquals("[key1, key2]",store.getValues().keySet().toString());
        assertEquals("[key1=value4][key2=value3]", store.getValuesAsString());
        assertEquals("[key1, key2]", store.queryValues(Instant.parse("2000-01-01T00:00:00Z"), Instant.parse("2000-01-02T00:00:00Z")).toString());
        assertEquals("value4", store.queryValues(Instant.parse("2000-01-01T00:00:00Z"), Instant.parse("2000-01-02T00:00:00Z"), null, 1).get(0).getValue());
        assertEquals("[key1]", store.queryValues(Instant.parse("2000-01-01T00:00:00Z"), Instant.parse("2000-01-02T00:00:00Z"), 1, null, 1).get(0).getValues().keySet().toString());
        assertEquals("{key1=value4, key2=value3}", store.getValuesMapString().toString());
        store.deleteValuesOfKey("key2");
        assertEquals("[key1]",store.getValues().keySet().toString());
    }

    @Test
    void load() {
        assertThrows(NotImplementedException.class, ()-> new MapValuesStore().load());
    }

    @Test
    void save() {
        assertThrows(NotImplementedException.class, ()-> new MapValuesStore().save());
    }

    @Test
    void getValuesAlerts() {
        assertNull(new MapValuesStore().getValuesAlerts());
    }

    @Test
    void deleteValuesOlderThanXDays() {
        assertEquals("OK (no historized values in Map Values Store)", new MapValuesStore().deleteValuesOlderThanXDays(1));
    }

}
