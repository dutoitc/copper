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
        StringBuffer sb = new StringBuffer();
        sb.append("Values changed: ");
        changedValueKeys.forEach(key -> {
                if (sb.length() > 16) sb.append(',');
                    sb.append(key).append('=').append(valueStore.getValue(key));
                }
        );
        if (sb.length() > 16) {
            logger.info(sb.toString());
        }
    }
}
