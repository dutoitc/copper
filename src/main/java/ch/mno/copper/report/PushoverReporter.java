package ch.mno.copper.report;

import ch.mno.copper.collect.connectors.ConnectorException;
import ch.mno.copper.collect.connectors.HttpConnector;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by dutoitc on 31.01.2016.
 */
public class PushoverReporter {

    private static final String URL = "https://api.pushover.net/1/messages.json";

    public void send(String applicationToken, String dest, String title, String message, boolean html) throws ConnectorException {
        HttpConnector conn = new HttpConnector("api.pushover.net", 443, "https");
        Map<String, String> params = new HashMap<>();
        params.put("token", applicationToken);
        params.put("user", dest);
        params.put("title", title);
        params.put("message", message);
        if (html) params.put("html", "1");

        String ret=conn.post("/1/messages.json", params);
        System.out.println(ret); // {"status":1,"request":"ead7edb7aa67c0e4502etc..."}
    }

    public static void main(String[] args) throws ConnectorException {
        new PushoverReporter().send("anAppToken", "aUser", "aTitle", "test", false);
    }

}
