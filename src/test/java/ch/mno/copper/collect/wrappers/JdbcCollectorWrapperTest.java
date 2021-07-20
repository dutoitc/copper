package ch.mno.copper.collect.wrappers;

import ch.mno.copper.collect.connectors.ConnectorException;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class JdbcCollectorWrapperTest {

    @Test
    void testAll() throws ConnectorException {
        String sql = "select 1 as as1, 2 as as2 from dual";
        JdbcCollectorWrapper wrapper = new JdbcCollectorWrapper("url", "user", "pass", sql) {
            List<List<String>> queryValues() {
                var lst = new ArrayList<List<String>>();
                lst.add(Arrays.asList("head1", "head2"));
                lst.add(Arrays.asList("value1", "value2"));
                return lst;
            }
        };
        assertEquals("url", wrapper.getUrl());
        assertEquals("user", wrapper.getUsername());
        assertEquals("pass", wrapper.getPassword());
        assertEquals("[as1, as2]", wrapper.getAs().toString());
        assertEquals(sql, wrapper.getQuery());

        // Run
        var ret = wrapper.execute();
        assertEquals(2, ret.keySet().size());
        assertEquals("value1", ret.get("head1"));
        assertEquals("value2", ret.get("head2"));
    }

    @Test
    void testNoValues() throws ConnectorException {
        String sql = "select 1,2 from dual";
        JdbcCollectorWrapper wrapper = new JdbcCollectorWrapper("url", "user", "pass", sql) {
            List<List<String>> queryValues() {
                var lst = new ArrayList<List<String>>();
                lst.add(Arrays.asList("head1", "head2"));
                return lst;
            }
        };

        // Run
        var ret = wrapper.execute();
        assertEquals(0, ret.keySet().size());
    }

    @Test
    void testTooMuchValuesReturnedMustThrowException() {
        String sql = "select 1,2 from dual";
        JdbcCollectorWrapper wrapper = new JdbcCollectorWrapper("url", "user", "pass", sql) {
            List<List<String>> queryValues() {
                var lst = new ArrayList<List<String>>();
                lst.add(Arrays.asList("head1", "head2", "head3"));
                lst.add(Arrays.asList("value1", "value2", "value3"));
                lst.add(Arrays.asList("value1", "value2"));
                return lst;
            }
        };
        assertThrows(RuntimeException.class, ()->wrapper.execute());
    }

    @Test
    void testExecute2D() throws ConnectorException {
        String sql = "select 1,2 from dual";
        JdbcCollectorWrapper wrapper = new JdbcCollectorWrapper("url", "user", "pass", sql) {
            List<List<String>> queryValues() {
                var lst = new ArrayList<List<String>>();
                lst.add(Arrays.asList("head1", "head2"));
                lst.add(Arrays.asList("value1", "value2"));
                return lst;
            }
        };
        assertEquals("[[head1, head2], [value1, value2]]", wrapper.execute2D().toString());
    }

}
