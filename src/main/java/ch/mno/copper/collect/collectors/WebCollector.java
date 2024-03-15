package ch.mno.copper.collect.collectors;

import ch.mno.copper.collect.connectors.HttpConnector;
import ch.mno.copper.collect.connectors.HttpResponseData;
import ch.mno.copper.collect.connectors.JmxConnector;
import com.google.gson.JsonArray;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONArray;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.*;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Created by dutoitc on 29.01.2016.
 */
@Slf4j
public class WebCollector {

    @SuppressWarnings("java:S2589")
    public static List<String> query(String url, String username, String password, List<Pair<String, String>> valuesKept) {
        String host;
        int port;
        String scheme;
        try {
            var urlObj = new URL(url);
            host = urlObj.getHost();
            port = urlObj.getPort();
//            path = url.substring(url.indexOf(urlObj.getPath())); // Hack to have path, query and hash
            scheme = urlObj.getProtocol();
        } catch (MalformedURLException e) {
            throw new RuntimeException(e.getMessage());
        }

        List<String> results = null;
        try (var conn = buildHttpConnector(username, password, host, port, scheme)) {
            HttpResponseData<String> data = conn.get2(url);

            results = extractValues(data, valuesKept);
        } catch (Exception e) {
            log.error("Connector exception (server {}): {}", url, e.getMessage());
            if (log.isTraceEnabled()) {
                log.trace("Connector error: " + e.getMessage(), e);
            }
            if (results == null) {
                results = new ArrayList<>(valuesKept.size());
            }
            for (int i = results.size(); i < valuesKept.size(); i++) {
                results.add("");
            }
        }
        return results;
    }

    private static HttpConnector buildHttpConnector(String username, String password, String host, int port, String scheme) {
        if (username == null) {
            return new HttpConnector(host, port, scheme);
        }
        return new HttpConnector(host, port, scheme, username, password);
    }

    static List<String> extractValues(HttpResponseData<String> data, List<Pair<String, String>> valuesKept) {
        List<String> results = new ArrayList<>(valuesKept.size());
        for (Pair<String, String> value : valuesKept) {
            String key = value.getKey();
            if ("responseCode".equals(key)) {
                results.add(String.valueOf(data.getResponseCode()));
            } else if ("contentLength".equals(key)) {
                results.add(String.valueOf(data.getContentLength()));
            } else if ("contentType".equals(key)) {
                results.add(String.valueOf(data.getContentType()));
            } else if ("*".equals(key) || "body".equals(key)) {
                results.add(data.getData());
            } else if (key.startsWith("regexp:")) {
                var matcher = Pattern.compile(key.substring(7)).matcher(data.getData());
                if (matcher.find()) {
                    results.add(matcher.group("capture"));
                } else {
                    results.add("?");
                }
            } else {
                addOtherToResult(data, results, key);
            }
        }
        return results;
    }

    @SuppressWarnings("java:S2589")
    private static void addOtherToResult(HttpResponseData<String> data, List<String> results, String key) {
        try {
            Object o = JsonPath.read(data.getData(), key);
            if (o instanceof JSONArray) {
                var res = (JSONArray) o;
                if (res.isEmpty()) {
                    log.info("Warning: jsonpath {} not found in {}", key, data);
                    results.add("ERR_NOT_FOUND");
                } else if (res.size() > 1) {
                    results.add("TOO_MUCH_VALUES_FOUND");
                } else {
                    results.add(res.get(0).toString());
                }
            } else if (o instanceof String) {
                results.add((String) o);
            } else if (o == null) {
                results.add("null");
            } else {
                results.add(o.toString());
            }
        } catch (PathNotFoundException e) {
            log.error("JsonPath not found: {}", key);
            results.add("?");
        }
    }

    public String read(JmxConnector jmxConnector, String objectName, String attribute) throws IOException, MalformedObjectNameException, AttributeNotFoundException, MBeanException, ReflectionException, InstanceNotFoundException {
        return jmxConnector.getObject(objectName, attribute);
    }

}

