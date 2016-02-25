package ch.mno.copper.helpers;

import ch.mno.copper.stories.StoryGrammar;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Created by xsicdt on 09/02/16.
 */
public class SyntaxHelper {


    public static void checkSyntax(StoryGrammar grammar, String pattern, String value) {
        if (Pattern.compile(pattern, Pattern.DOTALL).matcher(value).matches()) return;

        StringBuffer sbM = new StringBuffer();
        grammar.getKeys().stream().filter(p->Pattern.compile(p, Pattern.DOTALL).matcher(value).find()).forEach(v->sbM.insert(0,v + ','));

        StringBuffer sb = new StringBuffer();
        sb.append("Pattern \n   >>>" + pattern + "\n does not match\n   >>>" + value + "\n");
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
                        sb.append("Pattern start \n   >>>" + currPattern + "\nmatches\n   >>>" + valuePart+"\n\n");
                        throw new SyntaxException(sb.toString());
                    }
                }
            } catch (PatternSyntaxException e) {
                // Just ignore
            }
        }

        // Try all patterns
        //grammar.getKeys().stream().filter(k->k.length()>5).filter(p->Pattern.compile(p, Pattern.DOTALL).matcher(value).find()).forEach(v->sb.append("Matching " + v + '\n'));


        throw new RuntimeException(sb.toString());
    }

}
