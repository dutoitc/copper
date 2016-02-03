package ch.mno.copper.report;

import ch.mno.copper.collect.connectors.ConnectorException;

import java.util.Map;

/**
 * Created by dutoitc on 03.02.2016.
 */
public interface AbstractReporter {

    void report(String message, Map<String, String> values) throws ConnectorException;

}
