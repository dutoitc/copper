package ch.mno.copper.collect;

import ch.mno.copper.collect.connectors.HttpConnector;
import ch.mno.copper.collect.connectors.HttpResponseData;
import ch.mno.copper.collect.connectors.JmxConnector;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
import net.minidev.json.JSONArray;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MalformedObjectNameException;
import javax.management.ReflectionException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
            path = url.substring(url.indexOf(urlObj.getPath())); // Hack to have path, query and hash
        } catch (MalformedURLException e) {
            throw new RuntimeException(e.getMessage());
        }

        List<String> results=null;
        try {
            if (username==null) {
                conn = new HttpConnector(host, port, null);
            } else {
                conn = new HttpConnector(host, port, username, password);
            }


            HttpResponseData<String> data = conn.get2(path);

            results = extractValues(data, valuesKept);
        } catch (Exception e) {
            LOG.error("Connector exception (server {}): {}", url, e.getMessage());
            if (LOG.isTraceEnabled()) {
                e.printStackTrace();
            }
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

    static List<String> extractValues(HttpResponseData<String> data, List<Pair<String, String>> valuesKept) {
        List<String> results = new ArrayList(valuesKept.size());
        for (Pair<String, String> value: valuesKept) {
            String key = value.getKey();
            if ("responseCode".equals(key)) {
                results.add(String.valueOf(data.getResponseCode()));
            } else if ("contentLength".equals(key)) {
                results.add(String.valueOf(data.getContentLength()));}
            else if ("contentType".equals(key)) {
                results.add(String.valueOf(data.getContentType()));
            } else if ("*".equals(key) || "body".equals(key)) {
                results.add(data.getData());
            } else if (key.startsWith("regexp:")) {
                Matcher matcher = Pattern.compile(key.substring(7)).matcher(data.getData());
                if (matcher.find()) {
                    results.add(matcher.group("capture"));
                } else {
                    //System.out.println("Not found " + key.substring((7)) + " in " + store.getData());
                    results.add("?");
                }
            } else {
                try {
                    Object o = JsonPath.read(data.getData(), key);
                    if (o instanceof JSONArray) {
                        net.minidev.json.JSONArray res = (JSONArray) o;
                        if (res == null || res.size() == 0) {
                            LOG.info("Warning: jsonpath " + key + " not found in " + data);
                            results.add("ERR_NOT_FOUND");
                        } else if (res.size() > 1) {
                            results.add("TOO_MUCH_VALUES_FOUND");
                        } else {
                            results.add(res.get(0).toString());
                        }
                    } else if (o instanceof String) {
                        results.add((String) o);
                    } else if (o==null) {
                        results.add("null");
                    } else {
                        results.add(o.toString());
                    }
                } catch (PathNotFoundException e) {
                    LOG.error("JsonPath not found: " + key);
                    results.add("?");
                }
            }
        }
        return results;
    }

}

