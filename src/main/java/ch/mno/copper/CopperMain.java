package ch.mno.copper;

import ch.mno.copper.collect.CollectorTask;
import ch.mno.copper.collect.connectors.ConnectorException;
import ch.mno.copper.process.AbstractProcessor;
import ch.mno.copper.process.Slf4jProcessor;
import ch.mno.copper.stories.Story;
import ch.mno.copper.stories.StoryGrammar;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Created by dutoitc on 29.01.2016.
 */
public class CopperMain {

    public static void main(String[] args) throws ConnectorException, IOException, InterruptedException {
        ValuesStore valuesStore = ValuesStore.getInstance();

        StoryGrammar grammar = new StoryGrammar(new FileInputStream("StoryGrammar.txt"));
        Story story = new Story(grammar, new FileInputStream("samples/jmxStorySimpleCollect.txt"));

        List<AbstractProcessor> processors = Arrays.asList(new Slf4jProcessor("MyLog1", Arrays.asList("*")));
        List<CollectorTask> collectorTasks = Arrays.asList(
                new CollectorTask(() -> {
                    try {
                        Map<String, String> values = story.getCollectorWrapper().execute();
                        values.forEach((key,value)->valuesStore.put(key, value));
                    } catch (ConnectorException e) {
                        e.printStackTrace();
                    }
                }, story.getCron())
        );


        Thread webserver = new Thread(new WebServer());
        webserver.start();


        CopperDaemon daemon = CopperDaemon.runWith(valuesStore, collectorTasks, processors);
        while (1 < 2) {
            Thread.sleep(1000);
        }


        // Collect
//        String url="service:jmx:rmi:///jndi/rmi://localhost:9999/jmxrmi";
//        List<String> res = JmxCollector.jmxQuery(url, new JmxCollector.JmxQuery("java.lang:type=Runtime", "SpecName"), new JmxCollector.JmxQuery("java.lang:type=Runtime", "SpecVersion"));
//        res.forEach(s->System.out.println("Found: " + s));
//        //valuesStore.post("JAVA_VERSION", res.get(1));
//
//        HttpCollector.httpQuery("http://www.shimbawa.ch", "/files/pong1", "/files/pong2", "/none").forEach(s->System.out.println("Found: " + s));

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
