package ch.mno.copper;

import ch.mno.copper.collect.CollectorTask;
import ch.mno.copper.collect.connectors.ConnectorException;
import ch.mno.copper.process.AbstractProcessor;
import ch.mno.copper.process.Slf4jProcessor;
import ch.mno.copper.stories.StoriesFacade;
import ch.mno.copper.web.WebServer;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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


        // Load files: yet use sample values if none is specified
        List<String> files;
        if (args.length==0) {
            files = new ArrayList<String>();
            for (File file: new File("stories").listFiles(f->f.isFile())) {
                files.add("stories/" + file.getName());
            }
        }  else {
            files = Arrays.asList(args);
        }


        for (String filename : files) {
            storiesFacade.buildStory(new FileInputStream(filename), new File(filename).toPath());
        }

        Thread webserver = new Thread(new WebServer());
        webserver.start();

        List<CollectorTask> collectorTasks = StoriesFacade.getInstance().buildCollectors(valuesStore);
        CopperDaemon daemon = CopperDaemon.runWith(valuesStore, collectorTasks, processors);
        while (1 < 2) {
            Thread.sleep(1000);
        }


    }
}
