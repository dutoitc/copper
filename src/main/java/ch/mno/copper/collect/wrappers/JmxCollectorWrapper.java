package ch.mno.copper.collect.wrappers;

import ch.mno.copper.collect.collectors.JmxCollector;
import ch.mno.copper.collect.connectors.ConnectorException;
import ch.mno.copper.helpers.NotImplementedException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by dutoitc on 07.02.2016.
 */
public class JmxCollectorWrapper implements AbstractCollectorWrapper {

    private String url;
    private String username;
    private String password;
    protected List<JmxCollector.JmxQuery> jmxQueries;
    protected List<String> as;

    public JmxCollectorWrapper(String url, String username, String password, List<JmxCollector.JmxQuery> jmxQueries, List<String> as) {
        this.url = url;
        this.username =username;
        this.password = password;
        this.jmxQueries = jmxQueries;
        this.as = as;
    }

    public List<String> getAs() {
        return as;
    }

    public List<JmxCollector.JmxQuery> getJmxQueries() {
        return jmxQueries;
    }

    public String getUrl() {
        return url;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    @Override
    public Map<String, String> execute() throws ConnectorException {
        List<String> values = queryValues();
        Map<String, String> map = new HashMap<>();
        if (values.size()!=as.size()) {
            throw new RuntimeException("Wrong values number, expected " + as.size() + ", got " + values.size());
        }
        for (int i=0; i<as.size(); i++) {
            map.put(as.get(i), values.get(i));
        }
        return map;
    }

    List<String> queryValues() throws ConnectorException {
        return JmxCollector.jmxQueryWithCreds(url, username, password, jmxQueries);
    }

    @Override
    public List<List<String>> execute2D() {
        throw new NotImplementedException();
    }

}
