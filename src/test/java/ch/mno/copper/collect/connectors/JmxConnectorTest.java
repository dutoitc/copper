package ch.mno.copper.collect.connectors;

import ch.mno.copper.AbstractJmxServerTestStarter;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import javax.management.*;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Created by dutoitc on 15.02.2016.
 */
@Disabled // JAVA17 JMX server blocks, not found why after 2h
class JmxConnectorTest extends AbstractJmxServerTestStarter {

    @Test
    void testX() throws IOException, MalformedObjectNameException, AttributeNotFoundException, MBeanException, ReflectionException, InstanceNotFoundException {
        JmxConnector conn = new JmxConnector(getJmxURL());
        String aValue = conn.getObject("java.lang:type=Runtime", "SpecName");
        assertTrue(aValue.contains("Java"));
        conn.close();
    }

    @Test
    void test2() throws IOException, MalformedObjectNameException, AttributeNotFoundException, MBeanException, ReflectionException, InstanceNotFoundException {
        JmxConnector conn = new JmxConnector(getJmxURL(), "user", "pass");
        String aValue = conn.getObject("java.lang:type=Runtime", "SpecName");
        assertTrue(aValue.contains("Java"));
        conn.close();
    }

}
