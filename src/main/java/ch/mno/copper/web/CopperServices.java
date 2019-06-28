package ch.mno.copper.web;

import ch.mno.copper.CopperMediator;
import ch.mno.copper.collect.connectors.ConnectorException;
import ch.mno.copper.data.InstantValues;
import ch.mno.copper.data.StoreValue;
import ch.mno.copper.data.ValuesStore;
import ch.mno.copper.helpers.GraphHelper;
import ch.mno.copper.helpers.SyntaxException;
import ch.mno.copper.stories.StoriesFacade;
import ch.mno.copper.stories.Story;
import ch.mno.copper.stories.StoryValidationResult;
import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import it.sauronsoftware.cron4j.Predictor;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.time.*;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.reflect.Type;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

// FIXME: check documentation for http://localhost:30400/swagger.json
// FIXME: complete documentation with https://github.com/swagger-api/swagger-core/wiki/Annotations-1.5.X
@Path("/ws")
@Api
public class CopperServices {

    private final ValuesStore valuesStore;

    public CopperServices() {
        this.valuesStore = CopperMediator.getInstance().getValuesStore();
    }

//    @OPTIONS
//    @Path("{path : .*}")
//    public Response options() {
//        return Response.ok("")
//                .header("Access-Control-Allow-Origin", "*")
//                .header("Access-Control-Allow-Headers", "origin, content-type, accept, authorization")
//                .header("Access-Control-Allow-Credentials", "true")
//                .header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, HEAD")
//                .header("Access-Control-Max-Age", "1209600")
//                .build();
//    }

    @GET
    @Path("ping")
    @Produces(MediaType.TEXT_PLAIN)
    @ApiOperation(value = "Ping method answering 'pong'",
            notes = "Use this to monitor that Copper is up")
    public String test() {
        return "pong";
    }

    @POST
    @Path("validation/story")
    @Produces(MediaType.APPLICATION_JSON)
    //@Consumes(MediaType.WILDCARD_TYPE)
    @ApiOperation(value = "Validation of a posted story",
            notes = "Post a story to this service, and validate it without saving it")
    public StoryValidationResult postStory(String story) throws IOException, ConnectorException {
        StoriesFacade sf = StoriesFacade.getInstance();
        return sf.validate(story);
    }


    @POST
    @Path("story/{storyName}")
    @Produces(MediaType.TEXT_PLAIN)
    @Consumes("application/json")
    @ApiOperation(value = "Method to create a new story",
            notes = "Use this to store a story. If originalStoryName='new', a new story is saved and 'Ok' is returned. otherwise the story will be updated by storyName (originalStoryName)")
    public String postStory(@PathParam("storyName") String storyName, StoryPost post) throws IOException, ConnectorException {
        StoriesFacade sf = StoriesFacade.getInstance();

        // Create
        if (post.originalStoryName.equals("new")) {
            try {
                String msg = sf.saveNewStory(post.storyName, post.storyText);
                if (msg == "Ok") {
                    return "Ok";
                } else {
                    return msg;
                }
            } catch (SyntaxException e) {
                return e.getMessage();
            }
        }


        // Update
        Story story = sf.getStoryByName(storyName);
        if (story == null) {
            throw new RuntimeException("Story " + storyName + " was not found");
        } else {
            String msg = sf.updateStory(post.originalStoryName, post.storyName, post.storyText);
            if (msg == "Ok") {
                return "Ok";
            } else {
                return msg;
            }
        }
    }

    @GET
    @Path("values")
    @Produces(MediaType.TEXT_PLAIN)
    @ApiOperation(value = "Convenience way to retrieve all valid values from Copper",
            notes = "Use this to extract many values, example for remote webservice, angular service, ...")
    public String getValues() {
        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapter(Instant.class, new InstantAdapter());
        Gson gson = builder.create();
        return gson.toJson(valuesStore.getValues());
    }

