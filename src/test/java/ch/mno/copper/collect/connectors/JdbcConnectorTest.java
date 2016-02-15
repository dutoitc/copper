package ch.mno.copper.collect.connectors;

import org.junit.Assert;
import org.junit.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.List;

/**
 * Created by dutoitc on 15.02.2016.
 */
public class JdbcConnectorTest {

    @Test
    public void test1() throws Exception {
        org.apache.derby.jdbc.EmbeddedDriver driver;
        Class.forName("org.apache.derby.jdbc.EmbeddedDriver");
        String url = "jdbc:derby:memory:sampleDB;create=true";
        Connection conn = DriverManager.getConnection(url);
        Statement stmnt = conn.createStatement();
        stmnt.executeUpdate("create table test_table(value int)");
        stmnt.executeUpdate("insert into test_table(value) values (42)");

        JdbcConnector conn1 = new JdbcConnector("jdbc:derby:memory:sampleDB", null, null);
        List<List<String>> res = conn1.query("select * from test_table");
        Assert.assertEquals(2, res.size());
        Assert.assertEquals(1, res.get(0).size());
        Assert.assertEquals("VALUE", res.get(0).get(0));
        Assert.assertEquals("42", res.get(1).get(0));
        conn1.close();
    }


}
