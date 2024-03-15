package ch.mno.copper.report;

import ch.mno.copper.collect.connectors.ConnectorException;
import ch.mno.copper.collect.connectors.HttpConnector;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.Charset;
import java.util.Map;

/**
 *
 */
public class WebexReporter implements AbstractReporter {
    private static final Logger LOG = LoggerFactory.getLogger(WebexReporter.class);

    private static final String URL = "https://webexapis.com/v1/messages";

    @Override
    @SuppressWarnings("java:S2696")
    public void report(String message, Map<String, String> values) throws ConnectorException {
        HttpConnector conn = buildConnector();

        var token = values.get(PARAMETERS.TOKEN.toString());
        var roomId = values.get(PARAMETERS.ROOM_ID.toString());
        var body = "{\"roomId\": \"" + roomId + "\", \"text\":\"" + message + "\"}";

        var post = new HttpPost(URL);
        post.setEntity(new StringEntity(body, Charset.defaultCharset()));
        post.addHeader("Authorization", "Bearer " + token);
        post.addHeader("Content-Type", "application/json");
        var ret = conn.sendPost(post);

        LOG.info("Webex returned {}", ret);
    }

    HttpConnector buildConnector() {
        return new HttpConnector("webexapis.com", 443, "https", null, 3128, "http", null, null);
    }


    public enum PARAMETERS {TOKEN, ROOM_ID}


}