    @GET
    @Path("values/query")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Retrieve values between date",
            notes = "(from null means from 2000, to null means now). Warning, retrieving many dates could be time-consuming and generate high volume of data")
    public Response getValues(@QueryParam("from") String dateFrom, @QueryParam("to") String dateTo, @QueryParam("columns") String columns, @QueryParam("maxvalues") Integer maxValues) {
        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapter(Instant.class, new InstantAdapter());
        Gson gson = builder.create();

        Instant from = findInstant(dateFrom, Instant.parse("2000-01-01T00:00:00.00Z"), true);
        Instant to = findInstant(dateTo, Instant.now(), false);
        if (maxValues==null) {
            maxValues = 100;
        }
        try {
            List<String> cols = Arrays.asList(columns.split(","));
            return Response.ok(gson.toJson(valuesStore.queryValues(from, to, cols, maxValues)), MediaType.APPLICATION_JSON).build();
        } catch (RuntimeException e) {
            return Response.serverError().entity("SERVER ERROR\n" + e.getMessage()).build();
        }
    }

    @GET
    @Path("instants/query")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Retrieve values between date",
            notes = "")
    public Response getValues(@QueryParam("from") String dateFrom, @QueryParam("to") String dateTo, @QueryParam("columns") String columns, @QueryParam("intervalSeconds") long intervalSeconds,@QueryParam("maxvalues") Integer maxValues) {
        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapter(Instant.class, new InstantAdapter());
        Gson gson = builder.create();


        Instant from = findInstant(dateFrom, Instant.parse("2000-01-01T00:00:00.00Z"), true);
        Instant to = findInstant(dateTo, Instant.now(), false);
        if (maxValues==null) {
            maxValues = 100;
        }
        try {
            List<String> cols = Arrays.asList(columns.split(","));
            List<InstantValues> values = valuesStore.queryValues(from, to, intervalSeconds, cols, maxValues);
            return Response.ok(gson.toJson(values), MediaType.APPLICATION_JSON).build();
        } catch (RuntimeException e) {
            e.printStackTrace();
            return Response.serverError().entity("SERVER ERROR\n" + e.getMessage()).build();
        }
    }

    private Instant toInstant(String date, boolean am) {
        if (date == null || "null".equals(date)) return null;

        String[] formats = new String[]{"dd.MM.yyyy", "yyyy-MM-dd"};
        for (String format : formats) {
            try {
//                System.out.println("Parsing '"+date+"' with '"+format+"'");
                LocalDate ld = LocalDate.parse(date, DateTimeFormatter.ofPattern(format));
                if (am) return LocalDateTime.of(ld, LocalTime.of(0, 0)).toInstant(ZoneOffset.UTC);
                return LocalDateTime.of(ld, LocalTime.of(23, 59, 59)).toInstant(ZoneOffset.UTC);
            } catch (DateTimeParseException e) {
            }
        }

        formats = new String[]{"yyyy-MM-dd HH:mm", "yyyy-MM-dd'T'HH:mm", "dd.MM.yyyy'T'HH:mm"};
        for (String format : formats) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
            try {
                //return (LocalDateTime)formatter.parse(date);
                return LocalDateTime.parse(date, formatter).toInstant(ZoneOffset.UTC);
            } catch (DateTimeParseException e) {

            }
        }
        throw new RuntimeException("Cannot parse '" + date + "'");
    }


    @GET
    @Path("stories")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Retrieve all stories",
            notes = "")
    public String getStories() {
        Gson gson = new GsonBuilder().registerTypeAdapter(StoryWEBDTO.class, new MyStoryAdapter<StoryWEBDTO>()).create();

        List<StoryWEBDTO> stories = StoriesFacade.getInstance().getStories(true).stream()
                .map(s -> new StoryWEBDTO(s))
                .collect(Collectors.toList());
        return gson.toJson(stories);
    }


    @GET
    @Path("story/{storyName}/run")
    @Produces(MediaType.TEXT_PLAIN)
    @ApiOperation(value = "Ask to run a story",
            notes = "Story is run before 3''")
    public String getStoryRun(@PathParam("storyName") String storyName) {
        CopperMediator.getInstance().run(storyName);
        return "Story " + storyName + " marked for execution";
    }

    @GET
    @Path("story/{storyName}/delete")
    @Produces(MediaType.TEXT_PLAIN)
    @ApiOperation(value = "Delete story by name",
            notes = "")
    public String getStoryDelete(@PathParam("storyName") String storyName) {
        StoriesFacade.getInstance().deleteStory(storyName);
        return "Story " + storyName + " deleted.";
    }

    @GET
    @Path("/story/{storyName}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Retrieve story by name",
            notes = "")
    public String getStory(@PathParam("storyName") String storyName) {
        Story story = StoriesFacade.getInstance().getStoryByName(storyName);
        if (story == null) {
            throw new RuntimeException("Story not found");
        } else {
            Gson gson = new GsonBuilder().registerTypeAdapter(StoryWEBDTO.class, new MyStoryAdapter()).create();
            return gson.toJson(new StoryWEBDTO(story));
        }
    }


    @GET
    @Path("value/{valueName}")
    @Produces(MediaType.TEXT_PLAIN)
    @ApiOperation(value = "Retrieve a single value",
            notes = "")
    public String getValue(@PathParam("valueName") String valueName) {
        StoreValue storeValue = valuesStore.getValues().get(valueName);
        if (storeValue == null) {
            throw new RuntimeException("Value not found: " + valueName);
        }
        return storeValue.getValue();
    }


    @POST
    @Path("value/{valueName}")
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.TEXT_PLAIN)
    public String posttValue(@PathParam("valueName") String valueName, String message) {
        valuesStore.put(valueName, message);
        StoreValue storeValue = valuesStore.getValues().get(valueName);

        if (storeValue == null) {
            throw new RuntimeException("Value not found: " + valueName);
        }
        return storeValue.getValue();
    }


    @GET
    @Path("overview")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "View stories name and next run",
            notes = "")
    public String getOverview() {
        Gson gson = new Gson();
        return gson.toJson(buildOverview());
    }



    @GET
    @Path("values/query/png")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    @ApiOperation(value = "Retrieve values between date",
            notes = "(from null means from 2000, to null means now). Warning, retrieving many dates could be time-consuming and generate high volume of data")
    public Response getValuesAsPNG(@QueryParam("from") String dateFrom,
                                   @QueryParam("to") String dateTo,
                                   @QueryParam("columns") String columns,
                                   @QueryParam("ytitle") String yTitle,
                                   @DefaultValue("100") @QueryParam("maxvalues") Integer maxValues,
                                   @DefaultValue("600") @QueryParam("width") Integer width,
                                   @DefaultValue("400") @QueryParam("height") Integer height) {
        Instant from = findInstant(dateFrom, Instant.parse("2000-01-01T00:00:00.00Z"), true);
        Instant to = findInstant(dateTo, Instant.now(), false);

        try {
            // Query
            List<String> cols = Arrays.asList(columns.split(","));
            List<StoreValue> storeValues = valuesStore.queryValues(from, to, cols, maxValues);

            // Graph
            JFreeChart chart = GraphHelper.createChart(storeValues, columns, yTitle);


            // Image
            BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = image.createGraphics();
            g2.setRenderingHint(JFreeChart.KEY_SUPPRESS_SHADOW_GENERATION, true);
            Rectangle r = new Rectangle(0, 0, width, height);
            chart.draw(g2, r);
            BufferedImage chartImage = chart.createBufferedImage(width, height, null);
            // TODO: return values
            byte[] png = ChartUtilities.encodeAsPNG(chartImage);
            return Response.ok(new ByteArrayInputStream(png)).build();
        } catch (RuntimeException|IOException e) {
            return Response.serverError().entity("SERVER ERROR\n" + e.getMessage()).build();
        }
    }

    private Instant findInstant(@QueryParam("from") String dateFrom, Instant defaultValue, boolean b) {
        Instant from;
        if (dateFrom == null) {
            from = defaultValue;
        } else {
            from = toInstant(dateFrom, b);
        }
        return from;
    }


    private Overview buildOverview() {
        Overview overview = new Overview();
        List<Story> stories = StoriesFacade.getInstance().getStories(true);
        overview.overviewStories = new ArrayList<>(stories.size());
        stories.stream().forEach(s -> overview.overviewStories.add(new OverviewStory(s)));
        return overview;
    }


    private static class OverviewStory {
        private String storyId;
        private long nextRun;

        public OverviewStory(Story story) {
            storyId = story.getName();
            if (story.getCron() != null) {
                nextRun = new Predictor(story.getCron()).nextMatchingTime();
            }
        }
    }

    private static class Overview {
        public List<OverviewStory> overviewStories;
    }


    private static class MyStoryAdapter<T extends StoryWEBDTO> extends TypeAdapter<StoryWEBDTO> {
        public StoryWEBDTO read(JsonReader reader) throws IOException {
            return null;
        }

        public void write(JsonWriter writer, StoryWEBDTO storyWebDTO) throws IOException {
            if (storyWebDTO == null) {
                writer.nullValue();
                return;
            }
            Story story = storyWebDTO.getStory();

//            writer.name("story");
            writer.beginObject();

//            writer.beginArray();
            writer.name("name");
            writer.value(story.getName());
            writer.name("cron");
            writer.value(story.getCron());
            writer.name("storyText");
            writer.value(story.getStoryText());
            writer.name("hasError");
            writer.value(story.hasError());
            writer.name("error");
            writer.value(story.getError());
            writer.name("nextRun");
            writer.value(storyWebDTO.getNextRun());
//            writer.endArray();

            writer.endObject();
        }
    }


    private static class StoryPost {
        private String originalStoryName;
        private String storyName;
        private String storyText;

        public String getOriginalStoryName() {
            return originalStoryName;
        }

        public void setOriginalStoryName(String originalStoryName) {
            this.originalStoryName = originalStoryName;
        }

        public String getStoryName() {
            return storyName;
        }

        public void setStoryName(String storyName) {
            this.storyName = storyName;
        }

        public String getStoryText() {
            return storyText;
        }

        public void setStoryText(String storyText) {
            this.storyText = storyText;
        }

    }

    private class InstantAdapter implements JsonSerializer<Instant> {

        @Override
        public JsonElement serialize(Instant instant, Type type, JsonSerializationContext jsonSerializationContext) {
            if (instant==null) {
                return new JsonPrimitive((String)null);
            }
            String sdatetime = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")
                    .withLocale(Locale.getDefault())
                    .withZone(ZoneId.systemDefault())
                    .format(instant);
            return new JsonPrimitive(sdatetime);
        }
    }
}