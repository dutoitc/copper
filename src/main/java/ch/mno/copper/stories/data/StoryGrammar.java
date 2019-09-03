package ch.mno.copper.stories.data;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Created by dutoitc on 07.02.2016.
 */
public class StoryGrammar {

    public static final String SEPARATOR = "¦";
   private Map<String, String> values = new HashMap<>();

    public StoryGrammar(InputStream source) {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(source, "UTF-8"))) {
            String line;
            int noLine = 0;
            while ((line = br.readLine()) != null) {
                noLine++;
                if (line.length() > 0 && line.charAt(0)!='#') {
                    String[] spl = line.split("::=");
                    if (spl.length != 2) throw new RuntimeException("Wrong story grammar at line " + noLine);
                    values.put(spl[0], spl[1]);

                    // Try to compile
                    try {
                        Pattern.compile(getPatternFull(spl[0]));
                    } catch (PatternSyntaxException e) {
                        throw new RuntimeException("Wrong story grammar at line " + noLine + ": " + e.getMessage());
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getPattern(String key) {
        return values.get(key);
    }

    /** Get pattern with @xx@ values replaced */
    public String getPatternFull(String key) {
        String pat = values.get(key);
        if (pat==null) {
            throw new RuntimeException("Cannot find pattern for " + key);
        }
        while (true) {
            int p1 = pat.indexOf(SEPARATOR);
            if (p1==-1) break;
            int p2 = pat.indexOf(SEPARATOR, p1+1);
            if (p2==-1) throw new RuntimeException("Wrong pattern for " + key + ", missing second '@'");
            pat = pat.substring(0, p1) + getPatternFull(pat.substring(p1+1, p2)) + pat.substring(p2+1);
        }
        return pat;
    }


    public Set<String> getKeys() {
        return values.keySet();
    }
}
