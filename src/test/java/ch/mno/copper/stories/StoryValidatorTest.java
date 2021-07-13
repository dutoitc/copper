package ch.mno.copper.stories;

import ch.mno.copper.stories.data.MatchedPattern;
import ch.mno.copper.stories.data.Story;
import ch.mno.copper.stories.data.StoryGrammar;
import ch.mno.copper.stories.data.StoryValidationResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

class StoryValidatorTest {


    private StoryGrammar storyGrammar;

    @BeforeEach
    void init() {
        storyGrammar = new StoryGrammar(Story.class.getResourceAsStream("/StoryGrammar.txt"));
    }

    @Test
    void testInvalid() {
        String story = "invalid";
        StoryValidationResult ret = new StoryValidator(storyGrammar).validate(story);
        assertEquals(0, ret.getPartialMatches().size());
        assertEquals(0, ret.getPerfectMatches().size());
    }

    @Test
    void testValid() {
        String story = "RUN ON CRON */5 * * * *\n" +
                "GIVEN COLLECTOR WEB WITH url=http://localhost:30400\n" +
                "    KEEP responseCode AS COPPER_WEB_RETURN_CODE\n" +
                "THEN STORE VALUES";
        StoryValidationResult ret = new StoryValidator(storyGrammar).validate(story);
        assertEquals("COLLECTOR,COLLECTOR_WEB,CRON_STD,CRON_STD,GIVEN,HTTP_URL,JSON_QUERY,REPORTER,RUN_ON", comparable(ret.getPartialMatches()));
        assertEquals("MAIN", comparable(ret.getPerfectMatches()));
    }

    private String comparable(Set<MatchedPattern> partialMatches) {
        return partialMatches.stream()
                .map(a -> a.getPatternName())
                .sorted()
                .collect(Collectors.joining(","));
    }

}
