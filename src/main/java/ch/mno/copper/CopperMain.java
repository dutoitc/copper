package ch.mno.copper;

import ch.mno.copper.daemon.CopperDaemon;
import ch.mno.copper.store.ValuesStore;
import ch.mno.copper.store.db.DBValuesStore;
import ch.mno.copper.stories.StoriesFacade;
import ch.mno.copper.stories.StoriesFacadeImpl;
import ch.mno.copper.web.WebServer;

/**
 * Created by dutoitc on 29.01.2016.
 */
public class CopperMain {

    /**
     * Start Copper Daemon and Webserver
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        // Properties
        PropertiesProvider propertiesProvider = new PropertiesProvider();
        int serverPort = Integer.parseInt(propertiesProvider.getProperty("serverPort", "30400"));
        String jmxPort = propertiesProvider.getProperty("jmxPort", "30409");
        int dbPort = Integer.parseInt(propertiesProvider.getProperty("dbPort", "0"));

        // Initialize instances
        ValuesStore valuesStore = new DBValuesStore(dbPort);
        StoriesFacade storiesFacade = new StoriesFacadeImpl();
        DataProvider dataProvider = new DataProviderImpl(storiesFacade, valuesStore);

        // Start daemon
        CopperDaemon daemon = new CopperDaemon(dataProvider, jmxPort);
        CopperMediator mediator = new CopperMediator(valuesStore, dataProvider, storiesFacade, daemon, propertiesProvider); // keep instances (used by services)
        Thread threadDaemon = new Thread(daemon);
        threadDaemon.start();



        // Start web server
        WebServer webServer = new WebServer(serverPort);
        Thread threadWebserver = new Thread(webServer);
        threadWebserver.start();

        // infinite loop
        while (1 < 2) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                webServer.stop();
                daemon.stop();
                Thread.sleep(3000);
                System.exit(1);
            }
        }
    }

}
