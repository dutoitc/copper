package ch.mno.copper.collect;

import ch.mno.copper.collect.connectors.HttpConnector;
import ch.mno.copper.collect.connectors.JmxConnector;
import com.jayway.jsonpath.JsonPath;
import org.apache.commons.lang3.tuple.Pair;
import org.json.simple.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.*;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by dutoitc on 29.01.2016.
 */
public class WebCollector {

    private static Logger LOG = LoggerFactory.getLogger(WebCollector.class);

    public String read(JmxConnector jmxConnector, String objectName, String attribute) throws IOException, MalformedObjectNameException, AttributeNotFoundException, MBeanException, ReflectionException, InstanceNotFoundException {
        return jmxConnector.getObject(objectName, attribute);
    }


    public static List<String> query(String url, String username, String password, List<Pair<String, String>> valuesKept) {
        HttpConnector conn = null;

        String host;
        int port;
        String path;
        try {
            URL urlObj = new URL(url);
            host = urlObj.getHost();
            port = urlObj.getPort();
            path = urlObj.getPath();
        } catch (MalformedURLException e) {
            throw new RuntimeException(e.getMessage());
        }

        List<String> results=null;
        try {
            if (username==null) {
                conn = new HttpConnector(host, port, null);
            } else {
                //conn = new HttpConnector(url, username, password);
                throw new RuntimeException("Username-Password is not yet supported");
            }

            String json = conn.get(path);
            //JSONParser jsonParser = new JSONParser();
            //JSONObject jsonObject = (JSONObject) jsonParser.parse(json);

            results = extractValues(json, valuesKept);
        } catch (Exception e) {
            System.err.println("Connector exception (server " + url+ "): " + e.getMessage());
            if (results==null) {
                results = new ArrayList<>(valuesKept.size());
            }
            for (int i=results.size(); i<valuesKept.size(); i++) {
                results.add("");
            }
        } finally {
            if (conn!=null) {
                conn.close();
            }
        }
        return results;
    }

    static List<String> extractValues(String json, List<Pair<String, String>> valuesKept) {
        List<String> results = new ArrayList(valuesKept.size());
        for (Pair<String, String> value: valuesKept) {
            if ("*".equals(value.getKey())) {
                results.add(json);
            } else {
                net.minidev.json.JSONArray res = JsonPath.read(json, value.getKey());
                if (res == null || res.size() == 0) {
                    LOG.info("Warning: jsonpath " + value.getKey() + " not found in " + json);
                    results.add("ERR_NOT_FOUND");
                } else if (res.size() > 1) {
                    results.add("TOO_MUCH_VALUES_FOUND");
                } else {
                    results.add(res.get(0).toString());
                }
            }
        }
        return results;
    }
}
