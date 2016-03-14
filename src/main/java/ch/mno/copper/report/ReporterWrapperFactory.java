package ch.mno.copper.report;

import ch.mno.copper.CopperMediator;
import ch.mno.copper.stories.StoryGrammar;

import java.util.regex.Pattern;

/**
 * Created by dutoitc on 09.02.2016.
 */
public class ReporterWrapperFactory {

    public static AbstractReporterWrapper buildReporterWrapper(StoryGrammar grammar, String storyGiven) {
        if (Pattern.compile(grammar.getPatternFull("PUSHOVER"), Pattern.DOTALL).matcher(storyGiven).find()) {
            return PushoverReporterWrapper.buildReporter(grammar, storyGiven + '\n');
        } else if (Pattern.compile(grammar.getPatternFull("MAIL"), Pattern.DOTALL).matcher(storyGiven).find()) {
            CopperMediator mediator = CopperMediator.getInstance();
            return MailReporterWrapper.buildReporter(grammar, storyGiven + '\n', mediator.getProperty("mailServer"), mediator.getProperty("mailUsername"), mediator.getProperty("mailPassword"), mediator.getProperty("mailFrom"), mediator.getProperty("mailReplyTo"));
        } else if (Pattern.compile("STORE VALUES").matcher(storyGiven).find()) {
            //return JdbcCollectorWrapper.buildCollector(grammar, storyGiven + '\n');
            return null;
        }
        throw new RuntimeException("Cannot find a valid REPORT expression builder");
    }
}
