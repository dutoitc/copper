package ch.mno.copper.process;

import ch.mno.copper.ValuesStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;

/**
 * Created by dutoitc on 02.02.2016.
 */
public class Slf4jProcessor extends AbstractProcessor {

    public Logger logger;

    public Slf4jProcessor(String name, List<String> valuesTrigger) {
        super(valuesTrigger);
        logger = LoggerFactory.getLogger(name);
    }

    public void trig(String key, String value) {
    }


    @Override
    public void trig(ValuesStore valueStore, Collection<String> changedValueKeys) {
        changedValueKeys.forEach(key->
            logger.info("Value " + key + " has changed: " + valueStore.getValue(key))
        );
    }
}
