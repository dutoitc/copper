package ch.mno.copper;

import ch.mno.copper.data.DbValuesStore;
import ch.mno.copper.data.ValuesStore;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Properties;

/**
 * Created by dutoitc on 14.02.2016.
 */
public class CopperMediator {


    private static final CopperMediator instance = new CopperMediator();
    private final ValuesStore valuesStore;
    private CopperDaemon daemon;
    private Properties properties;

    private CopperMediator()  {
        try {
            properties = new Properties();

            File file = new File("copper.properties");
            if (file.exists()) {
                FileInputStream inStream = new FileInputStream(file);
                properties.load(inStream);
            } else {
                System.err.println("Warning: copper.properties not found");
            }
            this.valuesStore = DbValuesStore.getInstance();
        } catch (FileNotFoundException e) {
            throw new RuntimeException("File not found: copper.properties in " + new File(".").getAbsolutePath()+"; " + e.getMessage());
        }catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static CopperMediator getInstance() { return instance; }

    public ValuesStore getValuesStore() {
        return valuesStore;
    }

    public void run(String storyName) {
        daemon.runStory(storyName);
    }

    public void registerCopperDaemon(CopperDaemon daemon) {
        this.daemon = daemon;
    }

    public String getProperty(String name) {
        String value = properties.getProperty(name);
        if (value==null) throw new RuntimeException("Missing property: " + name);
        return value;
    }

    public String getProperty(String name, String defaultValue) {
        String value = properties.getProperty(name);
        if (value==null) return defaultValue;
        return value;
    }
}
