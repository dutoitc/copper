package ch.mno.copper;

import ch.mno.copper.collect.CollectorTask;

import java.util.List;

/**
 * Created by dutoitc on 25.02.2016.
 */
public interface DataProvider {

    List<CollectorTask> getCollectorTasks();

}
