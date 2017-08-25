package ch.mno.copper.web;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * Created by dutoitc on 07.02.2016.
 */
public class WebServer implements Runnable {

    private static final Logger LOG = LoggerFactory.getLogger(WebServer.class);
    public static int PORT;

    private Server server;

    public WebServer() {
        this(30400);
    }

    public WebServer(int port) {
        PORT = port;
    }

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

        String webDir = WebServer.class.getClassLoader().getResource("WEB-INF").toExternalForm();
        LOG.info("Serving files from " + webDir);
        resource_handler.setResourceBase(webDir);

//        if (new File("src").exists()) {
//            resource_handler.setResourceBase("src/main/webapp/WEB-INF/");
//        } else {
//            resource_handler.setResourceBase(".");
//        }

        // Webservices
//        ServletHandler servletHandler = new ServletHandler();
//        servletHandler.addServletWithMapping(CopperServiceServlet.class, "/ws");
//        servletHandler.addServletWithMapping(CopperServiceServlet.class, "/ws/*");


        // externalweb/* mapped on /ext
        String webDirExt = new File("externalweb").getAbsolutePath();
        LOG.info("Serving ext files from " + webDirExt);
        ResourceHandler resource_handlerExt = new ResourceHandler();
        resource_handlerExt.setDirectoriesListed(true);
        resource_handlerExt.setWelcomeFiles(new String[]{"index.html"});
        resource_handlerExt.setResourceBase("externalweb");
        ContextHandler ctx = new ContextHandler("/ext"); /* the server uri path */
        ctx.setHandler(resource_handlerExt);


        // Handlers
        HandlerList handlers = new HandlerList();
        handlers.setHandlers(new Handler[]{resource_handler, ctx, context});//servletHandler});
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
