package ch.mno.copper.store.db;

import ch.mno.copper.store.StoreValue;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;



/**
 * Created by dutoitc on 29.09.2017.
 */
class DbValueStoreTest {
    Instant i3 = Instant.parse("2015-10-21T07:27:48.00Z");
    Instant i4 = Instant.parse("2015-10-21T07:27:49.00Z");
    Instant i5 = Instant.parse("2015-10-21T07:28:00.00Z");
    Instant i6 = Instant.parse("2015-10-21T07:28:01.00Z");
    Instant i7 = Instant.parse("2015-10-21T07:28:02.00Z");
    Instant i8 = Instant.parse("2015-10-21T07:28:03.00Z");
    Instant i9 = Instant.parse("2045-10-21T07:28:00.00Z");

    private DBValuesStore store;
    private DBServer server;

    @BeforeEach
    void init() throws SQLException, IOException {
        Files.deleteIfExists(Path.of("copperdbtst.mv.db"));
        DBServerManual.DBURL = "jdbc:h2:./copperdbtst";
        server = new DBServerManual(false, 0);
        store = new DBValuesStore(server);
        store.clearAllData();
        store.put("key1", "value10", i5);
        store.put("key2", "value20", i5);
        store.put("key3", "value30", i4);
        store.put("key3", "value31", i7);
        store.put("key4", "value40", i4);
        store.put("key4", "value41", i5);
        store.put("key4", "value42", i6);
        store.put("key10", "value78");
    }

    @AfterEach
    void done() throws Exception {
        server.close();
    }


    @Test
    void testGetValue() {
        assertEquals("value42", store.getValue("key4"));
    }

    @Test
    void testGetValues() {
        assertEquals(5, store.getValues().size());
    }

    @Test
    void testGetValuesMapString() {
        Map<String, String> map = store.getValuesMapString();
        StringBuilder sb = new StringBuilder();
        map.forEach((key, value) -> sb.append(key).append('=').append(value).append('\n'));
        assertEquals("key1=value10\n" +
                "key2=value20\n" +
                "key3=value31\n" +
                "key4=value42\n" +
                "key10=value78\n", sb.toString());
    }

    @Test
    void testQueryValues() {
        Collection<String> values = store.queryValues(Instant.parse("2015-10-21T07:27:59.99Z"), Instant.parse("2015-10-21T07:28:00.99Z"));
        assertEquals("[key1, key2, key4]", StringUtils.join(values));
    }

    @Test
    void testQueryValues2() {
        List<StoreValue> values = store.queryValues(Instant.parse("2015-10-21T07:27:59.99Z"), Instant.parse("2015-10-21T07:28:00.99Z"), Arrays.asList("key2"), 7);
        assertEquals(1, values.size());
        assertEquals("key2", values.get(0).getKey());
    }


//    @Test
//    @Disabled // FIXME comprehension problem or bug ?
//    void testQueryValues3() {
//        List<InstantValues> values = store.queryValues(Instant.parse("2015-10-21T07:27:50.00Z"), Instant.parse("2015-10-21T07:28:07.99Z"), 1, Arrays.asList("key2"), 7);
//        assertEquals(4, values.size());
//        assertEquals("key4", values.get(0).getValues().size());
//        assertEquals("value40", values.get(0).getValues().get(0).getValue());
//        assertEquals("key4", values.get(1).getValues().size());
//        assertEquals("value41", values.get(1).getValues().get(0).getValue());
//        assertEquals("key4", values.get(2).getValues().size());
//        assertEquals("value42", values.get(2).getValues().get(0).getValue());
//        assertEquals("key4", values.get(3).getValues().size());
//        assertEquals("value42", values.get(3).getValues().get(0).getValue());
//    }


}
