package ch.mno.copper.tomcat;

import ch.mno.copper.CopperApplication;

import java.io.IOException;

/**
 * TomcatRunner pour les besoins du développeur *copper*
 * Tout est pré-configuré
 */
public class CopperTomcatRunner {

    public static void main(String... args) throws IOException {
        System.setProperty("copper.properties", "sample/copper.properties");
        CopperApplication.main(args);
    }
}
