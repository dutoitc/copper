package ch.mno.copper.collect.wrappers;

import ch.mno.copper.collect.connectors.ConnectorException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * A Wrapper should host connector pool instance and queries for later execution.
 * Created by dutoitc on 07.02.2016.
 */
public abstract class AbstractCollectorWrapper {
    public abstract Map<String, String> execute() throws ConnectorException;

    /**
     *
     * @return table, first row is header (column names)
     * @throws ConnectorException
     */
    public abstract List<List<String>> execute2D() throws ConnectorException;


    /** Return AS values, keys */
    public List<String> getAs() {
        return new ArrayList<>();
    }

}
