package ch.mno.copper.collect.wrappers;

import ch.mno.copper.collect.connectors.ConnectorException;
import ch.mno.copper.collect.connectors.SocketConnector;

import java.util.*;

/**
 * Created by dutoitc on 07.02.2016.
 */
// TODO: parse query and store "AS xxx " values in 'as'
public class SocketCollectorWrapper implements AbstractCollectorWrapper {

    private final String host;
    private final int port;
    private final int timeoutMS;
    private final List<String> as;

    public SocketCollectorWrapper(String host, int port, int timeoutMs, String as) {
        this.host = host;
        this.port =port;
        this.timeoutMS = timeoutMs;
        this.as = Arrays.asList(as);
    }

    @Override
    public List<String> getAs() {
        return as;
    }


    @Override
    public Map<String, String> execute() throws ConnectorException {
        try (
                SocketConnector sc = new SocketConnector(host, port, timeoutMS);
        ) {
            SocketConnector.CONNECTION_CHECK status = sc.checkConnection();

            Map<String, String> map = new HashMap<>(2);
            map.put(as.get(0), status.toString());
            return map;
        }
    }

    @Override
    public List<List<String>> execute2D()  {
        try (
                SocketConnector sc = new SocketConnector(host, port, timeoutMS);
        ) {
            SocketConnector.CONNECTION_CHECK status = sc.checkConnection();

            List<List<String>> lst = new ArrayList<>();
            List<String> values = new ArrayList<>();
            values.add(status.toString());
            lst.add(values);
            return lst;
        }
    }


}
