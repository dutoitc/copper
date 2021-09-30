package ch.mno.copper.collect.builders;

import ch.mno.copper.collect.CollectorException;
import ch.mno.copper.collect.collectors.JmxCollector;
import ch.mno.copper.collect.wrappers.JmxCollectorWrapper;
import ch.mno.copper.helpers.SyntaxHelper;
import ch.mno.copper.stories.data.StoryGrammar;
import org.springframework.core.env.PropertyResolver;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JmxCollectorWrapperBuilder extends AbstractCollectorWrapperBuilder {

    public JmxCollectorWrapperBuilder(StoryGrammar grammar, PropertyResolver propertyResolver) {
        super(grammar, propertyResolver);
    }

    public JmxCollectorWrapper buildCollector(String storyGiven) {
        // Temp: JMX
        String patternJMX = grammar.getPatternFull("COLLECTOR_JMX");
        Matcher matcher = Pattern.compile(patternJMX, Pattern.DOTALL).matcher(storyGiven);
        if (!matcher.find()) {
            int p = storyGiven.indexOf("COLLECTOR JMX");
            if (p > 0) {
                SyntaxHelper.checkSyntax(grammar, storyGiven, patternJMX);
            }
            throw new CollectorException("Cannot find \n   >>>" + patternJMX + "\nin\n   >>>" + storyGiven);
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
            return buildInstance(url, username, password, jmxQueries, names);
        } else {
            throw new CollectorException("Cannot readInstant COLLECTOR_JMX body in <" + collectorJmxData + ">");
        }
    }

    /**
     * Build instance, with property conversion if present
     */
    private JmxCollectorWrapper buildInstance(String url, String username, String password, List<JmxCollector.JmxQuery> jmxQueries, List<String> names) {
        username = resolveProperty(username);
        password = resolveProperty(password);
        return new JmxCollectorWrapper(url, username, password, jmxQueries, names);
    }

}
