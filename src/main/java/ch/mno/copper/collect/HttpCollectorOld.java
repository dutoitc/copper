package ch.mno.copper.collect;

import ch.mno.copper.collect.connectors.ConnectorException;
import ch.mno.copper.collect.connectors.HttpConnector;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by dutoitc on 30.01.2016.
 */
public class HttpCollectorOld {

    public String read(HttpConnector conn, String uri) throws ConnectorException {
        return conn.get(uri);
    }

    /**
     *
     * @param serverUrl http://server:port
     * @param uris
     * @return
     * @throws ConnectorException
     */
    public static List<String> httpQuery(String serverUrl, String... uris) throws ConnectorException {
        List<String> results = new ArrayList(uris.length);

        URL uri = null;
        try {
            uri = new URL(serverUrl);
        } catch (MalformedURLException e) {
            throw new ConnectorException("Connector exception: Malformed URL: " + serverUrl + ": " + e.getMessage(), e);
        }
        try (HttpConnector conn =  new HttpConnector(uri.getHost(), uri.getPort(), uri.getProtocol())){
            for (String itUri: uris) {
                results.add(new HttpCollectorOld().read(conn, itUri));
            }
        } catch (Exception e) {
            throw new ConnectorException("Connector exception: " + e.getMessage(), e);
        }
        return results;
    }

//    public static void main(String[] args) throws IOException, MalformedObjectNameException, AttributeNotFoundException, MBeanException, ReflectionException, InstanceNotFoundException {
//        try {
//            HttpCollectorOld.httpQuery("http://www.shimbawa.ch", "/files/pong1", "/files/pong2", "/none").forEach(s->System.out.println("Found: " + s));
//        } catch (ConnectorException e) {
//            e.printStackTrace();
//        }
//    }

}
