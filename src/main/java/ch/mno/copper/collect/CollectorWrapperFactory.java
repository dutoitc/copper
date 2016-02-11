package ch.mno.copper.collect;

import ch.mno.copper.stories.StoryGrammar;

import java.util.regex.Pattern;

/**
 * Created by dutoitc on 09.02.2016.
 */
public class CollectorWrapperFactory {

    public static AbstractCollectorWrapper buildCollectorWrapper(StoryGrammar grammar, String storyGiven) {
        if (Pattern.compile(grammar.getPatternFull("COLLECTOR_JMX"), Pattern.DOTALL).matcher(storyGiven).find()) {
            return JmxCollectorWrapper.buildCollector(grammar, storyGiven + '\n');
        } else if (Pattern.compile(grammar.getPatternFull("COLLECTOR_ORACLE"), Pattern.DOTALL).matcher(storyGiven).find()) {
            return OracleCollectorWrapper.buildCollector(grammar, storyGiven + '\n');
        } else if (Pattern.compile("GIVEN" + grammar.getPatternFull("SPACE_EOL") + "+|STORED VALUES", Pattern.DOTALL).matcher(storyGiven).find()) {
            return null;
        }
        throw new RuntimeException("Cannot find a valid GIVEN expression builder");
    }
}
