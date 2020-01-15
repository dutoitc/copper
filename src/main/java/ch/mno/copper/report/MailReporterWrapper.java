package ch.mno.copper.report;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ch.mno.copper.helpers.SyntaxHelper;
import ch.mno.copper.store.ValuesStore;
import ch.mno.copper.stories.data.StoryGrammar;

public class MailReporterWrapper extends AbstractReporterWrapper {

    private StoryGrammar grammar;
    private String storyGiven;
    private String dest;
    private String title;
    private String messageTemplate;
    private MailReporter reporter;

    public MailReporterWrapper(StoryGrammar grammar, String storyGiven, String server, String serverUsername, String serverPassword, int serverPort, String from, String replyTo) {
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
        reporter = new MailReporter(server, serverUsername, serverPassword, serverPort, from, replyTo);
    }

    public static AbstractReporterWrapper buildReporter(StoryGrammar grammar, String storyGiven, String server, String serverUsername, String serverPassword, int serverPort, String from, String replyTo) {
        return new MailReporterWrapper(grammar, storyGiven, server,  serverUsername,  serverPassword,  serverPort, from,  replyTo);
    }

    @Override
    public void execute(Map<String, String> values, ValuesStore valuesStore) {
        String message = messageTemplate;
        String message2 = replaceValues(values, message);
        String title2 = replaceValues(values, title);

        Map<String, String> reporterValues = new HashMap<>();
        reporterValues.put(MailReporter.PARAMETERS.TITLE.toString(), title2);
        reporterValues.put(MailReporter.PARAMETERS.TO.toString(),dest);
        reporterValues.put(MailReporter.PARAMETERS.BODY.toString(),message2);

        reporter.report(message2, reporterValues);
    }

    private String replaceValues(Map<String, String> values, String message) {
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
        return message;
    }

}
