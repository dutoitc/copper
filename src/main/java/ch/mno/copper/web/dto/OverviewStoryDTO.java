package ch.mno.copper.web.dto;

import ch.mno.copper.stories.Story;
import it.sauronsoftware.cron4j.Predictor;

public class OverviewStoryDTO {
        private String storyId;
        private long nextRun;

        public OverviewStoryDTO(Story story) {
            storyId = story.getName();
            if (story.getCron() != null) {
                nextRun = new Predictor(story.getCron()).nextMatchingTime();
            }
        }
    }