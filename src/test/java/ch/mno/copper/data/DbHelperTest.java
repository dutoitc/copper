package ch.mno.copper.data;

import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.sql.SQLException;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;

/**
 * Created by dutoitc on 20.09.2017.
 */
public class DbHelperTest {

    Instant i3 = Instant.parse("2015-10-21T07:27:58.00Z");
    Instant i4 = Instant.parse("2015-10-21T07:27:59.00Z");
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
    public void testReadLatestForOneValue() throws SQLException {
        StoreValue readSV = DbHelper.readLatest("key1");
        Assert.assertEquals("value10", readSV.getValue());
    }


    @Test
    public void testReadLatestForTwoValues() throws SQLException {
        StoreValue readSV = DbHelper.readLatest("key3");
        Assert.assertEquals("value31", readSV.getValue());
    }

    @Test
    public void testReadLatestForThreeValues() throws SQLException {
        StoreValue readSV = DbHelper.readLatest("key4");
        Assert.assertEquals("value42", readSV.getValue());
    }


    @Test
    public void testReadAtTimestampForOneValue() throws SQLException {
        Assert.assertNull(DbHelper.read("key1", i3));
        Assert.assertNull(DbHelper.read("key1", i4));
        Assert.assertEquals("value10", DbHelper.read("key1", i5).getValue());
        Assert.assertEquals("value10", DbHelper.read("key1", i6).getValue());
        Assert.assertEquals("value10", DbHelper.read("key1", i7).getValue());
        Assert.assertEquals("value10", DbHelper.read("key1", i8).getValue());
        Assert.assertEquals("value10", DbHelper.read("key1", i9).getValue());
    }

    @Test
    public void testReadAtTimestampForTwoValues() throws SQLException {
        Assert.assertNull(DbHelper.read("key3", i3));
        Assert.assertEquals("value30", DbHelper.read("key3", i4).getValue());
        Assert.assertEquals("value30", DbHelper.read("key3", i5).getValue());
        Assert.assertEquals("value30", DbHelper.read("key3", i6).getValue());
        Assert.assertEquals("value31", DbHelper.read("key3", i7).getValue());
        Assert.assertEquals("value31", DbHelper.read("key3", i8).getValue());
        Assert.assertEquals("value31", DbHelper.read("key3", i9).getValue());
    }

    @Test
    public void testReadAtTimestampForThreeValues() throws SQLException {
        Assert.assertNull(DbHelper.read("key4", i3));
        Assert.assertEquals("value40", DbHelper.read("key4", i4).getValue());
        Assert.assertEquals("value41", DbHelper.read("key4", i5).getValue());
        Assert.assertEquals("value42", DbHelper.read("key4", i6).getValue());
        Assert.assertEquals("value42", DbHelper.read("key4", i7).getValue());
        Assert.assertEquals("value42", DbHelper.read("key4", i8).getValue());
        Assert.assertEquals("value42", DbHelper.read("key4", i9).getValue());
    }

    @Test
    public void testReadHistorizedForOneValue() throws SQLException {
        Assert.assertTrue(DbHelper.read("key1", i3, i4).isEmpty());
        Assert.assertTrue(DbHelper.read("key1", i4, i5).isEmpty());
        assertOneValue(DbHelper.read("key1", i5, i6), "value10");
        assertOneValue(DbHelper.read("key1", i6, i7), "value10");
        assertOneValue(DbHelper.read("key1", i5, i7), "value10");
        assertOneValue(DbHelper.read("key1", i4, i7), "value10");
    }

    @Test
    public void testReadHistorizedForTwoValues() throws SQLException {
        Assert.assertTrue(DbHelper.read("key3", i3, i4).isEmpty());
        assertOneValue(DbHelper.read("key3", i4, i5), "value30");
        assertOneValue(DbHelper.read("key3", i5, i6), "value30");
        assertOneValue(DbHelper.read("key3", i6, i7), "value30");
        assertOneValue(DbHelper.read("key3", i4, i7), "value30");
        assertOneValue(DbHelper.read("key3", i3, i7), "value30");
        assertOneValue(DbHelper.read("key3", i7, i8), "value31");
        assertOneValue(DbHelper.read("key3", i8, i9), "value31");
        assertTwoValues(DbHelper.read("key3", i3, i8), "value30", "value31");
    }

