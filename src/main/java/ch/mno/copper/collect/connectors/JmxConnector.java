package ch.mno.copper.collect.connectors;

import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by dutoitc on 29.01.2016.
 */
public class JmxConnector extends AbstractConnector {

    private JMXConnector jmxc;
    private MBeanServerConnection mbsc;


    public JmxConnector(String url, String username, String password) throws IOException {
        JMXServiceURL jmxServiceURL = new JMXServiceURL(url);
        Map<String, String[]> env = new HashMap<>();
        String[] creds = {username, password};
        env.put(JMXConnector.CREDENTIALS, creds);
        jmxc = JMXConnectorFactory.connect(jmxServiceURL, env);
        mbsc = jmxc.getMBeanServerConnection();
    }

    public JmxConnector(String url) throws IOException {
        JMXServiceURL jmxServiceURL = new JMXServiceURL(url);
        JMXConnector jmxc = JMXConnectorFactory.connect(jmxServiceURL, null);
        mbsc = jmxc.getMBeanServerConnection();
    }

    public String getObject(String objectName, String attribute) throws MalformedObjectNameException, AttributeNotFoundException, MBeanException, ReflectionException, InstanceNotFoundException, IOException {
        ObjectName objectName1 = new ObjectName(objectName);
        Object value = mbsc.getAttribute(objectName1, attribute);
        return String.valueOf(value);
    }


    @Override
    public void close() {
        try {
            if (jmxc!=null) {
                jmxc.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
