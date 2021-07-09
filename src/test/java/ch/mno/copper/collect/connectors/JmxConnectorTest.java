package ch.mno.copper.collect.connectors;

import ch.mno.copper.AbstractJmxServerTestStarter;
import org.junit.Assert;
import org.junit.jupiter.api.Test;

import javax.management.*;
import java.io.IOException;

/**
 * Created by dutoitc on 15.02.2016.
 */
public class JmxConnectorTest extends AbstractJmxServerTestStarter {

    @Test
    public void testX() throws IOException, MalformedObjectNameException, AttributeNotFoundException, MBeanException, ReflectionException, InstanceNotFoundException, InterruptedException {
        JmxConnector conn = new JmxConnector("service:jmx:rmi:///jndi/rmi://localhost:"+JMX_PORT+"/server");
        String aValue = conn.getObject("java.lang:type=Runtime", "SpecName");
        Assert.assertTrue(aValue.contains("Java"));
        conn.close();
    }

}
