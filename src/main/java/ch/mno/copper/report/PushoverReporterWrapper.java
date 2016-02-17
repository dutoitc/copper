package ch.mno.copper.report;

import ch.mno.copper.collect.connectors.ConnectorException;
import ch.mno.copper.stories.StoryGrammar;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by dutoitc on 07.02.2016.
 */
public class PushoverReporterWrapper extends AbstractReporterWrapper {

    private StoryGrammar grammar;
    private String storyGiven;
    private String applicationToken;
    private String dest;
    private String title;
    private String messageTemplate;
    private PushoverReporter reporter;

    public PushoverReporterWrapper(StoryGrammar grammar, String storyGiven) {
        this.grammar = grammar;
        this.storyGiven = storyGiven;

        // PUSHOVER::=REPORT BY PUSHOVER to ".*?"¦SPACE_EOL¦+WITH token=".*?"¦SPACE_EOL¦+WITH title=".*?"¦SPACE_EOL¦+WITH message=".*?"
        String spaceEol =  grammar.getPatternFull("SPACE_EOL");
        String pattern="REPORT BY PUSHOVER to \"(.*?)\""+spaceEol+"+WITH token=\"(.*?)\""+spaceEol+"+WITH title=\"(.*?)\""+spaceEol+"+WITH message=\"(.*?)\"";
        Matcher matcher = Pattern.compile(pattern, Pattern.DOTALL).matcher(storyGiven);
        if (!matcher.find()) throw new RuntimeException("Cannot find a valid PUSHOVER pattern in " + storyGiven + ", pattern " + pattern);

        dest = matcher.group(1);
        applicationToken = matcher.group(2);
        title = matcher.group(3);
        messageTemplate = matcher.group(4);
        PushoverReporter reporter = new PushoverReporter();
    }

    public static AbstractReporterWrapper buildReporter(StoryGrammar grammar, String storyGiven) {
        return new PushoverReporterWrapper(grammar, storyGiven);
    }

    @Override
    public void execute(Map<String, String> values) {
        String message = messageTemplate;
        int p1 = message.indexOf("{{");
        while (p1>0) {
            int p2 = message.indexOf("}}");
            if (p2==-1) throw new RuntimeException("Wrong message format: " + message);

            String key = message.substring(p1+2, p2);
            String value = values.get(key);
            if (value==null) value="?";
            message = message.substring(0, p1) + value + message.substring(p2+2);
            p1 = message.indexOf("{{");
        }

        Map<String, String> reporterValues = new HashMap<>();
        reporterValues.put(PushoverReporter.PARAMETERS.TITLE.toString(), title);
        reporterValues.put(PushoverReporter.PARAMETERS.APPLICATION_TOKEN.toString(),applicationToken);
        reporterValues.put(PushoverReporter.PARAMETERS.DEST.toString(), dest);
        reporterValues.put(PushoverReporter.PARAMETERS.HTML.toString(), "true");


        try {
            reporter.report(message, reporterValues);
        } catch (ConnectorException e) {
            e.printStackTrace();
        }
    }

}
