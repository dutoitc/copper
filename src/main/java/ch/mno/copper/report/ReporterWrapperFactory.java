package ch.mno.copper.report;

import ch.mno.copper.stories.StoryGrammar;

import java.util.regex.Pattern;

/**
 * Created by dutoitc on 09.02.2016.
 */
public class ReporterWrapperFactory {

    public static AbstractReporterWrapper buildReporterWrapper(StoryGrammar grammar, String storyGiven) {
        if (Pattern.compile(grammar.getPatternFull("PUSHOVER"), Pattern.DOTALL).matcher(storyGiven).find()) {
            return PushoverReporterWrapper.buildReporter(grammar, storyGiven + '\n');
        } else if (Pattern.compile("STORE VALUES").matcher(storyGiven).find()) {
            //return JdbcCollectorWrapper.buildCollector(grammar, storyGiven + '\n');
            return null;
        }
        throw new RuntimeException("Cannot find a valid REPORT expression builder");
    }
}
