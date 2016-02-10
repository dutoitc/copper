package ch.mno.copper.collect.connectors;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by dutoitc on 31.01.2016.
 */
public class OracleConnector implements AutoCloseable {

    private Connection connection;

    public void OracleConnector(String connParam, String username, String password) throws ConnectorException {
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
        return DriverManager.getConnection(connParam, username, password);
    }

    public List<List<String>> query(String sql) throws ConnectorException {
        List<List<String>> res = new ArrayList<>();
        try (
                Statement stmt = connection.createStatement();
                ResultSet rs = stmt.executeQuery(sql);) {
            while (rs.next()) {
                List<String> rowResult = new ArrayList<>(rs.getMetaData().getColumnCount());
                for (int i = 0; i < rs.getMetaData().getColumnCount(); i++) {
                    String value = rs.getString(i);
                    rowResult.add(value);
                }
                res.add(rowResult);
            }
        } catch (SQLException e) {
            throw new ConnectorException("SQL Exception: "+ e.getMessage(), e);
        }
        return res;
    }

    @Override
    public void close() throws Exception {
        try {
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
