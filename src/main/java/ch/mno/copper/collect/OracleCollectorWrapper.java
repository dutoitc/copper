package ch.mno.copper.collect;

import ch.mno.copper.collect.connectors.ConnectorException;
import ch.mno.copper.helpers.SyntaxHelper;
import ch.mno.copper.stories.StoryGrammar;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by dutoitc on 07.02.2016.
 */
public class OracleCollectorWrapper extends AbstractCollectorWrapper {

    private String url;
    private String username;
    private String password;
    private String query;

    public OracleCollectorWrapper(String url, String username, String password, String query ) {
        this.url = url;
        this.username =username;
        this.password = password;
        this.query = query;
    }

    @Override
    public Map<String, String> execute() throws ConnectorException {
        List<List<String>> table = new OracleCollector().query(url, username, password, query);
        if (table.size()==1) {
            return new HashMap<>();// no value
        } else if (table.size()>2) {
            throw new RuntimeException("Too much row for execute() use execute2D()");
        }

        List<String> header = table.get(0);
        List<String> values = table.get(1);
        Map<String, String> map = new HashMap<>(header.size()*4/3);
        for (int i=0; i<header.size(); i++) {
            map.put(header.get(i), values.get(i));
        }
        return map;
    }

    @Override
    public List<List<String>> execute2D() throws ConnectorException {
        return new OracleCollector().query(url, username, password, query);
    }

    public static OracleCollectorWrapper buildCollector(StoryGrammar grammar, String storyGiven) {
        String patternOracle = grammar.getPatternFull("COLLECTOR_ORACLE");
        Matcher matcher = Pattern.compile(patternOracle, Pattern.DOTALL).matcher(storyGiven);
        if (!matcher.find()) {
            int p = storyGiven.indexOf("COLLECTOR_ORACLE");
            if (p > 0) {
                SyntaxHelper.checkSyntax(storyGiven, patternOracle);
            }
            throw new RuntimeException("Cannot find \n   >>>" + patternOracle + "\nin\n   >>>" + storyGiven);
        }

        // TODO: fix below (query)
        String collectorOracleData = matcher.group(0);
        String patSpaceEol = grammar.getPatternFull("SPACE_EOL");
        String patSpace = grammar.getPatternFull("SPACE");
        String patEol = grammar.getPatternFull("EOL");
        Matcher matcher2 = Pattern.compile("url=(.*),.*user=(.*?),.*password=(.*?)" + patEol + "(.*)", Pattern.DOTALL).matcher(collectorOracleData);
        if (matcher2.find()) {
            String url = matcher2.group(1);
            String username = matcher2.group(2);
            String password = matcher2.group(3);
            String queries = matcher2.group(4);

            Matcher matcher3 = Pattern.compile("QUERY (.*?) FOR (.*?)" + patSpace + "AS (.*?)" + patSpaceEol).matcher(queries);
            List<JmxCollector.JmxQuery> jmxQueries = new ArrayList<>();
            List<String> names = new ArrayList<>();
            while (matcher3.find()) {
                String oName = matcher3.group(1);
                String att = matcher3.group(2);
                String name = matcher3.group(3);
                jmxQueries.add(new JmxCollector.JmxQuery(oName, att));
                names.add(name);
            }
            String query="";// TODO
            return new OracleCollectorWrapper(url, username, password, query);
        } else {
            throw new RuntimeException("Cannot read COLLECTOR_ORACLE body in <" + collectorOracleData + ">");
        }
    }

}
