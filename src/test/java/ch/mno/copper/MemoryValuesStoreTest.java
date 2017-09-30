package ch.mno.copper;

import ch.mno.copper.data.MemoryValuesStore;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

/**
 * Created by dutoitc on 16.02.2016.
 */
public class MemoryValuesStoreTest {

    @Test
    public void testX() throws InterruptedException {
        MemoryValuesStore st = MemoryValuesStore.getInstance();
        st.clear();
        Instant t = Instant.now();
        st.put("key1", "value1");
        Thread.sleep(1);
        Assert.assertEquals(1, st.queryValues(t, Instant.MAX).size());
        t = Instant.now();
        Collection<String> strings = st.queryValues(t, Instant.MAX);
        Assert.assertEquals(1, strings.size());
        Assert.assertTrue(System.currentTimeMillis()-st.getTimestampFrom("key1").toEpochMilli()<1000);
        st.put("key1", "value2");
        Assert.assertEquals(1, st.queryValues(t, Instant.MAX).size());
        Assert.assertEquals("value2", st.getValue("key1"));
        Thread.sleep(1);
        t = Instant.now();
        st.put("key1", "value2");
        Assert.assertEquals(1, st.queryValues(t, Instant.MAX).size()); // Same values
    }

    @Test
    public void testTwice() throws InterruptedException {
        MemoryValuesStore st = MemoryValuesStore.getInstance();
        st.clear();

        st.put("key1", "value2");
        long s1 = st.getTimestampFrom("key1").toEpochMilli();
        Thread.sleep(10);
        st.put("key1", "value2");
        long s2 = st.getTimestampFrom("key1").toEpochMilli();
        Assert.assertEquals(s1, s2);
    }

    @Test
    public void testEmpty() {
        Assert.assertNull(MemoryValuesStore.getInstance().getValue("none"));
//        Assert.assertEquals(-1, MemoryValuesStore.getInstance().getTimestamp("none"));
    }

    @Test
    public void testPutAll() {
        MemoryValuesStore st = MemoryValuesStore.getInstance();
        Instant t = Instant.now();
        st.putAll("key1,key2,key3", Arrays.asList("v1", "v2", "v3"));
        Assert.assertEquals(3, st.queryValues(t, Instant.MAX).size());
        Assert.assertEquals("v1", st.getValue("key1"));
        Assert.assertEquals("v2", st.getValue("key2"));
        Assert.assertEquals("v3", st.getValue("key3"));
        Map<String, String> map = st.getValuesMapString();
        Assert.assertEquals("v1", map.get("key1"));
        Assert.assertEquals("v3", map.get("key3"));
        String str = st.getValues().get("key1").toString();
        Assert.assertTrue(str, str.startsWith("StoreValue{key='key1', value='v1', timestampFrom="));
    }

    @Test
    public void testSerialization() throws IOException {
        Instant t = Instant.now();
        MemoryValuesStore st = new MemoryValuesStore();
        st.putAll("key1,key2,key3", Arrays.asList("v1", "value|222", "v;3"));
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        st.save(baos);
        String out = new String(baos.toByteArray());

        MemoryValuesStore st2 = new MemoryValuesStore();
        st2.load(new ByteArrayInputStream(out.getBytes()));
        Assert.assertEquals(3, st2.getValues().size());
        Assert.assertEquals("v1", st2.getValue("key1"));
        Assert.assertEquals("value|222", st2.getValue("key2"));
        Assert.assertEquals("v;3", st2.getValue("key3"));
        Assert.assertEquals(3, st2.queryValues(t, Instant.MAX).size());
    }

}