    @Test
    public void testReadInstantValues() throws SQLException {
        List<InstantValues> iv = DbHelper.readInstant(Arrays.asList("key4"), i3, i8, 2);
        Assert.assertEquals(4, iv.size());
        // TODO: continue
    }

    @Test
    public void testReadUpdatedKeys() throws SQLException {
        Assert.assertEquals("", StringUtils.join(DbHelper.readUpdatedKeys(i3, i4), ','));
        Assert.assertEquals("key3,key4", StringUtils.join(DbHelper.readUpdatedKeys(i4, i5), ','));
        Assert.assertEquals("key1,key2,key4", StringUtils.join(DbHelper.readUpdatedKeys(i5, i6), ','));
        Assert.assertEquals("key4", StringUtils.join(DbHelper.readUpdatedKeys(i6, i7), ','));
        Assert.assertEquals("key3", StringUtils.join(DbHelper.readUpdatedKeys(i7, i8), ','));
        Assert.assertEquals("", StringUtils.join(DbHelper.readUpdatedKeys(i8, i9), ','));
        Assert.assertEquals("key1,key2,key3,key4", StringUtils.join(DbHelper.readUpdatedKeys(i4, i7), ','));
    }

    @Test
    public void testReadLatest() throws SQLException {
        StringBuilder sb = new StringBuilder();
        DbHelper.readLatest().forEach(v -> sb.append(v.getKey()).append(':').append(v.getValue()).append(';'));
        Assert.assertEquals("key1:value10;key2:value20;key3:value31;key4:value42;", sb.toString());
    }

    // 1000->17s/23ms/22ms
    // 10000->428s/60ms/58ms
    @Test
    @Ignore
    public void testPerformance() throws SQLException {
        DbHelper.clearAllData();
        long t0 = System.currentTimeMillis();
        for (int i = 0; i < 10000; i++) {
            DbHelper.insert("keyPerf", "value" + i, Instant.now());
        }
        System.out.println("Insertion took " + (System.currentTimeMillis() - t0) / 1000 + "s");

        t0 = System.currentTimeMillis();
        DbHelper.readLatest("keyPerf");
        System.out.println("Read latest by key took " + (System.currentTimeMillis() - t0)  + "ms");

        t0 = System.currentTimeMillis();
        DbHelper.readLatest();
        System.out.println("Read latest took " + (System.currentTimeMillis() - t0)  + "ms");
    }


    private void assertOneValue(List<StoreValue> values, String value) {
        Assert.assertNotNull(values);
        Assert.assertEquals(1, values.size());
        Assert.assertEquals(value, values.get(0).getValue());
    }

