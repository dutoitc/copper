package ch.mno.copper.report;

import ch.mno.copper.collect.connectors.ConnectorException;
import ch.mno.copper.collect.connectors.HttpConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by dutoitc on 31.01.2016.
 */
public class PushoverReporter implements AbstractReporter {
    private static final Logger LOG = LoggerFactory.getLogger(PushoverReporter.class);

    private static final String URL = "https://api.pushover.net/1/messages.json";
    private static final int MAX_MESSAGE_PER_HOUR = 5;
    private static long nbMessageInHour = 0;
    private static long hour = -1;

    @Override
    @SuppressWarnings("java:S2696")
    public void report(String message, Map<String, String> values) throws ConnectorException {
        int currHour = new GregorianCalendar().get(Calendar.HOUR);
        if (currHour == hour) {
            if (nbMessageInHour > MAX_MESSAGE_PER_HOUR) {
                LOG.warn("Too much message for this hour, skipping message: {}", message);
                return;
            }
        } else {
            hour = currHour;
            nbMessageInHour = 0;
        }
        nbMessageInHour++;


        // FIXME: temporary test, create configuration file for this
        HttpConnector conn = new HttpConnector("api.pushover.net", 443, "https", "localhost", 3128, "http", null, null);

        Map<String, String> params = new HashMap<>();
        params.put("token", values.get(PARAMETERS.APPLICATION_TOKEN.toString()));
        params.put("user", values.get(PARAMETERS.DEST.toString()));
        params.put("title", values.get(PARAMETERS.TITLE.toString()));
        params.put("message", message);
        if ("true".equals(values.get(PARAMETERS.HTML.toString()))) params.put("html", "1");
        String ret = conn.post(URL, params);
        LOG.info("Pushover returned {}", ret);
    }


    public enum PARAMETERS {APPLICATION_TOKEN, DEST, TITLE, HTML}


}
