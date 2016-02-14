package ch.mno.copper.stories;

import ch.mno.copper.collect.AbstractCollectorWrapper;
import ch.mno.copper.collect.CollectorWrapperFactory;
import ch.mno.copper.collect.connectors.ConnectorException;
import ch.mno.copper.helpers.SyntaxHelper;
import ch.mno.copper.report.AbstractReporterWrapper;
import ch.mno.copper.report.ReporterWrapperFactory;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by dutoitc on 07.02.2016.
 */
public class Story {

    private String storyText;
    private String cron;
    private Path source;
    private transient AbstractCollectorWrapper collectorWrapper;
    private transient AbstractReporterWrapper reporterWrapper;

    public Story(StoryGrammar grammar, InputStream is, Path source) throws IOException, ConnectorException {
        this.source = source;
        storyText = IOUtils.toString(is);
        storyText = Pattern.compile("#.*?\n", Pattern.DOTALL).matcher(storyText).replaceAll("");
        if (!storyText.endsWith("\n")) storyText = storyText+"\n"; // Little help for parsing

        String patternMain = grammar.getPatternFull("MAIN");

        // Check Story Syntax
        // TODO: mark storyText as invalid, and permit WEB update
        SyntaxHelper.checkSyntax(grammar, patternMain, storyText);


        // Extract collector using GIVEN pattern
        Matcher matchGiven = Pattern.compile(grammar.getPatternFull("GIVEN"), Pattern.DOTALL).matcher(storyText);
        if (!matchGiven.find()) throw new RuntimeException("Cannot find a valid GIVEN expression");
        String storyGiven = matchGiven.group();
        this.collectorWrapper = CollectorWrapperFactory.buildCollectorWrapper(grammar, storyGiven);


        // Extract triggers using WHEN pattern
        // TODO support more than WHEN [CRON]
        if (Pattern.compile(grammar.getPatternFull("CRON"), Pattern.DOTALL).matcher(storyText).find()) {
            this.cron = buildCron(grammar);
        } else {
            throw new RuntimeException("cannot find a WHEN expression");
        }

        // Extract repporter using THEN pattern
        Matcher matchREPORTER = Pattern.compile(grammar.getPatternFull("REPORTER"), Pattern.DOTALL).matcher(storyText);
        if (!matchREPORTER.find()) throw new RuntimeException("Cannot find a valid REPORTER expression");
        String storyReporter = matchREPORTER.group();
        this.reporterWrapper = ReporterWrapperFactory.buildReporterWrapper(grammar, storyReporter);

//        THEN REPORT BY PUSHOVER to dest
//        WITH token=xxx
//        WITH title="Status RCEnt"
//        WITH message="Status (nouveau, en cours, en erreur, traitée):
//        PR {{RCENT_PR_STG_NOUVEAU}}/{{RCENT_PR_STG_EN_COURS}}/{{RCENT_PR_MST_EN_ERREUR}}/{{RCENT_PR_TRAITEE}}
//        PP {{RCENT_PP_STG_NOUVEAU}}/{{RCENT_PP_STG_EN_COURS}}/{{RCENT_PP_MST_EN_ERREUR}}/{{RCENT_PP_TRAITEE}}
//        VA {{RCENT_VA_STG_NOUVEAU}}/{{RCENT_VA_STG_EN_COURS}}/{{RCENT_VA_MST_EN_ERREUR}}/{{RCENT_VA_TRAITEE}}
//        IN {{RCENT_IN_STG_NOUVEAU}}/{{RCENT_IN_STG_EN_COURS}}/{{RCENT_IN_MST_EN_ERREUR}}/{{RCENT_IN_TRAITEE}}
//        "

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
}
