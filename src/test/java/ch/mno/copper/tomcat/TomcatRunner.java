package ch.mno.copper.tomcat;

import ch.mno.copper.CopperApplication;

import java.io.IOException;

public class TomcatRunner {

    public static void main(String... args) throws IOException {
        System.setProperty("copper.properties", "sample/copper.properties");

        CopperApplication.main(args);
    }
}
