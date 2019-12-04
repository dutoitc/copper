package ch.mno.copper.collect;

import ch.mno.copper.collect.connectors.ConnectorException;
import ch.mno.copper.collect.connectors.JdbcConnector;

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
    public List<List<String>> query(String url, String username, String password, String query) throws ConnectorException {
        try (JdbcConnector conn = new JdbcConnector(url, username, password)) {
            return conn.query(query);
        } catch (Exception e) {
            throw new ConnectorException(e.getMessage(), e);
        }
    }

}
