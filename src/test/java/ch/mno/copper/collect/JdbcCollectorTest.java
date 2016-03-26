package ch.mno.copper.collect;

import ch.mno.copper.collect.connectors.ConnectorException;
import org.junit.Assert;
import org.junit.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.List;

/**
 * Created by dutoitc on 26.03.2016.
 */
public class JdbcCollectorTest {


    @Test
    public void test1() throws Exception {
        org.apache.derby.jdbc.EmbeddedDriver driver;
        Class.forName("org.apache.derby.jdbc.EmbeddedDriver");
        String url = "jdbc:derby:memory:sampleDB;create=true";
        Connection conn = DriverManager.getConnection(url);
        Statement stmnt = conn.createStatement();
        stmnt.executeUpdate("create table test_table(value int)");
        stmnt.executeUpdate("insert into test_table(value) values (42)");

        JdbcCollector coll = new JdbcCollector();
        List<List<String>> res = coll.query("jdbc:derby:memory:sampleDB", null, null, "select * from test_table");
        Assert.assertEquals(2, res.size());
        Assert.assertEquals(1, res.get(0).size());
        Assert.assertEquals("VALUE", res.get(0).get(0));
        Assert.assertEquals("42", res.get(1).get(0));
    }

    @Test(expected= ConnectorException.class)
    public void testErr() throws ConnectorException {
        JdbcCollector coll = new JdbcCollector();
        coll.query("jdbc:dummy", null, null, "select * from test_table");
    }


}
