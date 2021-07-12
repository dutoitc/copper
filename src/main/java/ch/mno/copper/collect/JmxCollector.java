package ch.mno.copper.collect;

import ch.mno.copper.collect.connectors.ConnectorException;
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

    private static Logger LOG = LoggerFactory.getLogger(JmxCollector.class);

    public String read(JmxConnector jmxConnector, String objectName, String attribute) throws IOException, MalformedObjectNameException, AttributeNotFoundException, MBeanException, ReflectionException, InstanceNotFoundException {
        return jmxConnector.getObject(objectName, attribute);
    }


    public static List<String> jmxQueryWithCreds(String serverUrl, String username, String password, List<JmxQuery> queries) throws ConnectorException {
        List<String> results = new ArrayList<>(queries.size());
        JmxConnector conn = null;
        try {
            if (username==null) {
                conn = new JmxConnector(serverUrl);
            } else {
                conn = new JmxConnector(serverUrl, username, password);
            }

            for (JmxQuery aQuery: queries) {
                try {
                    results.add(new JmxCollector().read(conn, aQuery.objectName, aQuery.value));
                } catch (Exception e) {
                    LOG.error("JmxCollector exception#1 (server {}): {}", serverUrl, e.getMessage());
                    results.add("ERR");
                }
            }
        } catch (Exception e) {
            LOG.error("JmxCollector exception#2 (server {}): {}", serverUrl, e.getMessage());
            for (int i=results.size(); i<queries.size(); i++) {
                results.add("");
            }
        } finally {
            if (conn!=null) {
                conn.close();
            }
        }
        return results;
    }

    public static List<String> jmxQuery(String serverUrl, List<JmxQuery> queries) throws ConnectorException {
        return jmxQueryWithCreds(serverUrl, null, null, queries);
    }

    public static class JmxQuery {

        protected String objectName;
        protected String value;

        public JmxQuery(String objectName, String value) {
            this.objectName = objectName;
            this.value = value;
        }
    }

}
