package ch.mno.copper.collect;

import ch.mno.copper.collect.connectors.ConnectorException;
import ch.mno.copper.helpers.SyntaxHelper;
import ch.mno.copper.stories.StoryGrammar;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by dutoitc on 07.02.2016.
 */
public class JmxCollectorWrapper extends AbstractCollectorWrapper {

    private String url;
    private String username;
    private String password;
    private List<JmxCollector.JmxQuery> jmxQueries;
    private List<String> as;

    public JmxCollectorWrapper(String url, String username, String password, List<JmxCollector.JmxQuery> jmxQueries, List<String> as) {
        this.url = url;
        this.username =username;
        this.password = password;
        this.jmxQueries = jmxQueries;
        this.as = as;
    }

    @Override
    public Map<String, String> execute() throws ConnectorException {
        List<String> values = JmxCollector.jmxQueryWithCreds(url, username, password, jmxQueries);
        Map<String, String> map = new HashMap();
        if (values.size()!=as.size()) throw new RuntimeException("Wrong values number, expected " + as.size() + ", got " + values.size());
        for (int i=0; i<as.size(); i++) {
            map.put(as.get(i), values.get(i));
        }
        return map;
    }

    @Override
    public List<List<String>> execute2D() throws ConnectorException {
        throw new NotImplementedException();
    }

    public static JmxCollectorWrapper buildCollector(StoryGrammar grammar, String storyGiven) {
        // Temp: JMX
        String patternJMX = grammar.getPatternFull("COLLECTOR_JMX");
        Matcher matcher = Pattern.compile(patternJMX, Pattern.DOTALL).matcher(storyGiven);
        if (!matcher.find()) {
            int p = storyGiven.indexOf("COLLECTOR JMX");
            if (p > 0) {
                SyntaxHelper.checkSyntax(grammar, storyGiven, patternJMX);
            }
            throw new RuntimeException("Cannot find \n   >>>" + patternJMX + "\nin\n   >>>" + storyGiven);
        }
        //
        String collectorJmxData = matcher.group(0);
        String patSpaceEol = grammar.getPatternFull("SPACE_EOL");
        String patSpace = grammar.getPatternFull("SPACE");
        String patEol = grammar.getPatternFull("EOL");
        Matcher matcher2 = Pattern.compile("url=(.*),.*user=(.*?),.*password=(.*?)" + patEol + "(.*)", Pattern.DOTALL).matcher(collectorJmxData);
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
            return new JmxCollectorWrapper(url, username, password, jmxQueries, names);
        } else {
            throw new RuntimeException("Cannot read COLLECTOR_JMX body in <" + collectorJmxData + ">");
        }
    }

}
