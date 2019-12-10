package ch.mno.copper.daemon;

import javax.management.MBeanServer;
import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXConnectorServerFactory;
import javax.management.remote.JMXServiceURL;
import java.io.IOException;
import java.lang.management.ManagementFactory;

/**
 * A JMX Connector service JMX on localhost:jmxPort/server
 */
public class JmxServerStarter implements AutoCloseable {

    private final int jmxPort;
    private JMXConnectorServer jmxConnectorServer;

    public JmxServerStarter(int jmxPort) {
        this.jmxPort = jmxPort;
    }

    public void startJMX() throws IOException {
        MBeanServer server = ManagementFactory.getPlatformMBeanServer();

        java.rmi.registry.LocateRegistry.createRegistry(jmxPort);
        JMXServiceURL url = new JMXServiceURL("service:jmx:rmi:///jndi/rmi://localhost:" + jmxPort + "/server");
        jmxConnectorServer = JMXConnectorServerFactory.newJMXConnectorServer(url, null, server);
        jmxConnectorServer.start();
    }

    @Override
    public void close() {
        try {
            jmxConnectorServer.stop();
        } catch (IOException e) {
            // Nothing
        }
    }
}
