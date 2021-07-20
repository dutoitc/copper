package ch.mno.copper.collect.wrappers;

import ch.mno.copper.helpers.NotImplementedException;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class WebCollectorWrapperTest {

    @Test
    void testAll() {
        List<Pair<String, String>> as = new ArrayList<>();
        as.add(Pair.of("as1","as3"));
        as.add(Pair.of("as2","as4"));
        WebCollectorWrapper wrapper = new WebCollectorWrapper("url", "user", "pass", as) {
            List<String> queryValues() {
                return Arrays.asList("value1", "value2");
            }
        };
        assertEquals("url", wrapper.getUrl());
        assertEquals("user", wrapper.getUsername());
        assertEquals("pass", wrapper.getPassword());
        assertEquals("[(as1,as3), (as2,as4)]", wrapper.getValuesKept().toString());
        assertEquals("[as1, as2]", wrapper.getAs().toString());

        // Run
        var ret = wrapper.execute();
        assertEquals(2, ret.keySet().size());
        assertEquals("value1", ret.get("as3"));
        assertEquals("value2", ret.get("as4"));
    }

    @Test
    void testTooMuchValuesReturnedMustThrowException() {
        List<Pair<String, String>> as = new ArrayList<>();
        as.add(Pair.of("as1",null));
        as.add(Pair.of("as2",null));
        WebCollectorWrapper wrapper = new WebCollectorWrapper("url", "user", "pass", as) {
            List<String> queryValues() {
                return Arrays.asList("value1", "value2", "value3");
            }
        };
        assertThrows(RuntimeException.class, ()->wrapper.execute());
    }

    @Test
    void testExecute2DMustThrowException() {
        List<Pair<String, String>> as = new ArrayList<>();
        as.add(Pair.of("as1",null));
        as.add(Pair.of("as2",null));
        WebCollectorWrapper wrapper = new WebCollectorWrapper("url", "user", "pass", as) {
            List<String> queryValues() {
                return Arrays.asList("value1", "value2", "value3");
            }
        };
        assertThrows(NotImplementedException.class, ()->wrapper.execute2D());
    }

}
