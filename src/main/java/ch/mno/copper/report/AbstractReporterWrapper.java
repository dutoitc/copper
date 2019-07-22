package ch.mno.copper.report;

import ch.mno.copper.store.ValuesStore;

import java.util.Map;

/**
 * A Wrapper should host connector pool instance and queries for later execution.
 * Created by dutoitc on 07.02.2016.
 */
public abstract class AbstractReporterWrapper {

    public abstract void execute(Map<String, String> values, ValuesStore valuesStore);

}
