package ch.mno.copper.store.db;

import ch.mno.copper.store.StoreValue;
import org.apache.commons.lang3.StringUtils;
import org.junit.*;

import java.sql.SQLException;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Created by dutoitc on 29.09.2017.
 */
@Ignore // FIXME problem of port already taken while executing on server
public class DbValueStoreTest {

    Instant i3 = Instant.parse("2015-10-21T07:27:48.00Z");
    Instant i4 = Instant.parse("2015-10-21T07:27:49.00Z");
    Instant i5 = Instant.parse("2015-10-21T07:28:00.00Z");
    Instant i6 = Instant.parse("2015-10-21T07:28:01.00Z");
    Instant i7 = Instant.parse("2015-10-21T07:28:02.00Z");
    Instant i8 = Instant.parse("2015-10-21T07:28:03.00Z");
    Instant i9 = Instant.parse("2045-10-21T07:28:00.00Z");

    private DBValuesStore store;
    private DBServer server;

    @Before
    public void init() throws SQLException {
        DBServerManual.DBURL= "jdbc:h2:./copperdbtst";
        server = new DBServerManual(false, 12345);
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

    @After
    public void done() throws Exception {
        server.close();
    }


    @Test
    public void testGetValue() {
        Assert.assertEquals("value42", store.getValue("key4"));
    }

    @Test
    public void testGetValues() {
        Assert.assertEquals(5, store.getValues().size());
    }

    @Test
    public void testGetValuesMapString() {
        Map<String, String> map = store.getValuesMapString();
        StringBuilder sb = new StringBuilder();
        map.forEach((key, value)->sb.append(key).append('=').append(value).append('\n'));
        Assert.assertEquals("key1=value10\n" +
                "key2=value20\n" +
                "key3=value31\n" +
                "key4=value42\n" +
                "key10=value78\n", sb.toString());
    }

    @Test
    public void testQueryValues() {
        Collection<String> values = store.queryValues(Instant.parse("2015-10-21T07:27:59.99Z"), Instant.parse("2015-10-21T07:28:00.99Z"));
        Assert.assertEquals("[key1, key2, key4]", StringUtils.join(values));
    }

    @Test
    public void testQueryValues2() {
        List<StoreValue> values = store.queryValues(Instant.parse("2015-10-21T07:27:59.99Z"), Instant.parse("2015-10-21T07:28:00.99Z"), Arrays.asList("key2"), 7);
        Assert.assertEquals(1, values.size());
        Assert.assertEquals("key2", values.get(0).getKey());
    }


//    @Test
//    @Ignore // FIXME comprehension problem or bug ?
//    public void testQueryValues3() {
//        List<InstantValues> values = store.queryValues(Instant.parse("2015-10-21T07:27:50.00Z"), Instant.parse("2015-10-21T07:28:07.99Z"), 1, Arrays.asList("key2"), 7);
//        Assert.assertEquals(4, values.size());
//        Assert.assertEquals("key4", values.get(0).getValues().size());
//        Assert.assertEquals("value40", values.get(0).getValues().get(0).getValue());
//        Assert.assertEquals("key4", values.get(1).getValues().size());
//        Assert.assertEquals("value41", values.get(1).getValues().get(0).getValue());
//        Assert.assertEquals("key4", values.get(2).getValues().size());
//        Assert.assertEquals("value42", values.get(2).getValues().get(0).getValue());
//        Assert.assertEquals("key4", values.get(3).getValues().size());
//        Assert.assertEquals("value42", values.get(3).getValues().get(0).getValue());
//    }


}
