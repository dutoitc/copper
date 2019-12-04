package ch.mno.copper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Properties;

public class PropertiesProvider {

    private static final Logger LOG = LoggerFactory.getLogger(PropertiesProvider.class);

    /** Copper properties */
    private Properties properties;

    public static String PROPERTIES_FILE = "copper.properties";

    static {
        String folder = System.getProperty("copper.properties");
        if (folder!=null) {
            PROPERTIES_FILE = folder;
        }
    }

    /** Load copper.properties */
    public PropertiesProvider() {
        try {
            properties = new Properties();

            File file = new File(PROPERTIES_FILE);
            if (file.exists()) {
                FileInputStream inStream = new FileInputStream(file);
                properties.load(inStream);
            } else {
                System.err.println("Warning: " + PROPERTIES_FILE + " not found");
            }
        } catch (FileNotFoundException e) {
            throw new RuntimeException("File not found: " + PROPERTIES_FILE + " in " + new File(".").getAbsolutePath()+"; " + e.getMessage());
        }catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public String getProperty(String name) {
        String value = properties.getProperty(name);
        if (value==null) throw new RuntimeException("Missing property: " + name);
        return value;
    }

    public String getProperty(String name, String defaultValue) {
        String value = properties.getProperty(name);
        if (value==null) {
            LOG.info("Missing property " + name + ", using default value " + defaultValue);
            return defaultValue;
        }
        return value;
    }

}
