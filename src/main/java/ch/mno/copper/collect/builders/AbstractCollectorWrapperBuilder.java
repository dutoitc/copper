package ch.mno.copper.collect.builders;

import ch.mno.copper.collect.wrappers.AbstractCollectorWrapper;
import ch.mno.copper.stories.data.StoryGrammar;
import org.springframework.core.env.PropertyResolver;

public abstract class AbstractCollectorWrapperBuilder {

    protected final StoryGrammar grammar;
    protected final PropertyResolver propertyResolver;

    protected AbstractCollectorWrapperBuilder(StoryGrammar grammar, PropertyResolver propertyResolver) {
        this.grammar = grammar;
        this.propertyResolver = propertyResolver;
    }

    protected String resolveProperty(String value) {
        if (value==null) {
            return null;
        }
        if (value.startsWith("${")) {
            value = propertyResolver.getProperty(value.substring(2, value.length() - 1));
        }
        return value;
    }

    public abstract AbstractCollectorWrapper buildCollector(String s);
}
