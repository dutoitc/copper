package ch.mno.copper.report;

import ch.mno.copper.collect.connectors.ConnectorException;
import ch.mno.copper.collect.connectors.HttpConnector;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by dutoitc on 31.01.2016.
 */
public class PushoverReporter implements AbstractReporter  {

    private static final String URL = "https://api.pushover.net/1/messages.json";
    public static enum PARAMETERS {APPLICATION_TOKEN, DEST, TITLE, HTML};

//    public void send(String applicationToken, String dest, String title, String message, boolean html) throws ConnectorException {
//
//    }

//    public static void main(String[] args) throws ConnectorException {
//        new PushoverReporter().send("anAppToken", "aUser", "aTitle", "test", false);
//    }

    @Override
    public void report(String message, Map<String, String> values) throws ConnectorException {
        HttpConnector conn = new HttpConnector("api.pushover.net", 443, "https");
        Map<String, String> params = new HashMap<>();
        params.put("token", values.get(PARAMETERS.APPLICATION_TOKEN));
        params.put("user", values.get(PARAMETERS.DEST));
        params.put("title", values.get(PARAMETERS.TITLE));
        params.put("message", message);
        if ("true".equals(values.get(PARAMETERS.HTML))) params.put("html", "1");

        String ret=conn.post("/1/messages.json", params);
        System.out.println(ret); // {"status":1,"request":"ead7edb7aa67c0e4502etc..."}
    }
}
