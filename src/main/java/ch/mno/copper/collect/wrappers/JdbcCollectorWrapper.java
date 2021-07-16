package ch.mno.copper.collect.wrappers;

import ch.mno.copper.collect.collectors.JdbcCollector;
import ch.mno.copper.collect.connectors.ConnectorException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by dutoitc on 07.02.2016.
 */
// TODO: parse query and store "AS xxx " values in 'as'
public class JdbcCollectorWrapper extends AbstractCollectorWrapper {

    private String url;
    private String username;
    private String password;
    private String query;
    private List<String> as;

    public JdbcCollectorWrapper(String url, String username, String password, String query ) {
        this.url = url;
        this.username =username;
        this.password = password;
        this.setQuery(query);
    }

    @Override
    public List<String> getAs() {
        return as;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
        // Parse 'as': in case of errors(timeout, exception), values are put as {KEY}=ERR
        as = new ArrayList<>();
        Matcher matcher = Pattern.compile("[aA][sS] ([a-zA-Z0-9_]+)").matcher(this.query);
        while (matcher.find()) {
            as.add(matcher.group(1));
        }
    }

    @Override
    public Map<String, String> execute() throws ConnectorException {
        List<List<String>> table = new JdbcCollector().query(url, username, password, query);
        if (table.size()==1) {
            return new HashMap<>();// no value
        } else if (table.size()>2) {
            throw new RuntimeException("Too much row for execute() use execute2D()");
        }

        List<String> header = table.get(0);
        List<String> values = table.get(1);
        Map<String, String> map = new HashMap<>(header.size()*4/3);
        for (int i=0; i<header.size(); i++) {
            map.put(header.get(i), values.get(i));
        }
        return map;
    }

    @Override
    public List<List<String>> execute2D() throws ConnectorException {
        return new JdbcCollector().query(url, username, password, query);
    }

    public String getPassword() {
        return password;
    }
}