    private void assertTwoValues(List<StoreValue> values, String value1, String value2) {
        Assert.assertNotNull(values);
        Assert.assertEquals(2, values.size());
        Assert.assertEquals(value1, values.get(0).getValue());
        Assert.assertEquals(value2, values.get(1).getValue());
    }

    

/*
    @Test
    public void testAll() throws SQLException {
        DbHelper.clearAllData();

        // Create
        Instant i0 = Instant.ofEpochSecond(Instant.now().getEpochSecond()); // Get rid of millis
        DbHelper.insert("key1", "value10", i0);
        DbHelper.insert("key2", "value20", i0);
        DbHelper.insert("key3", "value30", i0);

        // Read simple values
//        DbHelper.dumpForTests();
        StoreValue readSV = DbHelper.readLatest("key1");
        Assert.assertEquals("value10", readSV.getValue());
        readSV = DbHelper.readLatest("key2");
        Assert.assertEquals("value20", readSV.getValue());
        readSV = DbHelper.readLatest("key3");
        Assert.assertEquals("value30", readSV.getValue());
        Assert.assertEquals(i0, readSV.getTimestampFrom());
        Assert.assertEquals(i9, readSV.getTimestampTo());

//        DbHelper.dumpForTests();

        // Read in the past
        readSV = DbHelper.readInstant("key1", Instant.ofEpochSecond(i0.getEpochSecond()-1));
        Assert.assertNull(readSV);

        // Read in the present
        readSV = DbHelper.readInstant("key1", i0);
        Assert.assertEquals("value10", readSV.getValue());

        // Read in the future
        readSV = DbHelper.readInstant("key1", Instant.ofEpochSecond(i0.getEpochSecond()+1));
        Assert.assertEquals("value10", readSV.getValue());

        // Insert
        Instant i2 = Instant.ofEpochSecond(i0.getEpochSecond() + 2);
        DbHelper.insert("key1", "value11", i2);

        // Read latest
//        DbHelper.dumpForTests();
        readSV = DbHelper.readLatest("key1");
        Assert.assertEquals("value11", readSV.getValue());

        // Read all
        List<StoreValue> list = DbHelper.readInstant("key1", Instant.MIN, Instant.MAX);
        Assert.assertEquals(2, list.size());
        Assert.assertEquals(i0, list.get(0).getTimestampFrom());
        Assert.assertEquals(i2, list.get(0).getTimestampTo());
        Assert.assertEquals(i2, list.get(1).getTimestampFrom());
        Assert.assertEquals(i9, list.get(1).getTimestampTo());
        Assert.assertEquals(3, DbHelper.readLatest().size());

        // Read by time interval
        Assert.assertEquals(0, DbHelper.readInstant("key1", Instant.ofEpochSecond(i0.getEpochSecond()-1), i0).size());
        Assert.assertEquals(1, DbHelper.readInstant("key1", i0, Instant.ofEpochSecond(i0.getEpochSecond()+1)).size());
        Assert.assertEquals(1, DbHelper.readInstant("key1", i0, Instant.ofEpochSecond(i0.getEpochSecond()+2)).size());
        Assert.assertEquals(2, DbHelper.readInstant("key1", i0, Instant.ofEpochSecond(i0.getEpochSecond()+3)).size());
        Assert.assertEquals(2, DbHelper.readInstant("key1", i0, Instant.ofEpochSecond(i0.getEpochSecond()+3)).size());
        Assert.assertEquals(2, DbHelper.readInstant("key1", Instant.ofEpochSecond(i0.getEpochSecond()+1), Instant.ofEpochSecond(i0.getEpochSecond()+3)).size());
        Assert.assertEquals(1, DbHelper.readInstant("key1", Instant.ofEpochSecond(i0.getEpochSecond()+2), Instant.ofEpochSecond(i0.getEpochSecond()+3)).size());

        // Read changed keys
        DbHelper.dumpForTests();
        Assert.assertEquals(3, DbHelper.readUpdatedKeys(i0,i2).size());
        Assert.assertEquals(1, DbHelper.readUpdatedKeys(i2,i9).size());

        // Read at some time
        Assert.assertNull(DbHelper.readInstant("key1", Instant.ofEpochSecond(i0.getEpochSecond()-1)));
        Assert.assertEquals("value10", DbHelper.readInstant("key1", Instant.ofEpochSecond(i0.getEpochSecond())).getValue());
        Assert.assertEquals("value10", DbHelper.readInstant("key1", Instant.ofEpochSecond(i0.getEpochSecond()+1)).getValue());
        Assert.assertEquals("value11", DbHelper.readInstant("key1", Instant.ofEpochSecond(i0.getEpochSecond()+2)).getValue());

        // Insert impossible (in the past)
        try {
            DbHelper.insert("key1", "valueNot", Instant.ofEpochSecond(i0.getEpochSecond() + 1));
            Assert.fail("Insertion in the past must be impossible");
        } catch (Exception e) {
            // Pass
        }
    }
    */

}
