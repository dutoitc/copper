package ch.mno.copper.collect;

import ch.mno.copper.stories.StoryGrammar;

import java.util.regex.Pattern;

/**
 * Created by dutoitc on 09.02.2016.
 */
public class CollectorWrapperFactory {

    public static AbstractCollectorWrapper buildCollectorWrapper(StoryGrammar grammar, String storyGiven) {
        if (Pattern.compile(grammar.getPatternFull("COLLECTOR_JMX"), Pattern.DOTALL).matcher(storyGiven).find()) {
            return JmxCollectorWrapper.buildCollectorJmx(grammar, storyGiven + '\n');
        }
        throw new RuntimeException("Cannot find a valid GIVEN expression builder");
    }
}
