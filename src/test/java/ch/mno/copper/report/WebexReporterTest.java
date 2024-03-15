package ch.mno.copper.report;

import ch.mno.copper.collect.connectors.ConnectorException;
import ch.mno.copper.collect.connectors.HttpConnector;
import org.apache.commons.io.IOUtils;
import org.apache.http.client.methods.HttpPost;
import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Created by dutoitc on 26.04.2019.
 */
class WebexReporterTest {

    @Test
    void testAll() throws ConnectorException {
        Map<String, String> values = new HashMap<>();
        values.put(WebexReporter.PARAMETERS.TOKEN.toString(), "1token");
        values.put(WebexReporter.PARAMETERS.ROOM_ID.toString(), "2room");

        try (var conn = Mockito.mock(HttpConnector.class)) {
            var post = ArgumentCaptor.forClass(HttpPost.class);
            Mockito.when(conn.sendPost(post.capture())).thenReturn("Ok");
            WebexReporter reporter = new WebexReporter() {
                HttpConnector buildConnector() {
                    return conn;
                }
            };
            reporter.report("aMessage with {{STATUS}}", values);

            assertEquals("[Authorization: Bearer 1token, Content-Type: application/json]", Arrays.toString(post.getValue().getAllHeaders()));
            assertEquals("{\"roomId\": \"2room\", \"text\":\"aMessage with {{STATUS}}\"}", IOUtils.toString(post.getValue().getEntity().getContent(), StandardCharsets.UTF_8));
        } catch (IOException e) {
            Assert.fail(e.getMessage());
        }
    }

}