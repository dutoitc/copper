package ch.mno.copper.web.dto;

public class StoryPostDTO {
        private String originalStoryName;
        private String storyName;
        private String storyText;

        public static StoryPostDTO of(String originalStoryName, String storyName, String storyText) {
            StoryPostDTO dto = new StoryPostDTO();
            dto.setOriginalStoryName(originalStoryName);
            dto.setStoryName(storyName);
            dto.setStoryText(storyText);
            return dto;
        }

        public String getOriginalStoryName() {
            return originalStoryName;
        }

        public void setOriginalStoryName(String originalStoryName) {
            this.originalStoryName = originalStoryName;
        }

        public String getStoryName() {
            return storyName;
        }

        public void setStoryName(String storyName) {
            this.storyName = storyName;
        }

        public String getStoryText() {
            return storyText;
        }

        public void setStoryText(String storyText) {
            this.storyText = storyText;
        }

    public boolean isNew() {
        return "new".equals(originalStoryName);
    }
}