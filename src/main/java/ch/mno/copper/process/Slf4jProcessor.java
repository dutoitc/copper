package ch.mno.copper.process;

import ch.mno.copper.report.Slf4jReporter;
import ch.mno.copper.store.ValuesStore;

import java.util.Collection;
import java.util.List;

/**
 * Created by dutoitc on 02.02.2016.
 */
public class Slf4jProcessor extends AbstractProcessor {

    Slf4jReporter reporter;

    public Slf4jProcessor(String name, List<String> valuesTrigger) {
        super(valuesTrigger);
        reporter = new Slf4jReporter(name);
    }

    @Override
    public void trig(ValuesStore valueStore, Collection<String> changedValueKeys) {
        var sb = new StringBuilder();
        var label = "Values changed: ";
        sb.append(label);
        changedValueKeys.forEach(key -> {
                    if (sb.length() > label.length()) {
                        sb.append(',');
                    }
                    sb.append(key).append('=').append(valueStore.getValue(key));
                }
        );
        if (sb.length() > label.length()) {
            reporter.report(sb.toString(), null);
        }
    }
}
