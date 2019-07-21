package ch.mno.copper;

import ch.mno.copper.daemon.CopperDaemon;
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
        CopperMediator mediator = CopperMediator.getInstance();
        int serverPort = Integer.parseInt(mediator.getProperty("serverPort", "30400"));

        WebServer webServer = new WebServer(serverPort);
        Thread thread = new Thread(webServer);
        thread.start();

        CopperDaemon daemon = CopperDaemon.runWith(new DataproviderImpl(StoriesFacadeImpl.getInstance(), CopperMediator.getInstance().getValuesStore()));
        while (1 < 2) { // Main loop
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
