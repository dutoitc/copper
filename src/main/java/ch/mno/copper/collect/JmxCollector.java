package ch.mno.copper.collect;

import ch.mno.copper.collect.connectors.ConnectorException;
import ch.mno.copper.collect.connectors.JmxConnector;

import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MalformedObjectNameException;
import javax.management.ReflectionException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by dutoitc on 29.01.2016.
 */
public class JmxCollector {

    public String read(JmxConnector jmxConnector, String objectName, String attribute) throws IOException, MalformedObjectNameException, AttributeNotFoundException, MBeanException, ReflectionException, InstanceNotFoundException {
        return jmxConnector.getObject(objectName, attribute);
    }


    public static List<String> jmxQueryWithCreds(String serverUrl, String username, String password, List<JmxQuery> queries) throws ConnectorException {
        List<String> results = new ArrayList(queries.size());
        try {
            JmxConnector conn;
            if (username==null) {
                conn = new JmxConnector(serverUrl);
            } else {
                conn = new JmxConnector(serverUrl, username, password);
            }

            for (JmxQuery aQuery: queries) {
                results.add(new JmxCollector().read(conn, aQuery.objectName, aQuery.value));
            }
        } catch (Exception e) {
            throw new ConnectorException("Connector exception: " + e.getMessage(), e);
        }
        return results;
    }

    public static List<String> jmxQuery(String serverUrl, List<JmxQuery> queries) throws ConnectorException {
        return jmxQueryWithCreds(serverUrl, null, null, queries);
    }


    public static class JmxQuery {

        private String objectName;
        private String value;

        public JmxQuery(String objectName, String value) {
            this.objectName = objectName;
            this.value = value;
        }
    }


    public static void main(String[] args) throws IOException, MalformedObjectNameException, AttributeNotFoundException, MBeanException, ReflectionException, InstanceNotFoundException {
        try {
            String url="service:jmx:rmi:///jndi/rmi://localhost:9999/jmxrmi";
            List<JmxQuery> queries = Arrays.asList(new JmxQuery("java.lang:type=Runtime", "SpecName"), new JmxQuery("java.lang:type=Runtime", "SpecVersion"));
            List<String> res = JmxCollector.jmxQuery(url, queries);
            System.out.println("Found Java name: " + res.get(0));
            System.out.println("Found Java version: " + res.get(1));
        } catch (ConnectorException e) {
            e.printStackTrace();
        }
    }

}
