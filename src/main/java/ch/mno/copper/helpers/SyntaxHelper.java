package ch.mno.copper.helpers;

import ch.mno.copper.stories.data.StoryGrammar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Created by xsicdt on 09/02/16.
 */
public class SyntaxHelper {

    private static final Logger LOG = LoggerFactory.getLogger(SyntaxHelper.class);

    private SyntaxHelper() {

    }

    public static String checkSyntax(StoryGrammar grammar, String pattern, String value) {
        var matcher = Pattern.compile(pattern, Pattern.DOTALL).matcher(value);
        if (matcher.matches()) {
            matcher = Pattern.compile(pattern, Pattern.DOTALL).matcher(value);
            if (!matcher.find()) {
                LOG.error("Match error for {}", value);
            }
            return matcher.group(0);
        }

        var sbM = new StringBuilder();
        grammar.getKeys().stream()
                .filter(p -> Pattern.compile(grammar.getPatternFull(p), Pattern.DOTALL).matcher(value).find())
                .forEach(v -> sbM.insert(0, v + ','));

        var sb = new StringBuilder();
        sb.append("Pattern \n   >>>" + pattern + "\n does not match\n   >>>" + value + "\n");
        if (sbM.length() > 0) {
            sb.append("But it matches the following patterns parts: [");
            sb.append(sbM.toString(), 0, sbM.length() - 1);
            sb.append("]\n\n");
        }


        // Test latest pattern possible
        for (int i = pattern.length() - 1; i > 0; i--) {
            var currPattern = pattern.substring(0, i);
            try {
                var currPatternCompiled = Pattern.compile(currPattern, Pattern.DOTALL);

                for (int j = value.length() - 1; j > 1; j--) {
                    var valuePart = value.substring(0, j);
                    if (currPatternCompiled.matcher(valuePart).matches()) {
                        sb.append("Pattern start \n   >>>" + currPattern + "\nmatches\n   >>>" + valuePart + "\n\n");
                        throw new SyntaxException(sb.toString());
                    }
                }
            } catch (PatternSyntaxException e) {
                // Just ignore
            }
        }
        throw new SyntaxException(sb.toString());
    }

}
