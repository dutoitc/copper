package ch.mno.copper.stories;

import ch.mno.copper.collect.AbstractCollectorWrapper;
import ch.mno.copper.collect.JmxCollector;
import ch.mno.copper.collect.JmxCollectorWrapper;
import ch.mno.copper.collect.connectors.ConnectorException;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Created by dutoitc on 07.02.2016.
 */
public class Story {

    private String storyText;
    private AbstractCollectorWrapper collectorWrapper;

    public Story(StoryGrammar grammar, InputStream is) throws IOException, ConnectorException {
        String story = IOUtils.toString(is);
        story = Pattern.compile("#.*?\n", Pattern.DOTALL).matcher(story).replaceAll("");
        if (!story.endsWith("\n")) story = story+"\n"; // Little help for parsing

        String patternMain = grammar.getPatternFull("MAIN");


        //if (!Pattern.compile(patternMain).matcher(story).matches()) throw new RuntimeException("Invalid story, check syntax");
        checkSyntax(patternMain, story);

        storyText = story;

        // Temp: JMX
        String patternJMX = grammar.getPatternFull("COLLECTOR_JMX");
        Matcher matcher = Pattern.compile(patternJMX+"WHEN", Pattern.DOTALL).matcher(storyText);
        if (!matcher.find()) {
            int p = storyText.indexOf("COLLECTOR JMX");
            if (p>0) {
                checkSyntax(storyText.substring(p), patternJMX);
            }
            throw new RuntimeException("Cannot find \n   >>>"+patternJMX+"\nin\n   >>>" + storyText);
        }

        String collectorJmxData = matcher.group(0);
        String patSpaceEol = grammar.getPatternFull("SPACE_EOL");
        String patSpace = grammar.getPatternFull("SPACE");
        String patEol = grammar.getPatternFull("EOL");
        Matcher matcher2 = Pattern.compile("url=(.*),.*user=(.*?),.*password=(.*?)" + patEol+"(.*)\n", Pattern.DOTALL).matcher(collectorJmxData);
        if (matcher2.find()) {
            String url = matcher2.group(1);
            String username = matcher2.group(2);
            String password = matcher2.group(3);
            String queries = matcher2.group(4);

            Matcher matcher3 = Pattern.compile("QUERY (.*?) FOR (.*?)"+patSpace+"AS (.*?)"+patSpaceEol).matcher(queries);
            List<JmxCollector.JmxQuery> jmxQueries = new ArrayList<>();
            List<String> names = new ArrayList<>();
            while(matcher3.find()) {
                String oName = matcher3.group(1);
                String att = matcher3.group(2);
                String name = matcher3.group(3);
                jmxQueries.add(new JmxCollector.JmxQuery(oName, att));
                names.add(name);
            }
            collectorWrapper = new JmxCollectorWrapper(url, username, password, jmxQueries, names);
        } else {
            throw new RuntimeException("Cannot read COLLECTOR_JMX body");
        }

//        List<String> res = JmxCollector.jmxQuery(url, new JmxCollector.JmxQuery("java.lang:type=Runtime", "SpecName"), new JmxCollector.JmxQuery("java.lang:type=Runtime", "SpecVersion"));
//        res.forEach(s->System.out.println("Found: " + s));
    }


    private void checkSyntax(String pattern, String value) {
        if (Pattern.compile(pattern, Pattern.DOTALL).matcher(value).matches()) return;
        // Test latest pattern possible
        for (int i=pattern.length()-1; i>0; i--) {
            String currPattern = pattern.substring(0, i);
            try {
                Pattern currPatternCompiled = Pattern.compile(currPattern, Pattern.DOTALL);

                for (int j = value.length()-1; j>1; j--) {
                    String valuePart = value.substring(0, j);
                    if (currPatternCompiled.matcher(valuePart).matches()) {
                        throw new RuntimeException("Pattern \n   >>>" + pattern + "\n does not match\n   >>>" + value + "\n but pattern start \n   >>>" + currPattern + "\nmatches\n   >>>" + valuePart);
                    }
                }
            } catch (PatternSyntaxException e) {
                // Just ignore
            }
        }
    }


    public AbstractCollectorWrapper getCollectorWrapper() {
        return collectorWrapper;
    }
}
