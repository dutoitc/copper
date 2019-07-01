package ch.mno.copper.daemon;

import javax.management.MBeanServer;
import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXConnectorServerFactory;
import javax.management.remote.JMXServiceURL;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.net.MalformedURLException;
import java.rmi.RemoteException;

/**
 * A JMX Connector service JMX on localhost:jmxPort/server
 */
public class JMXConnector implements AutoCloseable {

    private final int jmxPort;
    private JMXConnectorServer jmxConnectorServer;

    public JMXConnector(String jmxPort) {
        this.jmxPort = Integer.parseInt(jmxPort);
    }

    public void startJMX() {
        MBeanServer server = ManagementFactory.getPlatformMBeanServer();
        try {
            java.rmi.registry.LocateRegistry.createRegistry(jmxPort);
            JMXServiceURL url = new JMXServiceURL("service:jmx:rmi:///jndi/rmi://localhost:" + jmxPort + "/server");
            jmxConnectorServer = JMXConnectorServerFactory.newJMXConnectorServer(url, null, server);
            jmxConnectorServer.start();
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
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
