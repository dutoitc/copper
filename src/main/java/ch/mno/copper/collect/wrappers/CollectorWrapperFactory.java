package ch.mno.copper.collect.wrappers;

import ch.mno.copper.collect.builders.*;
import ch.mno.copper.stories.data.StoryGrammar;
import org.springframework.core.env.PropertyResolver;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

/**
 * Created by dutoitc on 09.02.2016.
 */
@Component
public class CollectorWrapperFactory {

    private final PropertyResolver propertyResolver;
    private final StoryGrammar grammar;

    public CollectorWrapperFactory(PropertyResolver propertyResolver, StoryGrammar grammar) {
        this.propertyResolver = propertyResolver;
        this.grammar = grammar;
    }

    public AbstractCollectorWrapper build(String storyGiven) {
        AbstractCollectorWrapperBuilder builder = null;
        if (matchesPattern("COLLECTOR_JMX", storyGiven)) {
            builder = new JmxCollectorWrapperBuilder(grammar, propertyResolver);
        } else if (matchesPattern("COLLECTOR_WEB", storyGiven)) {
            builder = new WebCollectorWrapperBuilder(grammar, propertyResolver);
        } else if (matchesPattern("COLLECTOR_JDBC", storyGiven)) {
            builder = new JdbcCollectorWrapperBuilder(grammar, propertyResolver);
        } else if (matchesPattern("COLLECTOR_SOCKET", storyGiven)) {
            builder = new SocketCollectorWrapperBuilder(grammar, propertyResolver);
        } else if (matchesPattern("COLLECTOR_BINARY", storyGiven)) {
            builder = new BinaryCollectorWrapperBuilder(grammar, propertyResolver);
        } else if (Pattern.compile("GIVEN" + grammar.getPatternFull("SPACE_EOL") + "+|STORED VALUES", Pattern.DOTALL).matcher(storyGiven).find()) {
            return null;
        }
        if (builder != null) {
            return builder.buildCollector(storyGiven + '\n');
        }
        throw new RuntimeException("Cannot find a valid GIVEN expression builder");
    }

    private boolean matchesPattern(String PATTERN_KEY, String storyGiven) {
        return Pattern.compile(grammar.getPatternFull(PATTERN_KEY), Pattern.DOTALL).matcher(storyGiven).find();
    }

}
