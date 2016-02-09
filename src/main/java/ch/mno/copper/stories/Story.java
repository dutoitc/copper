package ch.mno.copper.stories;

import ch.mno.copper.collect.AbstractCollectorWrapper;
import ch.mno.copper.collect.CollectorWrapperFactory;
import ch.mno.copper.collect.connectors.ConnectorException;
import ch.mno.copper.helpers.SyntaxHelper;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by dutoitc on 07.02.2016.
 */
public class Story {

    private String storyText;
    private AbstractCollectorWrapper collectorWrapper;
    private String cron;

    public Story(StoryGrammar grammar, InputStream is) throws IOException, ConnectorException {
        String story = IOUtils.toString(is);
        story = Pattern.compile("#.*?\n", Pattern.DOTALL).matcher(story).replaceAll("");
        if (!story.endsWith("\n")) story = story+"\n"; // Little help for parsing

        String patternMain = grammar.getPatternFull("MAIN");


        //if (!Pattern.compile(patternMain).matcher(story).matches()) throw new RuntimeException("Invalid story, check syntax");
        SyntaxHelper.checkSyntax(patternMain, story);

        storyText = story;

        // Extract collector using GIVEN pattern
        Matcher matchGiven = Pattern.compile(grammar.getPatternFull("GIVEN"), Pattern.DOTALL).matcher(story);
        if (!matchGiven.find()) throw new RuntimeException("Cannot find a valid GIVEN expression");
        String storyGiven = matchGiven.group();
        this.collectorWrapper = CollectorWrapperFactory.buildCollectorWrapper(grammar, storyGiven);


        // Cron: yet only support WHEN (cron)
        if (Pattern.compile(grammar.getPatternFull("CRON"), Pattern.DOTALL).matcher(storyText).find()) {
            this.cron = buildCron(grammar);
        } else {
            throw new RuntimeException("cannot find a WHEN expression");
        }

//        List<String> res = JmxCollector.jmxQuery(url, new JmxCollector.JmxQuery("java.lang:type=Runtime", "SpecName"), new JmxCollector.JmxQuery("java.lang:type=Runtime", "SpecVersion"));
//        res.forEach(s->System.out.println("Found: " + s));
    }

    private String buildCron(StoryGrammar grammar) {
        String patSpaceEol = grammar.getPatternFull("SPACE_EOL");
        String patEol = grammar.getPatternFull("EOL");
        String patternCron = grammar.getPatternFull("CRON");
        Matcher matcher3 = Pattern.compile(patternCron, Pattern.DOTALL).matcher(storyText);
        if (!matcher3.find()) throw new RuntimeException("Only supporting WHEN cron expressions yet.");
        String cronTxt = matcher3.group(0);
        matcher3 = Pattern.compile("DAILY at (\\d{4})").matcher(cronTxt);
        if (matcher3.find()) {
            String date = matcher3.group(0).substring(9);
            int hour = Integer.parseInt(date.substring(0, 2), 10);
            int min = Integer.parseInt(date.substring(2, 4), 10);
            return min + " " + hour + " * * *";
        }

        String patCronStd = grammar.getPatternFull("CRON_STD");
        matcher3 = Pattern.compile("CRON" + patSpaceEol + "+(" + patCronStd + ")" + patEol, Pattern.DOTALL).matcher(cronTxt);
        if (!matcher3.find()) throw new RuntimeException("Not found cron in " + cronTxt);
        return matcher3.group(1);
    }






    public AbstractCollectorWrapper getCollectorWrapper() {
        return collectorWrapper;
    }

    public String getCron() {
        return cron;
    }
}
