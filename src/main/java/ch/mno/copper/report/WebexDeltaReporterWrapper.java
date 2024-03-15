package ch.mno.copper.report;

import ch.mno.copper.CopperException;
import ch.mno.copper.collect.connectors.ConnectorException;
import ch.mno.copper.store.ValuesStore;
import ch.mno.copper.stories.data.StoryGrammar;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
public class WebexDeltaReporterWrapper implements AbstractReporterWrapper {

    private final int delta;
    private final String token;
    private final String roomId;
    private final String keyFilter;
    private final String messageTemplate;
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
            throw new CopperException("Cannot find a valid WEBEX pattern in " + storyGiven + ", pattern " + pattern);

        delta = Integer.parseInt(matcher.group(1));
        token = matcher.group(2);
        roomId = matcher.group(3);
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
        var start = now.minusSeconds(60L * delta);
        var pat = Pattern.compile(keyFilter);
        var deltaStr = valuesStore.queryValues(start, now)
                .stream()
                .sorted()
                .distinct()
                .filter(key -> pat.matcher(key).matches())
                .map(key -> "- " + key + ": " + valuesStore.getValue(key))
                .collect(Collectors.joining("\n"));

        // Report
        if (!deltaStr.isBlank()) {
            String message = messageTemplate;
            message = message.replace("{{STATUS}}", deltaStr);
            ReportHelper.expandMessage(values, message, valuesStore);


            Map<String, String> reporterValues = new HashMap<>();
            reporterValues.put(WebexReporter.PARAMETERS.TOKEN.toString(), token);
            reporterValues.put(WebexReporter.PARAMETERS.ROOM_ID.toString(), roomId);

            try {
                reporter.report(message, reporterValues);
            } catch (ConnectorException e) {
                log.debug("Exception: " + e.getMessage(), e);
            }
        }
    }


}
