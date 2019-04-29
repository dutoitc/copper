package ch.mno.copper.web;

import ch.mno.copper.stories.Story;
import it.sauronsoftware.cron4j.Predictor;

/**
 * Created by dutoitc on 29.04.2019.
 */
public class StoryWEBDTO {


    private Story story;
    private long nextRun=0;

    public StoryWEBDTO(Story story) {
        this.story = story;

        if (story.getCron()!=null) {
            this.nextRun = new Predictor(story.getCron()).nextMatchingTime();
        }
    }

    public Story getStory() {
        return story;
    }

    public long getNextRun() {
        return nextRun;
    }
}
