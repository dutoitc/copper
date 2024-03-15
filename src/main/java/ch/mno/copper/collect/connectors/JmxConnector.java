package ch.mno.copper.collect.connectors;

import lombok.extern.log4j.Log4j2;

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
@Log4j2
public class JmxConnector extends AbstractConnector {

    private JMXConnector jmxc;
    private MBeanServerConnection mbsc;


    public JmxConnector(String url) throws IOException {
        this(url, null, null);
    }

    public JmxConnector(String url, String username, String password) throws IOException {
        var jmxServiceURL = new JMXServiceURL(url);
        buildJmxc(username, password, jmxServiceURL);
        mbsc = jmxc.getMBeanServerConnection();
    }

    private static JMXConnector connectWithTimeout(JMXServiceURL jmxServiceURL, Map<String, String[]> env, long timeoutMSec) throws InterruptedException, ExecutionException, TimeoutException {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<JMXConnector> future = executor.submit(() -> JMXConnectorFactory.connect(jmxServiceURL, env));
        return future.get(timeoutMSec, TimeUnit.MILLISECONDS);
    }

    private void buildJmxc(String username, String password, JMXServiceURL jmxServiceURL) throws IOException {
        if (username == null) {
            log.info("Connecting on " + jmxServiceURL);
            jmxc = JMXConnectorFactory.connect(jmxServiceURL, null);
        } else {
            Map<String, String[]> env = new HashMap<>();
            String[] creds = {username, password};
            env.put(JMXConnector.CREDENTIALS, creds);
            try {
                jmxc = connectWithTimeout(jmxServiceURL, env, 1000 * 5l);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IOException("JMX connection interrupted");
            } catch (ExecutionException e) {
                throw new IOException("JMX connection error: " + e.getMessage());
            } catch (TimeoutException e) {
                throw new IOException("JMX connection error: timeout");
            }
        }
    }

    public String getObject(String objectName, String attribute) throws MalformedObjectNameException, AttributeNotFoundException, MBeanException, ReflectionException, InstanceNotFoundException, IOException {
        var objectName1 = new ObjectName(objectName);
        Object value = mbsc.getAttribute(objectName1, attribute);
        return String.valueOf(value);
    }


    @Override
    public void close() {
        try {
            if (jmxc != null) {
                jmxc.close();
            }
        } catch (IOException e) {
            log.debug("Close error: " + e.getMessage(), e);
        }
    }


}
