package ch.mno.copper.collect.builders;

import ch.mno.copper.collect.wrappers.WebCollectorWrapper;
import ch.mno.copper.helpers.SyntaxHelper;
import ch.mno.copper.stories.data.StoryGrammar;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.core.env.PropertyResolver;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class WebCollectorWrapperBuilder extends AbstractCollectorWrapperBuilder {

    public WebCollectorWrapperBuilder(StoryGrammar grammar, PropertyResolver propertyResolver) {
        super(grammar, propertyResolver);
    }

    public WebCollectorWrapper buildCollector(String storyGiven) {
        String patternJMX = grammar.getPatternFull("COLLECTOR_WEB");
        var matcher = Pattern.compile(patternJMX, Pattern.DOTALL).matcher(storyGiven);
        if (!matcher.find()) {
            int p = storyGiven.indexOf("COLLECTOR_WEB JMX");
            if (p > 0) {
                SyntaxHelper.checkSyntax(grammar, storyGiven, patternJMX);
            }
            throw new RuntimeException("Cannot find \n   >>>" + patternJMX + "\nin\n   >>>" + storyGiven);
        }
        //
        String collectorWebData = matcher.group(0);
        String patSpaceEol = grammar.getPatternFull("SPACE_EOL");
        String patSpace = grammar.getPatternFull("SPACE");
        String patEol = grammar.getPatternFull("EOL");

        var matcher2 = Pattern.compile("url=(.*),.*user=(.*?),.*password=(.*?)" + patEol + "(.*)", Pattern.DOTALL).matcher(collectorWebData);
        String queries;
        String url;
        String username = null;
        String password = null;
        if (matcher2.find()) {
            url = matcher2.group(1);
            username = matcher2.group(2);
            password = matcher2.group(3);
            queries = matcher2.group(4);
        } else {
            matcher2 = Pattern.compile("url=(.*?)" + patEol + "(.*)", Pattern.DOTALL).matcher(collectorWebData);
            if (matcher2.find()) {
                url = matcher2.group(1);
                queries = matcher2.group(2);
            } else {
                throw new RuntimeException("Cannot readInstant COLLECTOR_WEB body in <" + collectorWebData + ">");
            }
        }

        var matcher3 = Pattern.compile("KEEP (.*?)" + patSpace + "AS (.*?)" + patSpaceEol).matcher(queries);
        List<Pair<String, String>> valuesKept = new ArrayList<>();
        while (matcher3.find()) {
            String name1 = matcher3.group(1);
            String name2 = matcher3.group(2);
            valuesKept.add(new ImmutablePair<>(name1, name2));
        }
        return new WebCollectorWrapper(url, resolveProperty(username), resolveProperty(password), valuesKept);
    }

}
