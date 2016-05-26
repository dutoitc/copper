package ch.mno.copper.report;

import ch.mno.copper.ValuesStore;
import ch.mno.copper.collect.connectors.ConnectorException;
import ch.mno.copper.stories.StoryGrammar;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by dutoitc on 07.02.2016.
 */
public class CsvReporterWrapper extends AbstractReporterWrapper {

    private StoryGrammar grammar;
    private String storyGiven;
    private String filename;
    private String headers;
    private String line;
    private CsvReporter reporter;

    public CsvReporterWrapper(StoryGrammar grammar, String storyGiven) {
        this.grammar = grammar;
        this.storyGiven = storyGiven;

        String spaceEol =  grammar.getPatternFull("SPACE_EOL");
        String pattern= "REPORT BY CSV to \"(.*?)\"" + spaceEol + "+WITH headers=\"(.*?)\"" + spaceEol + "+WITH line=\"(.*?)\"";
        Matcher matcher = Pattern.compile(pattern, Pattern.DOTALL).matcher(storyGiven);
        if (!matcher.find()) throw new RuntimeException("Cannot find a valid CSV pattern in " + storyGiven + ", pattern " + pattern);

        filename = matcher.group(1);
        headers = matcher.group(2);
        line = matcher.group(3);
        reporter = new CsvReporter();
    }

    public static AbstractReporterWrapper buildReporter(StoryGrammar grammar, String storyGiven) {
        return new CsvReporterWrapper(grammar, storyGiven);
    }

    @Override
    public void execute(Map<String, String> values) {
        String message = ReportHelper.expandMessage(values, line, ValuesStore.getInstance());

        Map<String, String> reporterValues = new HashMap<>();
        reporterValues.put(CsvReporter.PARAMETERS.FILENAME.toString(), filename);
        reporterValues.put(CsvReporter.PARAMETERS.HEADERS.toString(),headers);
        reporterValues.put(CsvReporter.PARAMETERS.LINE.toString(), message);


        try {
            reporter.report(message, reporterValues);
        } catch (ConnectorException e) {
            e.printStackTrace();
        }
    }

}
