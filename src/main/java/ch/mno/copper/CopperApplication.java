package ch.mno.copper;

import config.CopperDbProperties;
import config.CopperMailProperties;
import config.CopperServicesConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;


@SpringBootApplication
@Import({
        CopperServicesConfig.class,
        CopperDbProperties.class,
        CopperMailProperties.class
})
@PropertySources(value = {
        @PropertySource(value = "file:copper.properties", ignoreResourceNotFound = true),
        @PropertySource(value = "file:${copper.properties}", ignoreResourceNotFound = true),
})
public class CopperApplication {

    public static void main(String... args) {
        SpringApplication.run(CopperApplication.class, args);
    }
}
