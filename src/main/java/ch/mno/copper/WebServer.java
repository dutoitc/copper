package ch.mno.copper;

import ch.mno.copper.stories.Story;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import it.sauronsoftware.cron4j.Predictor;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.server.handler.gzip.GzipHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Created by dutoitc on 07.02.2016.
 */
public class WebServer implements Runnable {

    private Server server;

    public void WebServer() {

    }

    // http://www.eclipse.org/jetty/documentation/current/embedded-examples.html
    @Override
    public void run() {
        server = new Server(30400);

        ResourceHandler resource_handler = new ResourceHandler();
        resource_handler.setDirectoriesListed(true);
        resource_handler.setWelcomeFiles(new String[]{ "index.html" });
        resource_handler.setResourceBase("src/main/webapp/WEB-INF/");


        ServletContextHandler contextHandler = new ServletContextHandler(ServletContextHandler.SESSIONS);
        contextHandler.setResourceBase("src/main/webapp/WEB-INF/");
        contextHandler.setContextPath("/copper/web");



        // Webservices
        ServletHandler servletHandler = new ServletHandler();
        servletHandler.addServletWithMapping(CopperServiceServlet.class, "/ws");
        servletHandler.addServletWithMapping(CopperServiceServlet.class, "/ws/*"); // FIXME
//        servletHandler.addServletWithMapping(CopperServiceServlet.class, "/ws/value"); // FIXME
//        servletHandler.addServletWithMapping(CopperServiceServlet.class, "/ws/values"); // FIXME
//        servletHandler.addServletWithMapping(CopperServiceServlet.class, "/ws/stories"); // FIXME
//        servletHandler.addServletWithMapping(CopperServiceServlet.class, "/ws/story"); // FIXME
//        servletHandler.addServletWithMapping(CopperServiceServlet.class, "/ws/overview"); // FIXME

        HandlerList handlers = new HandlerList();
        handlers.setHandlers(new Handler[] { resource_handler , servletHandler});
        //resource_handler, servletHandler, contextHandler, new DefaultHandler()

        // GZip
        GzipHandler gzip = new GzipHandler();
        server.setHandler(gzip);
        gzip.setHandler(handlers);




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

    @SuppressWarnings("serial")
    public static class CopperServiceServlet extends HttpServlet
    {
        private final ValuesStore valueStore;
        private final List<Story> stories;

        public CopperServiceServlet() {
            this.valueStore = ValuesStore.getInstance();
            this.stories = CopperMediator.getInstance().getStories();
        }

        @Override
        protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
            PrintWriter pw = response.getWriter();
            String requestUri = request.getRequestURI();
            if (requestUri.contains("/story/")) {
                serveStoryPOST(request, response, pw, requestUri);
            } else {
                pw.write("Unsupported request: " + requestUri);
            }
        }

        @Override
        protected void doGet( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException
        {
            PrintWriter pw = response.getWriter();
            String requestUri = request.getRequestURI();
            if (requestUri.contains("/values")) {
                serveValues(response, pw);
            } else if (requestUri.contains("/stories")) {
                serveStories(response, pw);
            } else if (requestUri.contains("/story/") && requestUri.endsWith("/run")) {
                serveStoryRun(response, pw, requestUri);
            } else if (requestUri.contains("/story/") && !requestUri.endsWith("/run")) {
                serveStory(response, pw, requestUri);
            } else if (requestUri.contains("/value/")) {
                serveValue(response, pw, requestUri);
            } else if (requestUri.contains("/overview")) {
                serveOverview(pw);
            } else {
                serveRoot(response, pw);
            }
            pw.flush();
        }


        private void serveRoot(HttpServletResponse response, PrintWriter pw) {
            response.setContentType("text/html");
            response.setStatus(HttpServletResponse.SC_OK);
            pw.println("<html><head>");
            pw.println("<style type='text/css'>");
            pw.println("table{border: 1px solid blue;width:60%;margin:auto;text-align:center}");
            pw.println("table th {border: 1px solid lightblue;padding: 5px;background: lightblue}");
            pw.println("table td {border: 1px solid lightblue;padding: 5px;}");
            pw.println("</style>");
            pw.println("</head><body>");
            pw.println("<h1>Copper values</h1>");
            pw.println("<table><thead><th>Key</th><th>Value</th><th>Timestamp</th></thead><tbody>");
            valueStore.getValues().forEach((key, value) -> pw.println("<tr><td><a href=\"/ws/value/" + key + "\">" + key + "</a></td><td>" + value.getValue() + "</td><td>"+value.getTimestamp()+"</td>\n"));
            pw.println("</tbody></table>");
            pw.println("<br/><br/>");
            pw.println("<h2>Stories:</h2>");
            stories.forEach(s-> {
                pw.println("<a title=\"" + s.getStoryText().replaceAll("\"", "\\\"") + "\">" + s.getName() + "</a> @" + s.getCron());
            });
            pw.println("</body></html>");
        }

        private void serveOverview(PrintWriter pw) {
            Gson gson = new Gson();
            pw.write(gson.toJson(buildOverview()));
        }

        private void serveValues(HttpServletResponse response, PrintWriter pw) {
            response.setContentType("application/json");
            response.setStatus(HttpServletResponse.SC_OK);
            Gson gson = new Gson();
            pw.write(gson.toJson(valueStore.getValues()));
        }

        private void serveStories(HttpServletResponse response, PrintWriter pw) {
            response.setContentType("application/json");
            response.setStatus(HttpServletResponse.SC_OK);

            Gson gson = new GsonBuilder().registerTypeAdapter(Story.class, new MyStoryAdapter<Story>()).create();

            pw.write(gson.toJson(stories));
        }

        private void serveStory(HttpServletResponse response, PrintWriter pw, String requestUri) {
            Matcher matcher = Pattern.compile("\\/story\\/(.*)").matcher(requestUri);
            if (matcher.find()) {
                List<Story> lstMatches = stories.stream().filter(s -> s.getName().equals(matcher.group(1))).collect(Collectors.toList());
                if (lstMatches.size()==0) {
                    response.setContentType("text/text");
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    pw.write("Story " + matcher.group(1) + " was not found");
                } else if (lstMatches.size()>1) {
                    response.setContentType("text/text");
                    response.setStatus(HttpServletResponse.SC_MULTIPLE_CHOICES);
                    pw.write("Story " + matcher.group(1) + " has too much results");
                } else {
                    Gson gson = new GsonBuilder().registerTypeAdapter(Story.class, new MyStoryAdapter<Story>()).create();
                    pw.write(gson.toJson(lstMatches));
                }
            } else {
                response.setContentType("text/text");
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                pw.write("Story name was not found");
            }
        }


        private void serveStoryPOST(HttpServletRequest request, HttpServletResponse response, PrintWriter pw, String requestUri) throws IOException {
            Matcher matcher = Pattern.compile("\\/story\\/(.*)").matcher(requestUri);
            if (matcher.find()) {
                List<Story> lstMatches = stories.stream().filter(s -> s.getName().equals(matcher.group(1))).collect(Collectors.toList());
                if (lstMatches.size()==0) {
                    response.setContentType("text/text");
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    pw.write("Story " + matcher.group(1) + " was not found");
                } else if (lstMatches.size()>1) {
                    response.setContentType("text/text");
                    response.setStatus(HttpServletResponse.SC_MULTIPLE_CHOICES);
                    pw.write("Story " + matcher.group(1) + " has too much results");
                } else {
                    StringBuffer jb = new StringBuffer();
                    String line = null;
                    try {
                        BufferedReader reader = request.getReader();
                        while ((line = reader.readLine()) != null)
                            jb.append(line);
                    } catch (Exception e) { /*report an error*/ }
                    System.out.println(jb);

//
//
//
                    StoryPost post = new GsonBuilder().create().fromJson(jb.toString(), StoryPost.class);
//                    StoryPost post = StoryPost.build(request);

                    // TODO: update from request
                    response.setContentType("text/text");
                    response.setStatus(HttpServletResponse.SC_OK);
                    pw.write("Ok");
                }
            } else {
                response.setContentType("text/text");
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                pw.write("Story name was not found");
            }
        }

        private void serveValue(HttpServletResponse response, PrintWriter pw, String requestUri) {
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
        }

        private void serveStoryRun(HttpServletResponse response, PrintWriter pw, String requestUri) {
            Matcher matcher = Pattern.compile("\\/story\\/(.*?)\\/run").matcher(requestUri);
            if (matcher.find()) {
                CopperMediator.getInstance().run(matcher.group(1));
                response.setContentType("text/text");
                response.setStatus(HttpServletResponse.SC_OK);
                pw.write("Story " + matcher.group(1) + " marked for execution");
            } else {
                response.setContentType("text/text");
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                pw.write("Story name was not found");
            }
        }

        private Overview buildOverview() {
            Overview overview = new Overview();
            overview.overviewStories = new ArrayList<>(stories.size());
            stories.stream().forEach(s->overview.overviewStories.add(new OverviewStory(s)));
            return overview;
        }
    }

    private static class OverviewStory {
        private String storyId;
        private long nextRun;
        public OverviewStory(Story story) {
            storyId = story.getName();
            nextRun = new Predictor(story.getCron()).nextMatchingTime();
        }
    }

    private static class Overview {
        public List<OverviewStory> overviewStories;
    }

    private static class StoryPost  {
        private String originalStoryName;
        private String storyName;
        private String storyText;
        private String cron;

        public static StoryPost build(HttpServletRequest request) {
            StoryPost p = new StoryPost();
            p.originalStoryName = request.getParameter("originalStoryName");
            p.storyName = request.getParameter("storyName");
            p.storyText = request.getParameter("storyText");
            p.cron = request.getParameter("cron");
            return p;
        }
    }

    private static class MyStoryAdapter<T> extends TypeAdapter<T> {
        public T read(JsonReader reader) throws IOException {
            return null;
        }

        public void write(JsonWriter writer, T obj) throws IOException {
            if (obj == null) {
                writer.nullValue();
                return;
            }
            Story story = (Story) obj;

//            writer.name("story");
            writer.beginObject();

//            writer.beginArray();
            writer.name("name");
            writer.value(story.getName());
            writer.name("cron");
            writer.value(story.getCron());
            writer.name("storyText");
            writer.value(story.getStoryText());
//            writer.endArray();

            writer.endObject();
        }
    }

}
