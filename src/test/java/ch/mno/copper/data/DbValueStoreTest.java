package ch.mno.copper.data;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.sql.SQLException;
import java.time.Instant;

/**
 * Created by dutoitc on 29.09.2017.
 */
public class DbValueStoreTest {

    Instant i3 = Instant.parse("2015-10-21T07:27:48.00Z");
    Instant i4 = Instant.parse("2015-10-21T07:27:49.00Z");
    Instant i5 = Instant.parse("2015-10-21T07:28:00.00Z");
    Instant i6 = Instant.parse("2015-10-21T07:28:01.00Z");
    Instant i7 = Instant.parse("2015-10-21T07:28:02.00Z");
    Instant i8 = Instant.parse("2015-10-21T07:28:03.00Z");
    Instant i9 = Instant.parse("2045-10-21T07:28:00.00Z");

    @Before
    public void init() throws SQLException {
        DbHelper.clearAllData();
        DbHelper.insert("key1", "value10", i5);
        DbHelper.insert("key2", "value20", i5);
        DbHelper.insert("key3", "value30", i4);
        DbHelper.insert("key3", "value31", i7);
        DbHelper.insert("key4", "value40", i4);
        DbHelper.insert("key4", "value41", i5);
        DbHelper.insert("key4", "value42", i6);
    }


    @Test
    public void testGetValue() {
        DbValuesStore store = DbValuesStore.getInstance();
        Assert.assertEquals("value42", store.getValue("key4"));
    }

    @Test
    public void testGetValues() {
        DbValuesStore store = DbValuesStore.getInstance();
        Assert.assertEquals(4, store.getValues().size());
    }


}
