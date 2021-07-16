package ch.mno.copper.collect.builders;

import ch.mno.copper.collect.wrappers.JdbcCollectorWrapper;
import ch.mno.copper.helpers.SyntaxHelper;
import ch.mno.copper.stories.data.StoryGrammar;
import org.springframework.core.env.PropertyResolver;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JdbcCollectorWrapperBuilder extends AbstractCollectorWrapperBuilder {

    public JdbcCollectorWrapperBuilder(StoryGrammar grammar, PropertyResolver propertyResolver) {
        super(grammar, propertyResolver);
    }

    public JdbcCollectorWrapper buildCollector(String storyGiven) {
        String patternJdbc = grammar.getPatternFull("COLLECTOR_JDBC");
        Matcher matcher = Pattern.compile(patternJdbc, Pattern.DOTALL).matcher(storyGiven);
        if (!matcher.find()) {
            int p = storyGiven.indexOf("COLLECTOR_JDBC");
            if (p > 0) {
                SyntaxHelper.checkSyntax(grammar, storyGiven, patternJdbc);
            }
            throw new RuntimeException("Cannot find \n   >>>" + patternJdbc + "\nin\n   >>>" + storyGiven);
        }

        // TODO: fix below (query)
        String collectorJdbcData = matcher.group(0);
        String patSpaceEol = grammar.getPatternFull("SPACE_EOL");
        Matcher matcher2 = Pattern.compile("url=(.*),.*user=(.*?),.*password=(.*?)" + patSpaceEol + ".*?\"(.*)\"", Pattern.DOTALL).matcher(collectorJdbcData);
        if (matcher2.find()) {
            return buildWrapper(matcher2);
        } else {
            matcher2 = Pattern.compile("url=\"(.*)\",.*user=(.*?),.*password=(.*?)" + patSpaceEol + ".*?\"(.*)\"", Pattern.DOTALL).matcher(collectorJdbcData);
            if (matcher2.find()) {
                return buildWrapper(matcher2);
            } else {
                throw new RuntimeException("Cannot readInstant COLLECTOR_JDBC body in <" + collectorJdbcData + ">");
            }
        }
    }

    private JdbcCollectorWrapper buildWrapper(Matcher matcher2) {
        String url = matcher2.group(1);
        String username = matcher2.group(2);
        String password = matcher2.group(3);
        String query = matcher2.group(4);
        return new JdbcCollectorWrapper(url, resolveProperty(username), resolveProperty(password), query);
    }

}
