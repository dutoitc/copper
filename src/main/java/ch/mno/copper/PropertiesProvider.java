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

    /** Load copper.properties */
    PropertiesProvider() {
        try {
            properties = new Properties();

            File file = new File("copper.properties");
            if (file.exists()) {
                FileInputStream inStream = new FileInputStream(file);
                properties.load(inStream);
            } else {
                System.err.println("Warning: copper.properties not found");
            }
        } catch (FileNotFoundException e) {
            throw new RuntimeException("File not found: copper.properties in " + new File(".").getAbsolutePath()+"; " + e.getMessage());
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
