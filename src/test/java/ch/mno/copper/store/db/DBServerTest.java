package ch.mno.copper.store.db;

import ch.mno.copper.CopperApplication;
import ch.mno.copper.store.StoreValue;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.sql.SQLException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {
        CopperApplication.class
}, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT) // NONE
class DBServerTest {

    Instant i3 = Instant.parse("2015-10-21T07:27:58.00Z");
    Instant i4 = Instant.parse("2015-10-21T07:27:59.00Z");
    Instant i5 = Instant.parse("2015-10-21T07:28:00.00Z");
    Instant i6 = Instant.parse("2015-10-21T07:28:01.00Z");
    Instant i7 = Instant.parse("2015-10-21T07:28:02.00Z");
    Instant i8 = Instant.parse("2015-10-21T07:28:03.00Z");
    Instant i9 = Instant.parse("2045-10-21T07:28:00.00Z");

    @Autowired
    private DBServer server;

    @BeforeAll
    public static void beforeClass() {
        System.setProperty("logging.config", "classpath:logback-test.xml");
    }

    @BeforeEach
    void init() {
        server.clearAllData();
        server.insert("key1", "value10", i5);
        server.insert("key2", "value20", i5);
        server.insert("key3", "value30", i4);
        server.insert("key3", "value31", i7);
        server.insert("key4", "value40", i4);
        server.insert("key4", "value41", i5);
        server.insert("key4", "value42", i6);
    }

    @Test
    void testReadLatestForOneValue() {
        StoreValue readSV = server.readLatest("key1");
        assertEquals("value10", readSV.getValue());
    }


    @Test
    void testReadLatestForTwoValues()  {
        StoreValue readSV = server.readLatest("key3");
        assertEquals("value31", readSV.getValue());
    }

    @Test
    void testReadLatestForThreeValues()  {
        StoreValue readSV = server.readLatest("key4");
        assertEquals("value42", readSV.getValue());
    }


    @Test
    void testReadAtTimestampForOneValue()  {
        assertNull(server.read("key1", i3));
        assertNull(server.read("key1", i4));
        assertEquals("value10", server.read("key1", i5).getValue());
        assertEquals("value10", server.read("key1", i6).getValue());
        assertEquals("value10", server.read("key1", i7).getValue());
        assertEquals("value10", server.read("key1", i8).getValue());
        assertEquals("value10", server.read("key1", i9).getValue());
    }

    @Test
    void testReadAtTimestampForTwoValues()  {
        assertNull(server.read("key3", i3));
        assertEquals("value30", server.read("key3", i4).getValue());
        assertEquals("value30", server.read("key3", i5).getValue());
        assertEquals("value30", server.read("key3", i6).getValue());
        assertEquals("value31", server.read("key3", i7).getValue());
        assertEquals("value31", server.read("key3", i8).getValue());
        assertEquals("value31", server.read("key3", i9).getValue());
    }

    @Test
    void testReadAtTimestampForThreeValues()  {
        assertNull(server.read("key4", i3));
        assertEquals("value40", server.read("key4", i4).getValue());
        assertEquals("value41", server.read("key4", i5).getValue());
        assertEquals("value42", server.read("key4", i6).getValue());
        assertEquals("value42", server.read("key4", i7).getValue());
        assertEquals("value42", server.read("key4", i8).getValue());
        assertEquals("value42", server.read("key4", i9).getValue());
    }

    @Test
    void testReadHistorizedForOneValue() throws SQLException {
        assertTrue(server.read("key1", i3, i4, 100).isEmpty());
        assertTrue(server.read("key1", i4, i5, 100).isEmpty());
        assertOneValue(server.read("key1", i5, i6, 100), "value10");
        assertOneValue(server.read("key1", i6, i7, 100), "value10");
        assertOneValue(server.read("key1", i5, i7, 100), "value10");
        assertOneValue(server.read("key1", i4, i7, 100), "value10");
    }

    @Test
    void testReadHistorizedForTwoValues() throws SQLException {
        assertTrue(server.read("key3", i3, i4, 100).isEmpty());
        assertOneValue(server.read("key3", i4, i5, 100), "value30");
        assertOneValue(server.read("key3", i5, i6, 100), "value30");
        assertOneValue(server.read("key3", i6, i7, 100), "value30");
        assertOneValue(server.read("key3", i4, i7, 100), "value30");
        assertOneValue(server.read("key3", i3, i7, 100), "value30");
        assertOneValue(server.read("key3", i7, i8, 100), "value31");
        assertOneValue(server.read("key3", i8, i9, 100), "value31");
        assertTwoValues(server.read("key3", i3, i8, 100), "value30", "value31");
    }

