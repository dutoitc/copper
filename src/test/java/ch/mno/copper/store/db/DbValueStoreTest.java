package ch.mno.copper.store.db;

import org.junit.*;

import java.sql.SQLException;
import java.time.Instant;

/**
 * Created by dutoitc on 29.09.2017.
 */
@Ignore
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
        Assert.assertEquals(4, store.getValues().size());
    }


}
