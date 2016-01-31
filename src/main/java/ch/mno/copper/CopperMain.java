package ch.mno.copper;

import ch.mno.copper.collect.HttpCollector;
import ch.mno.copper.collect.JmxCollector;
import ch.mno.copper.collect.connectors.ConnectorException;

import java.util.List;

/**
 * Created by dutoitc on 29.01.2016.
 */
public class CopperMain {

    public static void main(String[] args) throws ConnectorException {
        ValuesStore valuesStore = new ValuesStore();

        // Collect
        String url="service:jmx:rmi:///jndi/rmi://localhost:9999/jmxrmi";
        List<String> res = JmxCollector.jmxQuery(url, new JmxCollector.JmxQuery("java.lang:type=Runtime", "SpecName"), new JmxCollector.JmxQuery("java.lang:type=Runtime", "SpecVersion"));
        res.forEach(s->System.out.println("Found: " + s));
        //valuesStore.put("JAVA_VERSION", res.get(1));

        HttpCollector.httpQuery("http://www.shimbawa.ch", "/files/pong1", "/files/pong2", "/none").forEach(s->System.out.println("Found: " + s));

        // Collectors
        // .stream()
        // .filter(CollectorsFilter)
        // .forEach(c->c.process(valuesStore));

        // Processors
        // .stream()
        // .filter(ProcessorFilter(valuesStore)) // if changed
        // .process(valuesStore)

        // Reporters
        // .stream()
        // .filter(ReportersFilter(valueStore)) // if changed
        // .process(reporter)
    }
}
