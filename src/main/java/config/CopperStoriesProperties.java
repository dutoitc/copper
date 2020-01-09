package config;

import oracle.ucp.proxy.annotation.Post;
import org.springframework.boot.context.properties.ConfigurationProperties;

import javax.annotation.PostConstruct;

@ConfigurationProperties(prefix = "stories")
public class CopperStoriesProperties {
    private String folder = "stories";

    @PostConstruct
    public void init() {
        String folder = System.getProperty("copper.stories.folder");
        if (folder!=null) {
            this.folder = folder;
        }
    }

    public String getFolder() {
        return folder;
    }

    public void setFolder(String folder) {
        this.folder = folder;
    }
}
