package ch.mno.copper.stories;

import ch.mno.copper.stories.data.StoryGrammar;
import ch.mno.copper.stories.data.StoryValidationResult;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class StoryValidator {

    private StoryGrammar grammar;

    public StoryValidator(StoryGrammar grammar) {
        this.grammar = grammar;
    }

    StoryValidationResult validate(String story) {
        StoryValidationResult result = new StoryValidationResult(story);
        // TODO: validation
        List<String> blacklist = Arrays.asList("EOL", "SPACE_EOL", "SPACE", "CRON_EL");
        for (String key: grammar.getKeys()) {
            if (blacklist.contains(key)) continue;

            String patternFull = grammar.getPatternFull(key);
            String patternShort = grammar.getPattern(key);
            Pattern pattern = Pattern.compile(patternFull);
            if (patternFull.length()<3) continue;
            if (pattern.matcher(story).matches()) {
                result.addPerfectMatch(0, story.length(), key, patternShort, patternFull);
            } else {
                Matcher matcher = pattern.matcher(story);
                while (matcher.find()) {
                    result.addPartialMatch(matcher.start(), matcher.end(), key, patternShort, patternFull);
                }
            }
        }
        return result;
    }

}
