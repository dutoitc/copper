package ch.mno.copper;

import ch.mno.copper.collect.AbstractCollectorWrapper;
import ch.mno.copper.collect.CollectorTask;
import ch.mno.copper.collect.connectors.ConnectorException;
import ch.mno.copper.process.AbstractProcessor;
import ch.mno.copper.process.Slf4jProcessor;
import ch.mno.copper.report.AbstractReporterWrapper;
import ch.mno.copper.stories.Story;
import ch.mno.copper.stories.StoryGrammar;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Created by dutoitc on 29.01.2016.
 */
public class CopperMain {

    public static void main(String[] args) throws ConnectorException, IOException, InterruptedException {
        // Base: ValuesStore and Grammar
        ValuesStore valuesStore = ValuesStore.getInstance();
        StoryGrammar grammar = new StoryGrammar(new FileInputStream("StoryGrammar.txt"));

        // DEBUG TODO: remove this
        valuesStore.put("aKey", "aValue");
        valuesStore.put("aKey2", "aValue2");

        // TODO: implement this
        List<AbstractProcessor> processors = Arrays.asList(new Slf4jProcessor("MyLog1", Arrays.asList("*")));

        List<CollectorTask> collectorTasks = new ArrayList<>();

        // Load files: yet use sample values if none is specified
        List<String> files;
        if (args.length==0) {
            files =Arrays.asList("samples/jmxStorySimpleCollect.txt");
        }  else {
            files = Arrays.asList(args);
        }

        List<Story> stories = new ArrayList<>();
        for (String filename : files) {
            File file = new File(filename);
            Story story = new Story(grammar, new FileInputStream(filename), file.toPath());
            stories.add(story);

            collectorTasks.add(
                    new CollectorTask(() -> {
                        // This code execute at every trigger (cron, ...) for the given story
                        try {
                            Map<String, String> values;
                            AbstractCollectorWrapper collectorWrapper = story.getCollectorWrapper();
                            if (collectorWrapper==null) { // Null means to read value store
                                values = valuesStore.getValuesMapString();
                            } else {
                                values = collectorWrapper.execute();
                            }

                            AbstractReporterWrapper reporter = story.getReporterWrapper();
                            if (reporter==null) {
                                values.forEach((key, value) -> valuesStore.put(key, value));
                            } else {
                                reporter.execute(values);
                            }
                        } catch (ConnectorException e) {
                            e.printStackTrace();
                        }
                    }, story.getCron())
            );

            // TODO: create reporter, and link to values
        }
        CopperMediator.getInstance().setStories(stories);


        Thread webserver = new Thread(new WebServer());
        webserver.start();


        CopperDaemon daemon = CopperDaemon.runWith(valuesStore, collectorTasks, processors);
        while (1 < 2) {
            Thread.sleep(1000);
        }


    }
}
