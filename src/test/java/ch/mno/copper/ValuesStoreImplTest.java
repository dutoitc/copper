package ch.mno.copper;

import ch.mno.copper.data.ValuesStoreImpl;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

/**
 * Created by dutoitc on 16.02.2016.
 */
public class ValuesStoreImplTest {

    @Test
    public void testX() throws InterruptedException {
        ValuesStoreImpl st = ValuesStoreImpl.getInstance();
        st.clear();
        LocalDateTime t = LocalDateTime.now();
        st.put("key1", "value1");
        Thread.sleep(1);
        Assert.assertEquals(1, st.queryValues(t, LocalDateTime.MAX).size());
        t = LocalDateTime.now();
        Collection<String> strings = st.queryValues(t, LocalDateTime.MAX);
        Assert.assertEquals(0, strings.size());
        Assert.assertTrue(System.currentTimeMillis()-st.getTimestamp("key1")<1000);
        st.put("key1", "value2");
        Assert.assertEquals(1, st.queryValues(t, LocalDateTime.MAX).size());
        Assert.assertEquals("value2", st.getValue("key1"));
        t = LocalDateTime.now();
        st.put("key1", "value2");
        Assert.assertEquals(0, st.queryValues(t, LocalDateTime.MAX).size()); // Same values
    }

    @Test
    public void testTwice() throws InterruptedException {
        ValuesStoreImpl st = ValuesStoreImpl.getInstance();
        st.clear();

        st.put("key1", "value2");
        long s1 = st.getTimestamp("key1");
        Thread.sleep(1);
        st.put("key1", "value2");
        long s2 = st.getTimestamp("key1");
        Assert.assertEquals(s1, s2);
    }

    @Test
    public void testEmpty() {
        Assert.assertNull(ValuesStoreImpl.getInstance().getValue("none"));
        Assert.assertEquals(-1, ValuesStoreImpl.getInstance().getTimestamp("none"));
    }

    @Test
    public void testPutAll() {
        ValuesStoreImpl st = ValuesStoreImpl.getInstance();
        LocalDateTime t = LocalDateTime.now();
        st.putAll("key1,key2,key3", Arrays.asList("v1", "v2", "v3"));
        Assert.assertEquals(3, st.queryValues(t, LocalDateTime.MAX).size());
        Assert.assertEquals("v1", st.getValue("key1"));
        Assert.assertEquals("v2", st.getValue("key2"));
        Assert.assertEquals("v3", st.getValue("key3"));
        Map<String, String> map = st.getValuesMapString();
        Assert.assertEquals("v1", map.get("key1"));
        Assert.assertEquals("v3", map.get("key3"));
        Assert.assertTrue(st.getValues().get("key1").toString().startsWith("StoreValue{value='v1', timestamp="));
    }

    @Test
    public void testSerialization() throws IOException {
        LocalDateTime t = LocalDateTime.now();
        ValuesStoreImpl st = new ValuesStoreImpl();
        st.putAll("key1,key2,key3", Arrays.asList("v1", "value|222", "v;3"));
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        st.save(baos);
        String out = new String(baos.toByteArray());

        ValuesStoreImpl st2 = new ValuesStoreImpl();
        st2.load(new ByteArrayInputStream(out.getBytes()));
        Assert.assertEquals(3, st2.getValues().size());
        Assert.assertEquals("v1", st2.getValue("key1"));
        Assert.assertEquals("value|222", st2.getValue("key2"));
        Assert.assertEquals("v;3", st2.getValue("key3"));
        Assert.assertEquals(3, st2.queryValues(t, LocalDateTime.MAX).size());
    }

}
