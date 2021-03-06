package config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import javax.annotation.PostConstruct;

@ConfigurationProperties(prefix = "stories")
public class CopperStoriesProperties {
    private String folder = "stories";

    @PostConstruct
    public void init() {
        String folderProperty = System.getProperty("copper.stories.folder");
        if (folderProperty != null) {
            this.folder = folderProperty;
        }
    }

    public String getFolder() {
        return folder;
    }

    public void setFolder(String folder) {
        this.folder = folder;
    }
}
