package ch.mno.copper.helpers;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.mno.copper.stories.data.StoryGrammar;

/**
 * Created by xsicdt on 09/02/16.
 */
public class SyntaxHelper {

    private static Logger LOG = LoggerFactory.getLogger(SyntaxHelper.class);

    public static String checkSyntax(StoryGrammar grammar, String pattern, String value) {
        Matcher matcher = Pattern.compile(pattern, Pattern.DOTALL).matcher(value);
        if (matcher.matches()) {
            matcher = Pattern.compile(pattern, Pattern.DOTALL).matcher(value);
            if (!matcher.find()) {
                LOG.error("Match error for {}", value);
            }
            return matcher.group(0);
        }

        StringBuffer sbM = new StringBuffer();
        grammar.getKeys().stream().filter(p->Pattern.compile(p, Pattern.DOTALL).matcher(value).find()).forEach(v->sbM.insert(0,v + ','));
//        for (String key: grammar.getKeys()) {
//            System.out.println("DBG1>> " + key + ": " + Pattern.compile(grammar.getPattern(key), Pattern.DOTALL).matcher(value).find());
//        }

        StringBuilder sb = new StringBuilder();
        sb.append("Pattern \n   >>>").append(pattern).append("\n does not match\n   >>>").append(value).append("\n");
        if (sbM.length()>0) {
            sb.append("But it matches the following patterns parts: [");
            sb.append(sbM.toString().substring(0, sbM.length()-1));
            sb.append("]\n\n");
        }


        // Test latest pattern possible
        for (int i=pattern.length()-1; i>0; i--) {
            String currPattern = pattern.substring(0, i);
            try {
                Pattern currPatternCompiled = Pattern.compile(currPattern, Pattern.DOTALL);

                for (int j = value.length()-1; j>1; j--) {
                    String valuePart = value.substring(0, j);
                    if (currPatternCompiled.matcher(valuePart).matches()) {
                        sb.append("Pattern start \n   >>>").append(currPattern).append("\nmatches\n   >>>").append(valuePart).append("\n\n");
                        throw new SyntaxException(sb.toString());
                    }
                }
            } catch (PatternSyntaxException e) {
                // Just ignore
            }
        }

        // Try all patterns
        //grammar.getKeys().stream().filter(k->k.length()>5).filter(p->Pattern.compile(p, Pattern.DOTALL).matcher(value).find()).forEach(v->sb.append("Matching " + v + '\n'));


        throw new SyntaxException(sb.toString());
    }

}
