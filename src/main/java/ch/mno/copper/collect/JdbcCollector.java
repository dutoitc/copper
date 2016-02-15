package ch.mno.copper.collect;

import ch.mno.copper.collect.connectors.JdbcConnector;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by dutoitc on 10.02.2016.
 */
public class JdbcCollector {

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
        try (JdbcConnector conn = new JdbcConnector(url, username, password)) {
            return conn.query(query);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return table;
    }

}
