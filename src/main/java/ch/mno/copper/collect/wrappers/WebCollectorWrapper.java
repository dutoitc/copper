package ch.mno.copper.collect.wrappers;

import ch.mno.copper.collect.collectors.WebCollector;
import ch.mno.copper.helpers.NotImplementedException;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by dutoitc on 07.02.2016.
 */
public class WebCollectorWrapper implements AbstractCollectorWrapper {

    protected List<Pair<String, String>> valuesKept;
    private String url;
    private String username;
    private String password;
    private List<String> as;

    public WebCollectorWrapper(String url, String username, String password, List<Pair<String, String>> valuesKept) {
        this.url = url;
        this.username = username;
        this.password = password;
        this.valuesKept = valuesKept;

        this.as = new ArrayList<>();
        for (Pair<String, String> pair : valuesKept) {
            as.add(pair.getKey());
        }
    }

    @Override
    public List<String> getAs() {
        return as;
    }

    public List<Pair<String, String>> getValuesKept() {
        return valuesKept;
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
    public Map<String, String> execute() {
        List<String> values = queryValues();
        Map<String, String> map = new HashMap<>();
        if (values.size() != valuesKept.size()) {
            throw new RuntimeException("Wrong values number, expected " + valuesKept.size() + ", got " + values.size());
        }
        for (var i = 0; i < valuesKept.size(); i++) {
            map.put(valuesKept.get(i).getValue(), values.get(i));
        }
        return map;
    }

    List<String> queryValues() {
        return WebCollector.query(url, username, password, valuesKept);
    }

    @Override
    public List<List<String>> execute2D() {
        throw new NotImplementedException();
    }


}
