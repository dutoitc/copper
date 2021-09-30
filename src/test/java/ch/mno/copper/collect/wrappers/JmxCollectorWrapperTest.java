package ch.mno.copper.collect.wrappers;

import ch.mno.copper.collect.collectors.JmxCollector;
import ch.mno.copper.collect.connectors.ConnectorException;
import ch.mno.copper.helpers.NotImplementedException;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JmxCollectorWrapperTest {

    @Test
    void testAll() throws ConnectorException {
        List<String> as = Arrays.asList("name1", "name2");
        ArrayList<JmxCollector.JmxQuery> jmxQueries = new ArrayList<>();
        JmxCollectorWrapper wrapper = new JmxCollectorWrapper("url", "user", "pass", jmxQueries, as) {
            List<String> queryValues() {
                return Arrays.asList("value1", "value2");
            }
        };
        assertEquals("url", wrapper.getUrl());
        assertEquals("user", wrapper.getUsername());
        assertEquals("pass", wrapper.getPassword());
        assertEquals(jmxQueries, wrapper.getJmxQueries());
        assertEquals(as, wrapper.getAs());

        // Run
        var ret = wrapper.execute();
        assertEquals(2, ret.keySet().size());
        assertEquals("value1", ret.get("name1"));
        assertEquals("value2", ret.get("name2"));
    }

    @Test
    void testTooMuchValuesReturnedMustThrowException() {
        List<String> as = Arrays.asList("name1", "name2");
        ArrayList<JmxCollector.JmxQuery> jmxQueries = new ArrayList<>();
        JmxCollectorWrapper wrapper = new JmxCollectorWrapper("url", "user", "pass", jmxQueries, as) {
            List<String> queryValues() {
                return Arrays.asList("value1", "value2", "value3");
            }
        };
        assertThrows(RuntimeException.class, ()->wrapper.execute());
    }

    @Test
    void testExecute2DMustThrowException() {
        List<String> as = Arrays.asList("name1", "name2");
        ArrayList<JmxCollector.JmxQuery> jmxQueries = new ArrayList<>();
        JmxCollectorWrapper wrapper = new JmxCollectorWrapper("url", "user", "pass", jmxQueries, as) {
            List<String> queryValues() {
                return Arrays.asList("value1", "value2", "value3");
            }
        };
        assertThrows(NotImplementedException.class, ()->wrapper.execute2D());
    }

}
