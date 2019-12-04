package ch.mno.copper.web;

import ch.mno.copper.CopperMediator;
import ch.mno.copper.collect.connectors.ConnectorException;
import ch.mno.copper.helpers.GraphHelper;
import ch.mno.copper.store.StoreValue;
import ch.mno.copper.store.ValuesStore;
import ch.mno.copper.store.data.InstantValues;
import ch.mno.copper.stories.StoriesFacade;
import ch.mno.copper.stories.data.Story;
import ch.mno.copper.stories.data.StoryValidationResult;
import ch.mno.copper.web.adapters.JsonInstantAdapter;
import ch.mno.copper.web.adapters.JsonStoryAdapter;
import ch.mno.copper.web.dto.OverviewDTO;
import ch.mno.copper.web.dto.OverviewStoryDTO;
import ch.mno.copper.web.dto.StoryPostDTO;
import ch.mno.copper.web.dto.StoryWEBDTO;
import ch.mno.copper.web.helpers.InstantHelper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.jfree.chart.JFreeChart;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
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
    @Path("/")
    public Response root() {
        return Response.temporaryRedirect(URI.create("swagger.json")).build();
    }

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
    @ApiOperation(value = "Validation of a posted story",
            notes = "Post a story to this service, and validate it without saving it")
    public StoryValidationResult postStory(String story) {
        return getStoriesFacade().validate(story);
    }



    @POST
    @Path("story/{storyName}")
    @Produces(MediaType.TEXT_PLAIN)
    @Consumes("application/json")
    @ApiOperation(value = "Method to create a new story",
            notes = "Use this to store a story. If originalStoryName='new', a new story is saved and 'Ok' is returned. otherwise the story will be updated by storyName (originalStoryName)")
    public Response postStory(@PathParam("storyName") String storyName, StoryPostDTO post) throws IOException, ConnectorException {
        StoriesFacade sf = getStoriesFacade();

        // Create
        if (post.isNew()) {
            try {
                String ret = sf.saveNewStory(post.getStoryName(), post.getStoryText());
                return Response.ok(ret).build();
            } catch (Exception e) {
                return Response.status(Response.Status.NOT_ACCEPTABLE).entity(e.getMessage()).build();
            }
        }

        // Update
        Story story = sf.getStoryByName(storyName);
        if (story == null) {
            throw new RuntimeException("Story " + storyName + " was not found");
        } else {
            try {
                String msg = sf.updateStory(post.getOriginalStoryName(), post.getStoryName(), post.getStoryText());
                return Response.ok(msg).build();
            } catch (Exception e) {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
            }
        }
    }

    @GET
    @Path("values")
    @Produces(MediaType.TEXT_PLAIN)
    @ApiOperation(value = "Convenience way to retrieve all valid values from Copper",
            notes = "Use this to extract many values, example for remote webservice, angular service, ...")
    public String getValues() {
        try {
            Map<String, StoreValue> values = valuesStore.getValues();
            String json = buildGson().toJson(values);
            return json;
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @GET
    @Path("values/alerts")
    @ApiOperation(value="Find alerts on values volumetry", notes="Use this to find values with too much store")
    @Produces(MediaType.TEXT_PLAIN)
    public String getValuesAlerts() {
        return valuesStore.getValuesAlerts();
    }

    @DELETE
    @Path("values/olderThanOneMonth")
    @ApiOperation(value="Delete values older than one month", notes="Use this to clean data after some time")
    @Produces(MediaType.TEXT_PLAIN)
    public String deleteValuesOlderThanOneMonth() {
        return valuesStore.deleteValuesOlderThanXDays(30);
    }


    @DELETE
    @Path("values/olderThanThreeMonth")
    @ApiOperation(value="Delete values older than one month", notes="Use this to clean data after some time")
    @Produces(MediaType.TEXT_PLAIN)
    public String deleteValuesOlderThanThreeMonth() {
        return valuesStore.deleteValuesOlderThanXDays(90);
    }


    @GET
    @Path("values/query")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Retrieve values between date",
            notes = "(from null means from 2000, to null means now). Warning, retrieving many dates could be time-consuming and generate high volume of store")
    public Response getValues(@QueryParam("from") String dateFrom,
                              @QueryParam("to") String dateTo,
                              @QueryParam("columns") String columns,
                              @DefaultValue("100") @QueryParam("maxvalues") Integer maxValues) {
        if (columns==null) return Response.serverError().entity("Missing 'columns'").build();
        Instant from = InstantHelper.findInstant(dateFrom, InstantHelper.INSTANT_2000, true);
        Instant to = InstantHelper.findInstant(dateTo, Instant.now(), false);

        try {
            List<String> cols = Arrays.asList(columns.split(","));
            return Response.ok(buildGson().toJson(valuesStore.queryValues(from, to, cols, maxValues)), MediaType.APPLICATION_JSON).build();
        } catch (RuntimeException e) {
            return Response.serverError().entity("SERVER ERROR\n" + e.getMessage()).build();
        }
    }

    @GET
    @Path("instants/query")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Retrieve values between date",
            notes = "")
    public Response getValues(@QueryParam("from") String dateFrom,
                              @QueryParam("to") String dateTo,
                              @QueryParam("columns") String columns,
                              @QueryParam("intervalSeconds") long intervalSeconds,
                              @DefaultValue("100") @QueryParam("maxvalues") Integer maxValues) {


        Instant from = InstantHelper.findInstant(dateFrom, InstantHelper.INSTANT_2000, true);
        Instant to = InstantHelper.findInstant(dateTo, Instant.now(), false);
        try {
            List<String> cols = Arrays.asList(columns.split(","));
            List<InstantValues> values = valuesStore.queryValues(from, to, intervalSeconds, cols, maxValues);
            return Response.ok(buildGson().toJson(values), MediaType.APPLICATION_JSON).build();
        } catch (RuntimeException e) {
            e.printStackTrace();
            return Response.serverError().entity("SERVER ERROR\n" + e.getMessage()).build();
        }
    }


    @GET
    @Path("stories")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Retrieve all stories",
            notes = "")
    public String getStories() {
        Gson gson = new GsonBuilder().registerTypeAdapter(StoryWEBDTO.class, new JsonStoryAdapter<>()).create();

        List<StoryWEBDTO> stories = getStoriesFacade().getStories(true).stream()
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
        getStoriesFacade().deleteStory(storyName);
        return "Story " + storyName + " deleted.";
    }

    @GET
    @Path("/story/{storyName}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Retrieve story by name",
            notes = "")
    public String getStory(@PathParam("storyName") String storyName) {
        Story story = getStoriesFacade().getStoryByName(storyName);
        if (story == null) {
            throw new RuntimeException("Story not found");
        } else {
            Gson gson = new GsonBuilder().registerTypeAdapter(StoryWEBDTO.class, new JsonStoryAdapter()).create();
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
        return buildGson().toJson(buildOverview());
    }



    @GET
    @Path("values/query/png")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    @ApiOperation(value = "Retrieve values between date",
            notes = "(from null means from 2000, to null means now). Warning, retrieving many dates could be time-consuming and generate high volume of store")
    public Response getValuesAsPNG(@QueryParam("from") String dateFrom,
                                   @QueryParam("to") String dateTo,
                                   @QueryParam("columns") String columns,
                                   @QueryParam("ytitle") String yTitle,
                                   @DefaultValue("100") @QueryParam("maxvalues") Integer maxValues,
                                   @DefaultValue("600") @QueryParam("width") Integer width,
                                   @DefaultValue("400") @QueryParam("height") Integer height) {
        if (columns==null) return Response.serverError().entity("Missing 'columns'").build();
        Instant from = InstantHelper.findInstant(dateFrom, InstantHelper.INSTANT_2000, true);
        Instant to = InstantHelper.findInstant(dateTo, Instant.now(), false);

        try {
            // Query
            List<String> cols = Arrays.asList(columns.split(","));
            List<StoreValue> storeValues = valuesStore.queryValues(from, to, cols, maxValues);

            // Graph
            JFreeChart chart = GraphHelper.createChart(storeValues, columns, yTitle);
            byte[] png = GraphHelper.toPNG(chart, width, height);

            // Response as stream
            return Response.ok(new ByteArrayInputStream(png), MediaType.valueOf("image/png")).build();
        } catch (RuntimeException|IOException e) {
            e.printStackTrace();
            return Response.serverError().entity("SERVER ERROR\n" + e.getMessage()).build();
        }
    }


    private StoriesFacade getStoriesFacade() {
        return CopperMediator.getInstance().getStoriesFacade();
    }

    private OverviewDTO buildOverview() {
        OverviewDTO overview = new OverviewDTO();
        List<Story> stories = getStoriesFacade().getStories(true);
        overview.overviewStories = new ArrayList<>(stories.size());
        stories.forEach(s -> overview.overviewStories.add(new OverviewStoryDTO(s)));
        return overview;
    }


    private Gson buildGson() {
        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapter(Instant.class, new JsonInstantAdapter());
        return builder.create();
    }

}