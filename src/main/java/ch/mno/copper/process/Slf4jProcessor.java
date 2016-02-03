package ch.mno.copper.process;

import ch.mno.copper.ValuesStore;
import ch.mno.copper.collect.connectors.ConnectorException;
import ch.mno.copper.report.Slf4jReporter;

import java.util.Collection;
import java.util.List;

/**
 * Created by dutoitc on 02.02.2016.
 */
public class Slf4jProcessor extends AbstractProcessor {

    private Slf4jReporter reporter;

    public Slf4jProcessor(String name, List<String> valuesTrigger) {
        super(valuesTrigger);
        reporter = new Slf4jReporter(name);
    }

//    public void trig(String key, String value) {
//    }


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
            try {
                reporter.report(sb.toString(), null);
            } catch (ConnectorException e) {
                e.printStackTrace();
            }
        }
    }
}
