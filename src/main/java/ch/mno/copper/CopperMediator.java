package ch.mno.copper;

import ch.mno.copper.daemon.CopperDaemon;
import ch.mno.copper.store.ValuesStore;
import ch.mno.copper.stories.StoriesFacade;

/**
 * Created by dutoitc on 14.02.2016.
 */
public class CopperMediator {

//    private static final Logger LOG = LoggerFactory.getLogger(CopperMediator.class);

    private static CopperMediator instance;

    private final PropertiesProvider propertiesProvider;

    /** Where to store values */
    private ValuesStore valuesStore;

    /** Stories and values provider, with cache */
    private DataProvider dataProvider;

    /** Everything that is related to stories */
    private StoriesFacade storiesFacade;

    /** The daemon which run stories */
    private CopperDaemon daemon;


    public CopperMediator(ValuesStore valuesStore, DataProvider dataProvider, StoriesFacade storiesFacade, CopperDaemon daemon, PropertiesProvider propertiesProvider) {
        this.valuesStore = valuesStore;
        this.dataProvider = dataProvider;
        this.storiesFacade = storiesFacade;
        this.daemon = daemon;
        this.propertiesProvider = propertiesProvider;
        CopperMediator.instance = this;
    }

    /** For webservices */
    public static CopperMediator getInstance() {
        return instance;
    }

    public ValuesStore getValuesStore() {
        return valuesStore;
    }

    public DataProvider getDataProvider() {
        return dataProvider;
    }

    public void run(String storyName) {
        daemon.runStory(storyName);
    }

    public StoriesFacade getStoriesFacade() {
        return storiesFacade;
    }

    public String getProperty(String key, String defaultValue) {
        return propertiesProvider.getProperty(key, defaultValue);
    }

    public String getProperty(String key) {
        return propertiesProvider.getProperty(key);
    }


//    public CopperDaemon getCopperDaemon() {
//        return daemon;
//    }



}