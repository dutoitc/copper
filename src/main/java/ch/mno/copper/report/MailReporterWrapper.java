package ch.mno.copper.report;

import ch.mno.copper.collect.connectors.ConnectorException;
import ch.mno.copper.helpers.SyntaxHelper;
import ch.mno.copper.stories.StoryGrammar;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by dutoitc on 07.02.2016.
 */
public class MailReporterWrapper extends AbstractReporterWrapper {

    private StoryGrammar grammar;
    private String storyGiven;
    private String dest;
    private String title;
    private String messageTemplate;
    private MailReporter reporter;

    public MailReporterWrapper(StoryGrammar grammar, String storyGiven, String server, String serverUsername, String serverPassword, String from, String replyTo) {
        this.grammar = grammar;
        this.storyGiven = storyGiven;

        // Mail::=REPORT BY Mail to ".*?"¦SPACE_EOL¦+WITH token=".*?"¦SPACE_EOL¦+WITH title=".*?"¦SPACE_EOL¦+WITH message=".*?"
        String spaceEol =  grammar.getPatternFull("SPACE_EOL");
        String pattern="REPORT BY MAIL to \"(.*?)\""+spaceEol+"+WITH title=\"(.*?)\""+spaceEol+"+WITH message=\"(.*?)\"";
        Matcher matcher = Pattern.compile(pattern, Pattern.DOTALL).matcher(storyGiven);
        if (!matcher.find()) {
            SyntaxHelper.checkSyntax(grammar, pattern, storyGiven);
            throw new RuntimeException("Cannot find a valid Mail pattern in " + storyGiven + ", pattern " + pattern);
        }

        dest = matcher.group(1);
        title = matcher.group(2);
        messageTemplate = matcher.group(3);
        reporter = new MailReporter(server, serverUsername, serverPassword, from, replyTo);
    }

    public static AbstractReporterWrapper buildReporter(StoryGrammar grammar, String storyGiven, String server, String serverUsername, String serverPassword, String from, String replyTo) {
        return new MailReporterWrapper(grammar, storyGiven, server,  serverUsername,  serverPassword,  from,  replyTo);
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
        reporterValues.put(MailReporter.PARAMETERS.TITLE.toString(), title);
        reporterValues.put(MailReporter.PARAMETERS.TO.toString(),dest);
        reporterValues.put(MailReporter.PARAMETERS.BODY.toString(),message);


        try {
            reporter.report(message, reporterValues);
        } catch (ConnectorException e) {
            e.printStackTrace();
        }
    }

}
