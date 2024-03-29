package ch.mno.copper.collect.collectors;

import ch.mno.copper.collect.connectors.ConnectorException;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Created by dutoitc on 26.03.2016.
 */
class JdbcCollectorTest {

    // TODO: builder test

    @Test
    void test1() throws Exception {
        Class.forName("org.apache.derby.jdbc.EmbeddedDriver").newInstance();
        String url = "jdbc:derby:memory:sampleDB;create=true";
        Connection conn = DriverManager.getConnection(url);
        Statement stmnt = conn.createStatement();
        try {
            stmnt.executeUpdate("drop table test_table");
        } catch (Exception e) {
            // OK
        }
        stmnt.executeUpdate("create table test_table(value int)");
        stmnt.executeUpdate("insert into test_table(value) values (42)");

        JdbcCollector coll = new JdbcCollector();
        List<List<String>> res = coll.query("jdbc:derby:memory:sampleDB", null, null, "select * from test_table");
        assertEquals(2, res.size());
        assertEquals(1, res.get(0).size());
        assertEquals("VALUE", res.get(0).get(0));
        assertEquals("42", res.get(1).get(0));
    }

    @Test
    void testErr() throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        Class.forName("org.apache.derby.jdbc.EmbeddedDriver").newInstance();
        assertThrows(ConnectorException.class, () -> {
            JdbcCollector coll = new JdbcCollector();
            coll.query("jdbc:dummy", null, null, "select * from test_table");
        });
    }

}
