package ch.mno.copper.web;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

/**
 * Created by dutoitc on 07.02.2016.
 */
public class WebServer implements Runnable {

    public static final int PORT = 30400;
    private Server server;

    // http://www.eclipse.org/jetty/documentation/current/embedded-examples.html
    @Override
    public void run() {
        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");

        server = new Server(PORT);
//        server.setHandler(context);

        // Resources
        ResourceHandler resource_handler = new ResourceHandler();
        resource_handler.setDirectoriesListed(true);
        resource_handler.setWelcomeFiles(new String[]{"index.html"});
        resource_handler.setResourceBase("src/main/webapp/WEB-INF/");

        // Webservices
//        ServletHandler servletHandler = new ServletHandler();
//        servletHandler.addServletWithMapping(CopperServiceServlet.class, "/ws");
//        servletHandler.addServletWithMapping(CopperServiceServlet.class, "/ws/*");

        // Handlers
        HandlerList handlers = new HandlerList();
        handlers.setHandlers(new Handler[]{resource_handler, context});//servletHandler});
        server.setHandler(handlers);


        // ...
        ServletHolder jerseyServlet = context.addServlet(org.glassfish.jersey.servlet.ServletContainer.class, "/*");
        jerseyServlet.setInitOrder(0);
        jerseyServlet.setInitParameter("jersey.config.server.provider.classnames", CopperServices.class.getCanonicalName());

        // GZip
//        GzipHandler gzip = new GzipHandler();
//        server.setHandler(gzip);
//        gzip.setHandler(handlers);


        // Start things up! By using the server.join() the server thread will join with the current thread.
        // See "http://docs.oracle.com/javase/1.5.0/docs/api/java/lang/Thread.html#join()" for more details.
        try {
            server.start();
            server.join();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void stop() throws Exception {
        server.stop();
    }


}
