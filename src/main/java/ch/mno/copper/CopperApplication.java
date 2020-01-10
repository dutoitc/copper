package ch.mno.copper;

import config.*;
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
        DataSourceConfig.class,
        CopperServicesConfig.class,
        CopperMailProperties.class,
        CopperStoriesProperties.class,
        CopperScreensProperties.class
})
@PropertySources(value = {
        @PropertySource(value = "file:copper.properties", ignoreResourceNotFound = true),
        @PropertySource(value = "file:${copper.properties}", ignoreResourceNotFound = true),
        @PropertySource(value = "file:src/test/resources/copper.properties", ignoreResourceNotFound = true), // FIXME: better way to do this ? test configuration profile ?
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
