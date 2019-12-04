package ch.mno.copper.report;

import ch.mno.copper.CopperMediator;
import ch.mno.copper.PropertiesProvider;
import ch.mno.copper.stories.data.StoryGrammar;

import java.util.regex.Pattern;

/**
 * Created by dutoitc on 09.02.2016.
 */
public class ReporterWrapperFactory {

    public static  <T extends AbstractReporterWrapper> T buildReporterWrapper(StoryGrammar grammar, String storyGiven) {
        if (Pattern.compile(grammar.getPatternFull("PUSHOVER"), Pattern.DOTALL).matcher(storyGiven).find()) {
            return (T)PushoverReporterWrapper.buildReporter(grammar, storyGiven + '\n');
        } else if (Pattern.compile(grammar.getPatternFull("MAIL"), Pattern.DOTALL).matcher(storyGiven).find()) {
            PropertiesProvider p = new PropertiesProvider();
            String sport = p.getProperty("mailPort", "25");
            int port=Integer.parseInt(sport);
            return (T)MailReporterWrapper.buildReporter(grammar, storyGiven + '\n',
                    p.getProperty("mailServer"),
                    p.getProperty("mailUsername"),
                    p.getProperty("mailPassword"), port,
                    p.getProperty("mailFrom"),
                    p.getProperty("mailReplyTo"));
        } else if (Pattern.compile(grammar.getPatternFull("CSV"), Pattern.DOTALL).matcher(storyGiven).find()) {
            return (T)CsvReporterWrapper.buildReporter(grammar, storyGiven + '\n');
        }  else if (Pattern.compile("STORE VALUES").matcher(storyGiven).find()) {
            //return JdbcCollectorWrapper.buildStoryTask(grammar, storyGiven + '\n');
            return null;
        }
        throw new RuntimeException("Cannot find a valid REPORT expression builder");
    }
}
