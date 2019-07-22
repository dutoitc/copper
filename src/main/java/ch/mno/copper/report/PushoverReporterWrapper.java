package ch.mno.copper.report;

import ch.mno.copper.collect.connectors.ConnectorException;
import ch.mno.copper.store.ValuesStore;
import ch.mno.copper.stories.data.StoryGrammar;

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
        reporter = new PushoverReporter();
    }

    public static AbstractReporterWrapper buildReporter(StoryGrammar grammar, String storyGiven) {
        return new PushoverReporterWrapper(grammar, storyGiven);
    }

    @Override
    public void execute(Map<String, String> values, ValuesStore valuesStore) {
        String message = ReportHelper.expandMessage(values, messageTemplate, valuesStore);

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
