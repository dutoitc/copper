package ch.mno.copper.report;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by dutoitc on 03.02.2016.
 */
public class Slf4jReporter implements AbstractReporter {


    public Logger logger;

    public Slf4jReporter(String name) {
        logger = LoggerFactory.getLogger(name);
    }

    public void report(String s, Map<String, String> values) {
        logger.info(s);
    }
}
