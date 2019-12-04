package ch.mno.copper;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;


@SpringBootApplication
@Import({
})
@PropertySources(value = {
        @PropertySource(value = "file:copper.properties", ignoreResourceNotFound = true),
        @PropertySource(value = "file:${copper.properties}", ignoreResourceNotFound = true),
})
public class CopperApplication {

    public static void main(String... args) {
        // Pour éviter un WARN de Atomikos
        System.setProperty("com.atomikos.icatch.registered", "true");
        SpringApplication.run(CopperApplication.class, args);
    }
}
