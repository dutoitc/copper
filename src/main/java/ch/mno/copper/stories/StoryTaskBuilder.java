package ch.mno.copper.stories;

import ch.mno.copper.collect.AbstractCollectorWrapper;
import ch.mno.copper.collect.StoryTask;
import ch.mno.copper.collect.StoryTaskImpl;
import ch.mno.copper.collect.connectors.ConnectorException;
import ch.mno.copper.report.AbstractReporterWrapper;
import ch.mno.copper.store.ValuesStore;
import ch.mno.copper.stories.data.Story;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class StoryTaskBuilder {

    private static Logger LOG = LoggerFactory.getLogger(StoryTaskBuilder.class);

    public static StoryTask build(Story story, ValuesStore valuesStore) {
       return new StoryTaskImpl(story, () -> {
        // This code execute at every trigger (cron, ...) for the given story
        try {
            Map<String, String> values;

            // Collect
            AbstractCollectorWrapper collectorWrapper = story.getCollectorWrapper();
            if (collectorWrapper == null) { // Null means to readInstant value store
                values = valuesStore.getValuesMapString();
            } else {
                values = collectorWrapper.execute();
            }
            if (!story.matchWhen(values, valuesStore)) {
                // Not matching WHEN->ignore report
                return;
            }

            if (values==null) {
                LOG.warn("Story " + story.getName() + " returned null values; trying to continue with valuesStore...");
                values = valuesStore.getValuesMapString();
            }


            // Report
            AbstractReporterWrapper reporter = story.getReporterWrapper();
            if (reporter == null) {
                values.forEach((key, value) -> valuesStore.put(key, value));
            } else {
                reporter.execute(values, valuesStore);
            }
        } catch (ConnectorException e) {
            e.printStackTrace();
        }
    }, story.getCron());
    }

}
