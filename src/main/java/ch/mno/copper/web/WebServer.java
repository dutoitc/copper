package ch.mno.copper.web;

import ch.mno.copper.web.filters.CORSFilter;
import ch.mno.copper.web.filters.LoggingRequestFilter;
import ch.mno.copper.web.filters.RestrictedOperationsRequestFilter;
import io.swagger.jaxrs.config.BeanConfig;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.servlet.ServletMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * Webserver serving /  /ext  /ws
 * Created by dutoitc on 07.02.2016.
 */
public class WebServer implements Runnable, AutoCloseable {

    private static final Logger LOG = LoggerFactory.getLogger(WebServer.class);
    public static int PORT;

    private Server server;

    public WebServer(int port) {
        PORT = port;
    }

    private static String context = "/";

    static {
        String property = System.getProperty("copper.context");
        if (property!=null) {
            context = property;
        }
    }

    // http://www.eclipse.org/jetty/documentation/current/embedded-examples.html
    @Override
    public void run() {
        server = new Server(PORT);

        // Handlers
        ServletContextHandler rootHandler = buildRootHandler();
        HandlerList handlers = new HandlerList();
        handlers.setHandlers(new Handler[]{buildResourcesContextHandler(), buildExtContextHandler(), rootHandler});//servletHandler});
        server.setHandler(handlers);


        // ...
        ServletHolder jerseyServlet = rootHandler.addServlet(org.glassfish.jersey.servlet.ServletContainer.class, "/*");
        jerseyServlet.setInitOrder(0);
        jerseyServlet.setInitParameter("jersey.config.server.provider.classnames", "io.swagger.jaxrs.listing.ApiListingResource,"+
                "io.swagger.jaxrs.listing.SwaggerSerializers,"+
                RestrictedOperationsRequestFilter.class.getCanonicalName() + "," + // TODO: add in copper.properties
                LoggingRequestFilter.class.getCanonicalName() + "," +
                CORSFilter.class.getCanonicalName()+","+
                CopperServices.class.getCanonicalName());

        // Swagger
        BeanConfig beanConfig = new BeanConfig();
        beanConfig.setVersion("1.0.2");
        beanConfig.setSchemes(new String[]{"http"});
        beanConfig.setHost("localhost:"+PORT);
        beanConfig.setBasePath(context);
        beanConfig.setResourcePackage("ch.mno.copper.web");
        beanConfig.setScan(true);


//        jerseyServlet = rootHandler.addServlet(org.glassfish.jersey.servlet.ServletContainer.class, "/openapi/*");
//        jerseyServlet.setInitOrder(1);
//        jerseyServlet.setInitParameter("jersey.config.server.provider.classnames", OpenApiServlet.class.getCanonicalName());

        // GZip
//        GzipHandler gzip = new GzipHandler();
//        server.setHandler(gzip);
//        gzip.setHandler(handlers);


        // Start things up! By using the server.join() the server thread will join with the current thread.
        // See "http://docs.oracle.com/javase/1.5.0/docs/api/java/lang/Thread.html#join()" for more details.
        try {
            server.start();
            PORT = ((ServerConnector)server.getConnectors()[0]).getLocalPort();
            LOG.info("Check server at http://localhost:" + PORT + context + "/");
            LOG.info("Check server at http://localhost:" + PORT + context + "/admin");
            LOG.info("                http://localhost:" + PORT + context + "/ext");
            LOG.info("                http://localhost:" + PORT + context + "/ws");
            LOG.info("                http://localhost:" + PORT + context + "/swagger.json");
            LOG.info("      DB at     http://127.0.1.1:35251");
            server.join();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private ServletContextHandler buildRootHandler() {
        ServletContextHandler rootHandler = new ServletContextHandler(ServletContextHandler.SESSIONS);
        rootHandler.setContextPath(context);
        return rootHandler;
    }

    /**
     * Expose WEB-INF/* as resources
     * @return
     */
    private AbstractHandler buildResourcesContextHandler() {
        String webDir = WebServer.class.getClassLoader().getResource("WEB-INF").toExternalForm();
        ResourceHandler resourceHandler = new ResourceHandler();
        resourceHandler.setDirectoriesListed(true);
        resourceHandler.setWelcomeFiles(new String[]{"index.html"});
        resourceHandler.setResourceBase(webDir);
        LOG.info("Serving files from " + webDir);


        ContextHandler context1 = new ContextHandler(context);
        context1.setHandler(resourceHandler);
        return context1;
    }

    /**
     * Expose folder externalweb as /ext
     * @return
     */
    private ContextHandler buildExtContextHandler() {
        String webDirExt = new File("externalweb").getAbsolutePath();
        ResourceHandler resourceHandlerExt = new ResourceHandler();
        resourceHandlerExt.setDirectoriesListed(true);
        resourceHandlerExt.setWelcomeFiles(new String[]{"index.html"});
        resourceHandlerExt.setResourceBase("externalweb");

        ContextHandler extHandler = new ContextHandler(context + "/ext"); /* the server uri path */
        extHandler.setHandler(resourceHandlerExt);
        LOG.info("Serving ext files from " + webDirExt);
        return extHandler;
    }

    public void stop() throws Exception {
        if (server!=null) {
            server.stop();
        }
    }


    public int getPort() {
        return PORT;
    }

    @Override
    public void close() throws Exception {
        stop();
    }
}
