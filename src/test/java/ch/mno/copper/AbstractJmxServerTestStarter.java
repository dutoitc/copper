package ch.mno.copper;

import ch.mno.copper.daemon.JmxServerStarter;
import org.springframework.util.ReflectionUtils;

import java.io.IOException;

public abstract class AbstractJmxServerTestStarter {
    private static JmxServerStarter jmxConnector;

    protected int getJmxPort() {
        return jmxConnector.getJmxPort();
    }

    protected String getJmxURL() {
        return jmxConnector.getJmxURL();
    }

    public AbstractJmxServerTestStarter() {
        if (jmxConnector == null) {
            try {
                jmxConnector = new JmxServerStarter();
                jmxConnector.startJMX();
            } catch (IOException e) {
                ReflectionUtils.rethrowRuntimeException(e);
            }
        }
    }
}