    //@Test
    /*void testReadInstantValues()  {
        List<InstantValues> iv = server.readInstant(Arrays.asList("key4"), i3, i8, 2);
        assertEquals(4, iv.size());
        // TODO: continue
    }*/

    @Test
    void testReadUpdatedKeys()  {
        assertEquals("", StringUtils.join(server.readUpdatedKeys(i3, i4), ','));
        assertEquals("key3,key4", StringUtils.join(server.readUpdatedKeys(i4, i5), ','));
        assertEquals("key1,key2,key4", StringUtils.join(server.readUpdatedKeys(i5, i6), ','));
        assertEquals("key4", StringUtils.join(server.readUpdatedKeys(i6, i7), ','));
        assertEquals("key3", StringUtils.join(server.readUpdatedKeys(i7, i8), ','));
        assertEquals("", StringUtils.join(server.readUpdatedKeys(i8, i9), ','));
        assertEquals("key1,key2,key3,key4", StringUtils.join(server.readUpdatedKeys(i4, i7), ','));
    }

    @Test
    void testReadLatest()  {
        StringBuilder sb = new StringBuilder();
        server.readLatest().forEach(v -> sb.append(v.getKey()).append(':').append(v.getValue()).append(';'));
        assertEquals("key1:value10;key2:value20;key3:value31;key4:value42;", sb.toString());
    }

    @Test
    void testDeleteValuesOfKey()  {
        assertEquals("value31", server.readLatest("key3").getValue());
        assertEquals("value42", server.readLatest("key4").getValue());
        assertEquals(2, server.deleteValuesOfKey("key3"));
        assertNull(server.readLatest("key3"));
        assertEquals("value42", server.readLatest("key4").getValue());
    }

    @Test
        // Note: need some improvments; do not delete when dateTo is specified (=last values)
    void deleteValuesOlderThanXDays()  {
        assertEquals("value31", server.readLatest("key3").getValue());
        assertEquals("value42", server.readLatest("key4").getValue());
        assertEquals(3, server.deleteValuesOlderThanXDays(1));
    }

    @Test
    void findAlerts() {
        for (int i = 0; i < 100; i++) {
            server.insert("key3", "value35-" + i, i7);
            server.insert("key4", "value42-" + i, i6);
        }
        assertEquals("key4:103\n" +
                "key3:102\n", server.findAlerts());
    }

    @Test
    @Disabled("to be reworked: timestamp not supported in recursive query")
    void testReadInstant() {
        Instant instant = Instant.parse("2020-10-21T00:00:00.00Z");
        for (int i = 0; i < 60; i++) {
            server.insert("key187", "" + i, instant.plus(i, ChronoUnit.MINUTES));
        }
        Instant from = Instant.parse("2020-10-21T00:09:59.00Z");
        Instant to = Instant.parse("2020-10-21T00:19:59.00Z");

        assertEquals(10, server.readInstant(Arrays.asList("key187"), from, to, 60, 10000).size());
    }


    // 1000->17s/23ms/22ms
    // 10000->428s/60ms/58ms
    @Test
    @Disabled("to be reworked")
    void testPerformance()  {
        long tstart = System.currentTimeMillis();
        server.clearAllData();
        long t0 = System.currentTimeMillis();
        for (int i = 0; i < 10000; i++) {
            server.insert("keyPerf", "value" + i, Instant.now());
        }
        System.out.println("Insertion took " + (System.currentTimeMillis() - t0) / 1000 + "s");

        t0 = System.currentTimeMillis();
        server.readLatest("keyPerf");
        System.out.println("Read latest by key took " + (System.currentTimeMillis() - t0) + "ms");

        t0 = System.currentTimeMillis();
        server.readLatest();
        System.out.println("Read latest took " + (System.currentTimeMillis() - t0) + "ms");

        long dt = System.currentTimeMillis() - tstart;
        Assertions.assertTrue(dt < 20 * 1000, "Too long: " + dt / 1000 + "s");
    }

