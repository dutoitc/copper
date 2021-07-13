package ch.mno.copper.collect.connectors;

import ch.mno.copper.AbstractJmxServerTestStarter;
import org.junit.jupiter.api.Test;

import javax.management.*;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Created by dutoitc on 15.02.2016.
 */
class JmxConnectorTest extends AbstractJmxServerTestStarter {

    @Test
    void testX() throws IOException, MalformedObjectNameException, AttributeNotFoundException, MBeanException, ReflectionException, InstanceNotFoundException, InterruptedException {
        JmxConnector conn = new JmxConnector("service:jmx:rmi:///jndi/rmi://localhost:" + JMX_PORT + "/server");
        String aValue = conn.getObject("java.lang:type=Runtime", "SpecName");
        assertTrue(aValue.contains("Java"));
        conn.close();
    }

    @Test
    void test2() throws IOException, MalformedObjectNameException, AttributeNotFoundException, MBeanException, ReflectionException, InstanceNotFoundException, InterruptedException {
        JmxConnector conn = new JmxConnector("service:jmx:rmi:///jndi/rmi://localhost:" + JMX_PORT + "/server", "user", "pass");
        String aValue = conn.getObject("java.lang:type=Runtime", "SpecName");
        assertTrue(aValue.contains("Java"));
        conn.close();
    }

}
