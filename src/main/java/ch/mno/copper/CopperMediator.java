package ch.mno.copper;

import ch.mno.copper.daemon.CopperDaemon;
import ch.mno.copper.store.db.DBValuesStore;
import ch.mno.copper.store.ValuesStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Properties;

/**
 * Created by dutoitc on 14.02.2016.
 */
public class CopperMediator {

    private static final Logger LOG = LoggerFactory.getLogger(CopperMediator.class);

    private static CopperMediator instance;
    private ValuesStore valuesStore;
    private CopperDaemon daemon;
    private Properties properties;

    private CopperMediator()  {
        loadProperties();
    }

    /** Load copper.properties */
    private void loadProperties() {
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

    public ValuesStore getValuesStore() {
        return valuesStore;
    }

    public void setValuesStore(DBValuesStore valuesStore) {
        this.valuesStore = valuesStore;
    }

    public static CopperMediator getInstance() {
        if (instance==null) {
            synchronized (CopperMediator.class) {
                if (instance==null) {
                    instance = new CopperMediator();
                    instance.setValuesStore(DBValuesStore.getInstance());
                }
            }
        }
        return instance;
    }

    public void run(String storyName) {
        daemon.runStory(storyName);
    }

    public void registerCopperDaemon(CopperDaemon daemon) {
        this.daemon = daemon;
    }

}