    @Test
    void testDateLastCheck() {
        server.clearAllData();
        Instant i1 = Instant.parse("2021-09-30T16:42:00.00Z");
        Instant i2 = Instant.parse("2021-09-30T16:42:03.00Z");
        Instant i3 = Instant.parse("2021-09-30T16:42:06.00Z");
        server.insert("k1", "1", i1);
        assertEquals("[StoreValue{key='k1', value='1', timestampFrom=2021-09-30T16:42:00Z, timestampTo=3000-12-31T00:00:00Z, timestampLast=2021-09-30T16:42:00Z, nbValues=-1}]", server.readAll("k1").toString());
        server.insert("k1", "1", i2);
        assertEquals("[StoreValue{key='k1', value='1', timestampFrom=2021-09-30T16:42:00Z, timestampTo=3000-12-31T00:00:00Z, timestampLast=2021-09-30T16:42:03Z, nbValues=-1}]", server.readAll("k1").toString());
        server.insert("k1", "2", i3);
        assertEquals("[StoreValue{key='k1', value='1', timestampFrom=2021-09-30T16:42:00Z, timestampTo=2021-09-30T16:42:06Z, timestampLast=2021-09-30T16:42:03Z, nbValues=-1}, " +
                "StoreValue{key='k1', value='2', timestampFrom=2021-09-30T16:42:06Z, timestampTo=3000-12-31T00:00:00Z, timestampLast=2021-09-30T16:42:06Z, nbValues=-1}]", server.readAll("k1").toString());
        assertEquals("[" +
                        "InstantValues{timestamp=2021-09-30T16:42:00Z, values={k1=StoreValue{key='k1', value='1', timestamp=2021-09-30T16:42:00Z}}}, " +
                        "InstantValues{timestamp=2021-09-30T16:42:01Z, values={k1=StoreValue{key='k1', value='1', timestamp=2021-09-30T16:42:01Z}}}, " +
                        "InstantValues{timestamp=2021-09-30T16:42:02Z, values={k1=StoreValue{key='k1', value='1', timestamp=2021-09-30T16:42:02Z}}}, " +
                        "InstantValues{timestamp=2021-09-30T16:42:03Z, values={k1=StoreValue{key='k1', value='1', timestamp=2021-09-30T16:42:03Z}}}, " +
                        "InstantValues{timestamp=2021-09-30T16:42:04Z, values={k1=StoreValue{key='k1', value='1', timestamp=2021-09-30T16:42:04Z}}}, " +
                        "InstantValues{timestamp=2021-09-30T16:42:05Z, values={k1=StoreValue{key='k1', value='1', timestamp=2021-09-30T16:42:05Z}}}, " +
                        "InstantValues{timestamp=2021-09-30T16:42:06Z, values={k1=StoreValue{key='k1', value='2', timestamp=2021-09-30T16:42:06Z}}}]",
                server.readInstant(List.of("k1"), i1, i3, 1, 10).toString());
    }

    @Test
    void testDelete() {
        server.clearAllData();
        Instant i1 = Instant.parse("2021-09-30T16:42:00.00Z");
        Instant i2 = Instant.parse("2021-09-30T16:42:03.00Z");
        server.insertForTests("K1", "V1", i1, DBServer.INSTANT_MAX, i1);
        server.insertForTests("K2", "V2", i1, DBServer.INSTANT_MAX, i1);
        server.insertForTests("K2", "V3", i1, DBServer.INSTANT_MAX, i2);
        assertEquals(1, server.readAll("K1").size());
        assertEquals(2, server.readAll("K2").size());

        server.deleteDuplicates();
        assertEquals(1, server.readAll("K1").size());
        assertEquals(1, server.readAll("K2").size());
    }


    private void assertOneValue(List<StoreValue> values, String value) {
        assertNotNull(values);
        assertEquals(1, values.size());
        assertEquals(value, values.get(0).getValue());
    }

