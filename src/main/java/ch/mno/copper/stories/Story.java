package ch.mno.copper.stories;

import ch.mno.copper.collect.AbstractCollectorWrapper;
import ch.mno.copper.collect.CollectorWrapperFactory;
import ch.mno.copper.collect.connectors.ConnectorException;
import ch.mno.copper.helpers.SyntaxException;
import ch.mno.copper.helpers.SyntaxHelper;
import ch.mno.copper.report.AbstractReporterWrapper;
import ch.mno.copper.report.ReporterWrapperFactory;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by dutoitc on 07.02.2016.
 */
public class Story {

    private static final Logger LOG = LoggerFactory.getLogger(Story.class);
    private Path source;
    private String storyText;
    private String error;
    private String cron;
    private transient AbstractCollectorWrapper collectorWrapper;
    private transient AbstractReporterWrapper reporterWrapper;


    public Story(StoryGrammar grammar, String storyName, String storyText) throws IOException, ConnectorException {
        this(grammar, new ByteArrayInputStream(storyText.getBytes()), Paths.get("stories/" + storyName));
    }


    public Story(StoryGrammar grammar, InputStream is, Path source) throws IOException, ConnectorException {
        this.source = source;
        storyText = IOUtils.toString(is);
        storyText = Pattern.compile("#.*?\n", Pattern.DOTALL).matcher(storyText).replaceAll(""); // Remove comments lines
        if (!storyText.endsWith("\n")) storyText = storyText+"\n"; // Little help for parsing

        String patternMain = grammar.getPatternFull("MAIN");

        // Check Story Syntax
        // TODO: mark storyText as invalid, and permit WEB update
        try {
            SyntaxHelper.checkSyntax(grammar, patternMain, storyText);
        } catch (SyntaxException e) {
            error = e.getMessage();
            LOG.error("Wrong story: " + error);
            return;
        }

        // Extract triggers
        if (Pattern.compile(grammar.getPatternFull("RUN_ON"), Pattern.DOTALL).matcher(storyText).find()) {
            this.cron = buildRunOn(grammar);
        } else {
            throw new RuntimeException("cannot find a RUN_ON expression");
        }


        // Extract collector using GIVEN pattern
        Matcher matchGiven = Pattern.compile(grammar.getPatternFull("GIVEN"), Pattern.DOTALL).matcher(storyText);
        if (!matchGiven.find()) throw new RuntimeException("Cannot find a valid GIVEN expression");
        String storyGiven = matchGiven.group();
        this.collectorWrapper = CollectorWrapperFactory.buildCollectorWrapper(grammar, storyGiven);


        // Extract repporter using THEN pattern
        Matcher matchREPORTER = Pattern.compile(grammar.getPatternFull("REPORTER"), Pattern.DOTALL).matcher(storyText);
        if (!matchREPORTER.find()) throw new RuntimeException("Cannot find a valid REPORTER expression");
        String storyReporter = matchREPORTER.group();
        this.reporterWrapper = ReporterWrapperFactory.buildReporterWrapper(grammar, storyReporter);
    }

    private String buildRunOn(StoryGrammar grammar) {
        String patSpaceEol = grammar.getPatternFull("SPACE_EOL");
        String patEol = grammar.getPatternFull("EOL");
        String patternRunOn = grammar.getPatternFull("RUN_ON");
        Matcher matcher3 = Pattern.compile(patternRunOn, Pattern.DOTALL).matcher(storyText);
        if (!matcher3.find()) throw new RuntimeException("Only supporting RUN_ON expressions yet.");
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

    public AbstractReporterWrapper getReporterWrapper() {
        return reporterWrapper;
    }

    public String getCron() {
        return cron;
    }

    public String getName() {
        return source.getFileName().toString();
    }

    public String getStoryText() {
        return storyText;
    }

    public Path getSource() {
        return source;
    }

    public boolean hasError() {
        return error!=null;
    }
}
