package ch.mno.copper.collect;

import ch.mno.copper.collect.connectors.ConnectorException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by dutoitc on 07.02.2016.
 */
public class JmxCollectorWrapper extends AbstractCollectorWrapper {

    private String url;
    private String username;
    private String password;
    private List<JmxCollector.JmxQuery> jmxQueries;
    private List<String> as;

    public JmxCollectorWrapper(String url, String username, String password, List<JmxCollector.JmxQuery> jmxQueries, List<String> as) {
        this.url = url;
        this.username =username;
        this.password = password;
        this.jmxQueries = jmxQueries;
        this.as = as;
    }

    @Override
    public Map<String, String> execute() throws ConnectorException {
        List<String> values = JmxCollector.jmxQueryWithCreds(url, username, password, jmxQueries);
        Map<String, String> map = new HashMap();
        if (values.size()!=as.size()) throw new RuntimeException("Wrong values number, expected " + as.size() + ", got " + values.size());
        for (int i=0; i<as.size(); i++) {
            map.put(as.get(i), values.get(i));
        }
        return map;
    }

}