    private void assertTwoValues(List<StoreValue> values, String value1, String value2) {
        assertNotNull(values);
        assertEquals(2, values.size());
        assertEquals(value1, values.get(0).getValue());
        assertEquals(value2, values.get(1).getValue());
    }

    

/*
    @Test
    void testAll()  {
        DbHelper.clearAllData();

        // Create
        Instant i0 = Instant.ofEpochSecond(Instant.now().getEpochSecond()); // Get rid of millis
        DbHelper.insert("key1", "value10", i0);
        DbHelper.insert("key2", "value20", i0);
        DbHelper.insert("key3", "value30", i0);

        // Read simple values
//        DbHelper.dumpForTests();
        StoreValue readSV = DbHelper.readLatest("key1");
        assertEquals("value10", readSV.getValue());
        readSV = DbHelper.readLatest("key2");
        assertEquals("value20", readSV.getValue());
        readSV = DbHelper.readLatest("key3");
        assertEquals("value30", readSV.getValue());
        assertEquals(i0, readSV.getTimestampFrom());
        assertEquals(i9, readSV.getTimestampTo());

//        DbHelper.dumpForTests();

        // Read in the past
        readSV = DbHelper.readInstant("key1", Instant.ofEpochSecond(i0.getEpochSecond()-1));
        assertNull(readSV);

        // Read in the present
        readSV = DbHelper.readInstant("key1", i0);
        assertEquals("value10", readSV.getValue());

        // Read in the future
        readSV = DbHelper.readInstant("key1", Instant.ofEpochSecond(i0.getEpochSecond()+1));
        assertEquals("value10", readSV.getValue());

        // Insert
        Instant i2 = Instant.ofEpochSecond(i0.getEpochSecond() + 2);
        DbHelper.insert("key1", "value11", i2);

        // Read latest
//        DbHelper.dumpForTests();
        readSV = DbHelper.readLatest("key1");
        assertEquals("value11", readSV.getValue());

        // Read all
        List<StoreValue> list = DbHelper.readInstant("key1", Instant.MIN, Instant.MAX);
        assertEquals(2, list.size());
        assertEquals(i0, list.get(0).getTimestampFrom());
        assertEquals(i2, list.get(0).getTimestampTo());
        assertEquals(i2, list.get(1).getTimestampFrom());
        assertEquals(i9, list.get(1).getTimestampTo());
        assertEquals(3, DbHelper.readLatest().size());

        // Read by time interval
        assertEquals(0, DbHelper.readInstant("key1", Instant.ofEpochSecond(i0.getEpochSecond()-1), i0).size());
        assertEquals(1, DbHelper.readInstant("key1", i0, Instant.ofEpochSecond(i0.getEpochSecond()+1)).size());
        assertEquals(1, DbHelper.readInstant("key1", i0, Instant.ofEpochSecond(i0.getEpochSecond()+2)).size());
        assertEquals(2, DbHelper.readInstant("key1", i0, Instant.ofEpochSecond(i0.getEpochSecond()+3)).size());
        assertEquals(2, DbHelper.readInstant("key1", i0, Instant.ofEpochSecond(i0.getEpochSecond()+3)).size());
        assertEquals(2, DbHelper.readInstant("key1", Instant.ofEpochSecond(i0.getEpochSecond()+1), Instant.ofEpochSecond(i0.getEpochSecond()+3)).size());
        assertEquals(1, DbHelper.readInstant("key1", Instant.ofEpochSecond(i0.getEpochSecond()+2), Instant.ofEpochSecond(i0.getEpochSecond()+3)).size());

        // Read changed keys
        DbHelper.dumpForTests();
        assertEquals(3, DbHelper.readUpdatedKeys(i0,i2).size());
        assertEquals(1, DbHelper.readUpdatedKeys(i2,i9).size());

        // Read at some time
        assertNull(DbHelper.readInstant("key1", Instant.ofEpochSecond(i0.getEpochSecond()-1)));
        assertEquals("value10", DbHelper.readInstant("key1", Instant.ofEpochSecond(i0.getEpochSecond())).getValue());
        assertEquals("value10", DbHelper.readInstant("key1", Instant.ofEpochSecond(i0.getEpochSecond()+1)).getValue());
        assertEquals("value11", DbHelper.readInstant("key1", Instant.ofEpochSecond(i0.getEpochSecond()+2)).getValue());

        // Insert impossible (in the past)
        try {
            DbHelper.insert("key1", "valueNot", Instant.ofEpochSecond(i0.getEpochSecond() + 1));
            fail("Insertion in the past must be impossible");
        } catch (Exception e) {
            // Pass
        }
    }
    */

}
