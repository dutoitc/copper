package ch.mno.copper.collect.builders;

import ch.mno.copper.collect.wrappers.SocketCollectorWrapper;
import ch.mno.copper.helpers.SyntaxHelper;
import ch.mno.copper.stories.data.StoryGrammar;
import org.springframework.core.env.PropertyResolver;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SocketCollectorWrapperBuilder extends AbstractCollectorWrapperBuilder {

    public SocketCollectorWrapperBuilder(StoryGrammar grammar, PropertyResolver propertyResolver) {
        super(grammar, propertyResolver);
    }

    private static SocketCollectorWrapper buildWrapper(Matcher matcher2) {
        String host = matcher2.group(1);
        int port = Integer.parseInt(matcher2.group(2));
        int timeoutMS = Integer.parseInt(matcher2.group(3));
        String query = matcher2.group(4);
        return new SocketCollectorWrapper(host, port, timeoutMS, query);
    }

    public SocketCollectorWrapper buildCollector(String storyGiven) {
        String patternJdbc = grammar.getPatternFull("COLLECTOR_SOCKET");
        Matcher matcher = Pattern.compile(patternJdbc, Pattern.DOTALL).matcher(storyGiven);
        if (!matcher.find()) {
            int p = storyGiven.indexOf("COLLECTOR_SOCKET");
            if (p > 0) {
                SyntaxHelper.checkSyntax(grammar, storyGiven, patternJdbc);
            }
            throw new RuntimeException("Cannot find \n   >>>" + patternJdbc + "\nin\n   >>>" + storyGiven);
        }

        String collectorSocketData = matcher.group(0);
        String patSpaceEol = grammar.getPatternFull("SPACE_EOL");
        Matcher matcher2 = Pattern.compile("host=(.*),.*port=(.*?),.*timeout_ms=(.*?)" + patSpaceEol + ".*?AS ([a-zA-Z0-9_]+)", Pattern.DOTALL).matcher(collectorSocketData);
        if (matcher2.find()) {
            return buildWrapper(matcher2);
        } else {
            throw new RuntimeException("Cannot readInstant COLLECTOR_SOCKET body in <" + collectorSocketData + ">");
        }
    }

}
