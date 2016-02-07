package ch.mno.copper;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Created by dutoitc on 07.02.2016.
 */
public class WebServer implements Runnable {

    private static ValuesStore valueStore;

    public void WebServer(ValuesStore valueStore) {
        this.valueStore = valueStore;
    }

    @Override
    public void run() {
        Server server = new Server(30400);

/*
        // Create the ResourceHandler. It is the object that will actually handle the request for a given file. It is
        // a Jetty Handler object so it is suitable for chaining with other handlers as you will see in other examples.
        ResourceHandler resource_handler = new ResourceHandler();
        // Configure the ResourceHandler. Setting the resource base indicates where the files should be served out of.
        // In this example it is the current directory but it can be configured to anything that the jvm has access to.
        resource_handler.setDirectoriesListed(true);
        resource_handler.setWelcomeFiles(new String[]{ "index.html" });
        resource_handler.setResourceBase(".");

        // Add the ResourceHandler to the server.
        GzipHandler gzip = new GzipHandler();
        server.setHandler(gzip);
        HandlerList handlers = new HandlerList();
        handlers.setHandlers(new Handler[] { resource_handler, new DefaultHandler() });
        gzip.setHandler(handlers);
        */

        ServletHandler handler = new ServletHandler();
        server.setHandler(handler);
        handler.addServletWithMapping(CopperServiceServlet.class, "/copper/ws");

        // Start things up! By using the server.join() the server thread will join with the current thread.
        // See "http://docs.oracle.com/javase/1.5.0/docs/api/java/lang/Thread.html#join()" for more details.
        try {
            server.start();
            server.join();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("serial")
    public static class CopperServiceServlet extends HttpServlet
    {
        private final ValuesStore valueStore;

        public CopperServiceServlet() {
            this.valueStore = ValuesStore.getInstance();
        }

        @Override
        protected void doGet( HttpServletRequest request,
                              HttpServletResponse response ) throws ServletException,
                IOException
        {
            PrintWriter pw = response.getWriter();
            String requestUri = request.getRequestURI();
            if (requestUri.contains("/value/")) {
                int p = requestUri.indexOf("/value/");
                ValuesStore.StoreValue storeValue = valueStore.getValues().get(requestUri.substring(p+7));
                if (storeValue==null) {
                    response.setContentType("text/text");
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    pw.write("Key not found: " + storeValue);
                } else {
                    response.setContentType("text/text");
                    response.setStatus(HttpServletResponse.SC_OK);
                    pw.write(storeValue.getValue());
                }
            } else {
                response.setContentType("text/html");
                response.setStatus(HttpServletResponse.SC_OK);
                pw.println("<html><head>");
                pw.println("<style type='text/css'>table{border: 1px solid lightblue;width:60%;margin:auto;} </style>");
                pw.println("</head><body>");
                pw.println("<h1>Copper values</h1>");
                pw.println("<table><thead><th>Key</th><th>Value</th><th>Timestamp</th></thead><tbody>");
                valueStore.getValues().forEach((key, value) -> pw.println("<tr><td><a href=\"/ws/value/" + key + "\">" + key + "</a></td><td>" + value.getValue() + "</td><td>"+value.getTimestamp()+"</td>\n"));
                pw.println("</tbody></table></body></html>");
            }
            pw.flush();
        }
    }

}
