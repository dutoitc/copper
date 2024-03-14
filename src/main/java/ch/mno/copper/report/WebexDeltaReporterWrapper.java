package ch.mno.copper.report;

import ch.mno.copper.collect.connectors.ConnectorException;
import ch.mno.copper.store.ValuesStore;
import ch.mno.copper.stories.data.StoryGrammar;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class WebexDeltaReporterWrapper implements AbstractReporterWrapper {

    private int delta;
    private String token;
    private String room_id;
    private String keyFilter;
    private String messageTemplate;
    private WebexReporter reporter;

    public WebexDeltaReporterWrapper(StoryGrammar grammar, String storyGiven) {

        // WEBEX::=REPORT BY WEBEX¦SPACE_EOL¦+WITH delta=\d+¦+WITH token=\".*?\"¦SPACE_EOL¦+WITH room_id=.\".*?\"¦SPACE_EOL¦+WITH message=\".*?\"
        String spaceEol = grammar.getPatternFull("SPACE_EOL");
        String pattern = "REPORT BY WEBEX" + spaceEol +
                "+WITH delta=(\\d+)" + spaceEol +
                "+WITH token=\"(.*?)\"" + spaceEol +
                "+WITH room_id=\"(.*?)\"" + spaceEol +
                "+WITH key_filter=\"(.*?)\"" + spaceEol +
                "+WITH message=\"(.*?)\"";
        Matcher matcher = Pattern.compile(pattern, Pattern.DOTALL).matcher(storyGiven);
        if (!matcher.find())
            throw new RuntimeException("Cannot find a valid WEBEX pattern in " + storyGiven + ", pattern " + pattern);

        delta = Integer.parseInt(matcher.group(1));
        token = matcher.group(2);
        room_id = matcher.group(3);
        keyFilter = matcher.group(4);
        messageTemplate = matcher.group(5);
        reporter = new WebexReporter();
    }

    void setReporter4Tests(WebexReporter reporter) {
        this.reporter = reporter;
    }

    public static AbstractReporterWrapper buildReporter(StoryGrammar grammar, String storyGiven) {
        return new WebexDeltaReporterWrapper(grammar, storyGiven);
    }

    @Override
    public void execute(Map<String, String> values, ValuesStore valuesStore) {
        // Find values
        var now = Instant.now();
        var start = now.minusSeconds(60 * delta);
        var pat = Pattern.compile(keyFilter);
        var deltaStr = valuesStore.queryValues(start, now)
                .stream()
                .sorted()
                .distinct()
                .filter(key -> pat.matcher(key).matches())
                .map(key -> key + ": " + valuesStore.getValue(key))
                .collect(Collectors.joining("<br/>"));

        // Report
        if (!deltaStr.isBlank()) {
            String message = ReportHelper.expandMessage(values, messageTemplate, valuesStore);
            message = message.replace("{{STATUS}}", deltaStr);

            Map<String, String> reporterValues = new HashMap<>();
            reporterValues.put(WebexReporter.PARAMETERS.TOKEN.toString(), token);
            reporterValues.put(WebexReporter.PARAMETERS.ROOM_ID.toString(), room_id);

            try {
                reporter.report(message, reporterValues);
            } catch (ConnectorException e) {
                e.printStackTrace();
            }
        }
    }


}
