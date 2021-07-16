package ch.mno.copper.collect.builders;

import ch.mno.copper.collect.wrappers.BinaryCollectorWrapper;
import ch.mno.copper.helpers.SyntaxHelper;
import ch.mno.copper.stories.data.StoryGrammar;
import org.springframework.core.env.PropertyResolver;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BinaryCollectorWrapperBuilder extends AbstractCollectorWrapperBuilder {

    public BinaryCollectorWrapperBuilder(StoryGrammar grammar, PropertyResolver propertyResolver) {
        super(grammar, propertyResolver);
    }

    public BinaryCollectorWrapper buildCollector(String storyGiven) {
        String pattern = grammar.getPatternFull("COLLECTOR_BINARY");
        Matcher matcher = Pattern.compile(pattern, Pattern.DOTALL).matcher(storyGiven);
        if (!matcher.find()) {
            int p = storyGiven.indexOf("COLLECTOR_BINARY");
            if (p > 0) {
                SyntaxHelper.checkSyntax(grammar, storyGiven, pattern);
            }
            throw new RuntimeException("Cannot find \n   >>>" + pattern + "\nin\n   >>>" + storyGiven);
        }

        String collectorSocketData = matcher.group(0);
        String patSpace = grammar.getPatternFull("SPACE");
        String patSpaceEol = grammar.getPatternFull("SPACE_EOL");
        String regex = "(CHECK_BY_WHICH|CHECK_BY_PATH)" + patSpace + "(.*?)" + patSpace + "AS" + patSpace + "(.*?)" + patSpaceEol;
        Matcher matcher2 = Pattern.compile(regex, Pattern.DOTALL).matcher(collectorSocketData);
        List<BinaryCollectorWrapper.CheckElement> checkElements = new ArrayList<>(8);
        while (matcher2.find()) {
            checkElements.add(new BinaryCollectorWrapper.CheckElement(matcher2.group(1),matcher2.group(2),matcher2.group(3)));
        }
        if (checkElements.isEmpty()) {
            throw new RuntimeException("No CHECK found in BINARY_CHECK");
        }
        return new BinaryCollectorWrapper(checkElements);
    }

}
