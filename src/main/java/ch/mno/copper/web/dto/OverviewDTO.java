package ch.mno.copper.web.dto;

import java.util.List;

public class OverviewDTO {
    private List<OverviewStoryDTO> overviewStories;

    public List<OverviewStoryDTO> getOverviewStories() {
        return overviewStories;
    }

    public void setOverviewStories(List<OverviewStoryDTO> overviewStories) {
        this.overviewStories = overviewStories;
    }

    public void add(OverviewStoryDTO overviewStoryDTO) {
        overviewStories.add(overviewStoryDTO);
    }
}