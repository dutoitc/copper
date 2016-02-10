package ch.mno.copper.collect;

import ch.mno.copper.collect.connectors.OracleConnector;

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by dutoitc on 10.02.2016.
 */
public class OracleCollector {

    /**
     *
     * @param url
     * @param username
     * @param password
     * @param query
     * @return table, first line holds column names
     */
    public List<List<String>> query(String url, String username, String password, String query) {
        List<List<String>> table = new ArrayList<>(1);
        try (OracleConnector conn = new OracleConnector()) {
            Connection c2 = conn.getConnection(url, username, password);
            ResultSet rs = c2.createStatement().executeQuery(query);
            int nbColumns = rs.getMetaData().getColumnCount();

            // Copy columns name
            List<String> row = new ArrayList<>(nbColumns);
            for (int i=0; i<nbColumns; i++) {
                row.add(rs.getMetaData().getColumnName(i+1));
            }
            table.add(row);

            // Lines
            while(rs.next()) {
                row = new ArrayList<>(nbColumns);
                for (int i=0; i<nbColumns; i++) {
                    row.add(rs.getString(i+1));
                }
                table.add(row);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return table;
    }

}
