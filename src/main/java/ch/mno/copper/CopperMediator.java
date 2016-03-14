package ch.mno.copper;

import java.io.FileInputStream;
import java.util.Properties;

/**
 * Created by dutoitc on 14.02.2016.
 */
public class CopperMediator {


    private static final CopperMediator instance = new CopperMediator();
    private CopperDaemon daemon;
    private Properties properties;

    public CopperMediator()  {
        try {
            properties = new Properties();
            properties.load(new FileInputStream("copper.properties"));
        }catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static CopperMediator getInstance() { return instance; }


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

}
