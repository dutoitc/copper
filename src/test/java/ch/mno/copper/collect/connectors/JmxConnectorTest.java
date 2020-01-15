package ch.mno.copper.collect.connectors;

import java.io.IOException;

import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MalformedObjectNameException;
import javax.management.ReflectionException;

import org.junit.Assert;
import org.junit.Test;

import ch.mno.copper.AbstractJmxServerTestStarter;

/**
 * Created by dutoitc on 15.02.2016.
 */
public class JmxConnectorTest extends AbstractJmxServerTestStarter {

    @Test
    public void testX() throws IOException, MalformedObjectNameException, AttributeNotFoundException, MBeanException, ReflectionException, InstanceNotFoundException {
        JmxConnector conn = new JmxConnector("service:jmx:rmi:///jndi/rmi://localhost:"+JMX_PORT+"/server");
        String aValue = conn.getObject("java.lang:type=Runtime", "SpecName");
        Assert.assertTrue(aValue.contains("Java"));
        conn.close();
    }

}
