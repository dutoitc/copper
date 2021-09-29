package ch.mno.copper.collect.collectors;

import ch.mno.copper.collect.connectors.JmxConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by dutoitc on 29.01.2016.
 */
public class JmxCollector {

    private static final Logger LOG = LoggerFactory.getLogger(JmxCollector.class);

    public static List<String> jmxQueryWithCreds(String serverUrl, String username, String password, List<JmxQuery> queries) {
        List<String> results = new ArrayList<>(queries.size());
        try (JmxConnector conn = new JmxConnector(serverUrl, username, password)) {
            queries.forEach(aQuery -> collectAndAdd(serverUrl, results, conn, aQuery));
        } catch (Exception e) {
            LOG.error("JmxCollector exception#2 (server {}): {}", serverUrl, e.getMessage());
            for (int i = results.size(); i < queries.size(); i++) {
                results.add("");
            }
        }
        return results;
    }

    private static void collectAndAdd(String serverUrl, List<String> results, JmxConnector conn, JmxQuery aQuery) {
        try {
            results.add(new JmxCollector().read(conn, aQuery.objectName, aQuery.value));
        } catch (Exception e) {
            LOG.error("JmxCollector exception#1 (server {}): {}", serverUrl, e.getMessage());
            results.add("ERR");
        }
    }

    public static List<String> jmxQuery(String serverUrl, List<JmxQuery> queries) {
        return jmxQueryWithCreds(serverUrl, null, null, queries);
    }

    public String read(JmxConnector jmxConnector, String objectName, String attribute) throws IOException, MalformedObjectNameException, AttributeNotFoundException, MBeanException, ReflectionException, InstanceNotFoundException {
        return jmxConnector.getObject(objectName, attribute);
    }

    public static class JmxQuery {

        protected String objectName;
        protected String value;

        public JmxQuery(String objectName, String value) {
            this.objectName = objectName;
            this.value = value;
        }

        public String getObjectName() {
            return objectName;
        }

        public String getValue() {
            return value;
        }
    }

}
