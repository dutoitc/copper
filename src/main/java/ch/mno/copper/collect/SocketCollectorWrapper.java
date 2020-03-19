package ch.mno.copper.collect;

import ch.mno.copper.collect.connectors.ConnectorException;
import ch.mno.copper.collect.connectors.SocketConnector;
import ch.mno.copper.helpers.SyntaxHelper;
import ch.mno.copper.stories.data.StoryGrammar;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by dutoitc on 07.02.2016.
 */
// TODO: parse query and store "AS xxx " values in 'as'
public class SocketCollectorWrapper extends AbstractCollectorWrapper {

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
    public List<List<String>> execute2D() throws ConnectorException {
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

    public static SocketCollectorWrapper buildCollector(StoryGrammar grammar, String storyGiven) {
        String patternJdbc = grammar.getPatternFull("COLLECTOR_SOCKET");
        Matcher matcher = Pattern.compile(patternJdbc, Pattern.DOTALL).matcher(storyGiven);
        if (!matcher.find()) {
            int p = storyGiven.indexOf("COLLECTOR_SOCKET");
            if (p > 0) {
                SyntaxHelper.checkSyntax(grammar, storyGiven, patternJdbc);
            }
            throw new RuntimeException("Cannot find \n   >>>" + patternJdbc + "\nin\n   >>>" + storyGiven);
        }

        String collectorSocketData = matcher.group(0);
        String patSpaceEol = grammar.getPatternFull("SPACE_EOL");
        Matcher matcher2 = Pattern.compile("host=(.*),.*port=(.*?),.*timeout_ms=(.*?)" + patSpaceEol + ".*?AS ([a-zA-Z0-9_]+)", Pattern.DOTALL).matcher(collectorSocketData);
        if (matcher2.find()) {
            return buildWrapper(matcher2);
        } else {
            throw new RuntimeException("Cannot readInstant COLLECTOR_SOCKET body in <" + collectorSocketData + ">");
        }
    }

    private static SocketCollectorWrapper buildWrapper(Matcher matcher2) {
        String host = matcher2.group(1);
        int port = Integer.parseInt(matcher2.group(2));
        int timeoutMS = Integer.parseInt(matcher2.group(3));
        String query = matcher2.group(4);
        return new SocketCollectorWrapper(host, port, timeoutMS, query);
    }

}
