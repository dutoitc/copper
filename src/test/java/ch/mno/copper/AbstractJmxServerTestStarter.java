package ch.mno.copper;

import ch.mno.copper.daemon.JMXConnector;
import org.springframework.util.ReflectionUtils;

import java.io.IOException;

public abstract class AbstractJmxServerTestStarter {
    public static final int JMX_PORT = 12345;
    private static JMXConnector jmxConnector;

    public AbstractJmxServerTestStarter() {
        if (jmxConnector == null) {
            try {
                jmxConnector = new JMXConnector(JMX_PORT);
                jmxConnector.startJMX();
            } catch (IOException e) {
                ReflectionUtils.rethrowRuntimeException(e);
            }
        }
    }
}
