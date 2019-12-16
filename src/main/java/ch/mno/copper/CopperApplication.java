package ch.mno.copper;

import config.CopperMailProperties;
import config.CopperServicesConfig;
import config.CopperStoriesProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.ApplicationPidFileWriter;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;

import java.io.File;
import java.io.IOException;


@SpringBootApplication
@Import({
        CopperServicesConfig.class,
        CopperMailProperties.class,
        CopperStoriesProperties.class
})
@PropertySources(value = {
        @PropertySource(value = "file:copper.properties", ignoreResourceNotFound = true),
        @PropertySource(value = "file:${copper.properties}", ignoreResourceNotFound = true),
})
public class CopperApplication {

    public static void main(String... args) throws IOException {
        System.out.println("Current working directory: " + new File(".").getCanonicalPath());

        final SpringApplication springApplication = new SpringApplication(CopperApplication.class);

        final String pidFile = System.getProperty("copper.pidfile");
        if (pidFile != null) {
            System.out.println("Writing PID in " + pidFile);
            springApplication.addListeners(new ApplicationPidFileWriter(new File(pidFile)));
        }

        springApplication.run(args);
    }
}
