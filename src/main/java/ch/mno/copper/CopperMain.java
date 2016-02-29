package ch.mno.copper;

import ch.mno.copper.collect.CollectorTask;
import ch.mno.copper.collect.connectors.ConnectorException;
import ch.mno.copper.process.AbstractProcessor;
import ch.mno.copper.process.Slf4jProcessor;
import ch.mno.copper.stories.StoriesFacade;
import ch.mno.copper.stories.Story;
import ch.mno.copper.web.WebServer;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by dutoitc on 29.01.2016.
 */
public class CopperMain {

    public static void main(String[] args) throws ConnectorException, IOException, InterruptedException, URISyntaxException {
        // Base: ValuesStore and Grammar
        ValuesStore valuesStore = ValuesStore.getInstance();
        StoriesFacade storiesFacade = StoriesFacade.getInstance();


        // TODO: implement this
        List<AbstractProcessor> processors = Arrays.asList(new Slf4jProcessor("MyLog1", Arrays.asList("*")));


        Thread webserver = new Thread(new WebServer());
        webserver.start();


//        DataProvider dataProvider = ()-> {
//            StoriesFacade.getInstance().refreshFromDisk();
//            return StoriesFacade.getInstance().buildCollectors(valuesStore);
//        };
        CopperDaemon daemon = CopperDaemon.runWith(valuesStore, new DataproviderImpl(), processors);
        while (1 < 2) {
            Thread.sleep(1000);
        }
    }

    /** A provider with data cache. At each call, it will load newer stories from disk, remove older from cache */
    private static class DataproviderImpl implements DataProvider {

        private List<CollectorTask> collectors; // Little hack to avoir recalculation at each call
        private Map<String, CollectorTask> knownStories = new HashMap<>();
        ValuesStore valuesStore;

        public DataproviderImpl() {
            valuesStore = ValuesStore.getInstance();
            StoriesFacade.getInstance().refreshFromDisk();
            collectors = StoriesFacade.getInstance().buildCollectors(valuesStore);
            collectors.forEach(c->knownStories.put(c.storyName(), c));
        }

        @Override
        public List<CollectorTask> getCollectorTasks() {
            StoriesFacade storiesFacade = StoriesFacade.getInstance();
            storiesFacade.refreshFromDisk();
            List<Story> stories = storiesFacade.getStories();

            // Add newer
            stories.stream()
                    .filter(s->!knownStories.containsKey(s.getName()))
                    .forEach(s-> {
                        CollectorTask collector = storiesFacade.buildCollector(s, valuesStore);
                        collectors.add(collector);
                        knownStories.put(s.getName(), collector);
            });

            // Remove older
            Iterator<CollectorTask> it = collectors.iterator();
            while (it.hasNext()) {
                CollectorTask collectorTask = it.next();
                if (!knownStories.containsKey(collectorTask.storyName())) {
                    it.remove();
                }
            }

            return collectors;
        }
    }

}
