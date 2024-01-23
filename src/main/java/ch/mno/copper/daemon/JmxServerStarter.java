package ch.mno.copper.daemon;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Disabled;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.StandardMBean;
import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXConnectorServerFactory;
import javax.management.remote.JMXServiceURL;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.net.MalformedURLException;
import java.net.ServerSocket;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.RMIServerSocketFactory;

/**
 * A JMX Connector service JMX on localhost:jmxPort/server
 */
@Log4j2
@Getter
public class JmxServerStarter implements AutoCloseable {

    private int jmxPort;
    private String jmxURL;
    private JMXConnectorServer jmxConnectorServer;

    public void startJMX() throws IOException {
        MBeanServer server = ManagementFactory.getPlatformMBeanServer();

        // RMIServerSocketFactory personnalisé pour capturer le port dynamique
        var serverSocketFactory = new CustomServerSocketFactory();
        java.rmi.registry.LocateRegistry.createRegistry(0, null, serverSocketFactory);

        // Délai d'attente maximum pour l'initialisation du port (en millisecondes)
        final long timeout = 5000;
        long startTime = System.currentTimeMillis();

        // Attendre que le port soit initialisé ou que le timeout soit atteint
        while (serverSocketFactory.getPort() == 0) {
            if (System.currentTimeMillis() - startTime > timeout) {
                throw new IOException("Timeout atteint lors de l'initialisation du port du registre RMI");
            }
            try {
                Thread.sleep(100); // Pause courte pour permettre l'initialisation du port
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IOException("Thread interrompu lors de l'attente de l'initialisation du port", e);
            }
        }

        jmxPort = serverSocketFactory.getPort();
        jmxURL = "service:jmx:rmi:///jndi/rmi://localhost:" + jmxPort + "/jmxrmi";
        JMXServiceURL url = new JMXServiceURL(jmxURL);
        jmxConnectorServer = JMXConnectorServerFactory.newJMXConnectorServer(url, null, server);
        log.info("Starting JMX on {}", url);
        jmxConnectorServer.start();
        log.info("Started");
    }

    @Override
    public void close() {
        try {
            jmxConnectorServer.stop();
        } catch (IOException e) {
            // Nothing
        }
    }


    @Getter
    static class CustomServerSocketFactory implements RMIServerSocketFactory {
        private int port = 0;

        @Override
        public ServerSocket createServerSocket(int port) throws IOException {
            ServerSocket serverSocket = new ServerSocket(port);
            this.port = serverSocket.getLocalPort();
            return serverSocket;
        }

    }

}