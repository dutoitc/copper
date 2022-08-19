package ch.mno.copper.collect.connectors;

import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Created by dutoitc on 15.02.2016.
 */
class JdbcConnectorTest {

    @Test
    void test1() throws Exception {
        Class.forName("org.apache.derby.jdbc.EmbeddedDriver");
        String url = "jdbc:derby:memory:sampleDB;create=true";
        Connection conn = DriverManager.getConnection(url);
        Statement stmnt = conn.createStatement();
        try {
            stmnt.executeUpdate("drop table test_table");
        } catch (SQLException e) {
            // Pass
        }
        stmnt.executeUpdate("create table test_table(value int)");
        stmnt.executeUpdate("insert into test_table(value) values (42)");

        JdbcConnector conn1 = new JdbcConnector("jdbc:derby:memory:sampleDB", null, null);
        List<List<String>> res = conn1.query("select * from test_table");
        assertEquals(2, res.size());
        assertEquals(1, res.get(0).size());
        assertEquals("VALUE", res.get(0).get(0));
        assertEquals("42", res.get(1).get(0));
        conn1.close();
    }


}
