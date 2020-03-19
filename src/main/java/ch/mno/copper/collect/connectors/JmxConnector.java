package ch.mno.copper.collect.connectors;

import javax.management.*;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;

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
        try {
            jmxc = connectWithTimeout(jmxServiceURL, env, 1000l*5l);
        } catch (InterruptedException e) {
            throw new IOException("JMX connection interrupted");
        } catch (ExecutionException e) {
            throw new IOException("JMX connection error: " + e.getMessage());
        } catch (TimeoutException e) {
            throw new IOException("JMX connection error: timeout");
        }
        mbsc = jmxc.getMBeanServerConnection();
    }

    private static JMXConnector connectWithTimeout(JMXServiceURL jmxServiceURL, Map<String, String[]> env, long timeoutMSec) throws InterruptedException, ExecutionException, TimeoutException {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<JMXConnector> future = executor.submit(() -> JMXConnectorFactory.connect(jmxServiceURL, env));
        return future.get(timeoutMSec, TimeUnit.MILLISECONDS);
    }


    public JmxConnector(String url) throws IOException {
        JMXServiceURL jmxServiceURL = new JMXServiceURL(url);
        jmxc = JMXConnectorFactory.connect(jmxServiceURL, null);
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
