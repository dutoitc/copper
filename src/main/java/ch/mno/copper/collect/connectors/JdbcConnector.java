package ch.mno.copper.collect.connectors;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import oracle.jdbc.OracleDriver;

/**
 * Created by dutoitc on 31.01.2016.
 */
public class JdbcConnector implements AutoCloseable {

    private Connection connection;

    public JdbcConnector(String connParam, String username, String password) throws ConnectorException {
        try {
            connection = getConnection(connParam, username, password);
        } catch (SQLException e) {
            throw new ConnectorException("Cannot connect to " + connParam + ": " + e.getMessage(), e);
        }
    }


    /**
     * @param connParam e.g. jdbc:oracle:thin:@localhost:1521:xe
     * @param username
     * @param password
     * @return
     * @throws SQLException
     */
    public Connection getConnection(String connParam, String username, String password) throws SQLException {
        if (connParam.contains("oracle")) {
            DriverManager.registerDriver (new OracleDriver());
        }
        return DriverManager.getConnection(connParam, username, password);
    }

    public List<List<String>> query(String sql) throws ConnectorException {
        List<List<String>> table = new ArrayList<>();
        try (
                Statement stmt = connection.createStatement();
                ResultSet rs = stmt.executeQuery(sql);) {


            // Copy columns name
            int nbColumns = rs.getMetaData().getColumnCount();
            List<String> row = new ArrayList<>(nbColumns);
            for (int i=0; i<nbColumns; i++) {
                row.add(rs.getMetaData().getColumnName(i+1));
            }
            table.add(row);

            while (rs.next()) {
                List<String> rowResult = new ArrayList<>(nbColumns);
                for (int i = 0; i < nbColumns; i++) {
                    String value = rs.getString(i+1);
                    rowResult.add(value);
                }
                table.add(rowResult);
            }
        } catch (SQLException e) {
            throw new ConnectorException("SQL Exception: "+ e.getMessage(), e);
        }
        return table;
    }

    @Override
    public void close() throws Exception {
        try {
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws ConnectorException {
        new JdbcConnector("jdbc:oracle:thin:@my-defcon:1521:myinstance", "auser", "apass").query("select 1 from dual");
    }

}
