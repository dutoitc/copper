package ch.mno.copper.tomcat;

import ch.mno.copper.CopperApplication;

import java.io.IOException;

public class TomcatRunner {

    public static void main(String... args) throws IOException {
        System.setProperty("copper.properties", "sample/copper.properties");
        //System.setProperty("logging.config", "sample/logback-spring.xml");

        CopperApplication.main(args);
    }
}
