package ch.mno.copper;

import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;

/**
 * Created by dutoitc on 16.02.2016.
 */
public class ValuesStoreTest {

    @Test
    public void testX() {
        ValuesStore st = ValuesStore.getInstance();
        st.put("key1", "value1");
        Assert.assertEquals(1, st.getChangedValues().size());
        Assert.assertEquals(0, st.getChangedValues().size());
        Assert.assertTrue(System.currentTimeMillis()-st.getTimestamp("key1")<1000);
        st.put("key1", "value2");
        Assert.assertEquals(1, st.getChangedValues().size());
        Assert.assertEquals(0, st.getChangedValues().size());
        Assert.assertEquals("value2", st.getValue("key1"));
        st.put("key1", "value2");
        Assert.assertEquals(0, st.getChangedValues().size()); // Same values
    }

    @Test
    public void testEmpty() {
        Assert.assertNull(ValuesStore.getInstance().getValue("none"));
        Assert.assertEquals(-1, ValuesStore.getInstance().getTimestamp("none"));
    }

    @Test
    public void testPutAll() {
        ValuesStore st = ValuesStore.getInstance();
        st.putAll("key1,key2,key3", Arrays.asList("v1", "v2", "v3"));
        Assert.assertEquals(3, st.getChangedValues().size());
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
        ValuesStore st = new ValuesStore();
        st.putAll("key1,key2,key3", Arrays.asList("v1", "value|222", "v;3"));
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        st.save(baos);
        String out = new String(baos.toByteArray());

        ValuesStore st2 = new ValuesStore();
        st2.load(new ByteArrayInputStream(out.getBytes()));
        Assert.assertEquals(3, st2.getValues().size());
        Assert.assertEquals("v1", st2.getValue("key1"));
        Assert.assertEquals("value|222", st2.getValue("key2"));
        Assert.assertEquals("v;3", st2.getValue("key3"));
        Assert.assertEquals(3, st2.getChangedValues().size());
    }

}
