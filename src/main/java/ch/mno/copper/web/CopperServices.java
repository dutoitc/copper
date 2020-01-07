package ch.mno.copper.web;

import ch.mno.copper.collect.connectors.ConnectorException;
import ch.mno.copper.daemon.CopperDaemon;
import ch.mno.copper.helpers.GraphHelper;
import ch.mno.copper.store.StoreValue;
import ch.mno.copper.store.ValuesStore;
import ch.mno.copper.store.data.InstantValues;
import ch.mno.copper.stories.DiskHelper;
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
import io.swagger.annotations.ApiOperation;
import org.jfree.chart.JFreeChart;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.QueryParam;
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

@RestController
@RequestMapping(value = "/ws", produces = MediaType.APPLICATION_JSON, consumes = MediaType.WILDCARD)
public class CopperServices {

    private final ValuesStore valuesStore;
    private final StoriesFacade storiesFacade;
    private final CopperDaemon daemon;

    @Autowired
    private DiskHelper diskHelper;

    public CopperServices(ValuesStore valuesStore, final StoriesFacade storiesFacade, final CopperDaemon daemon) {
        this.valuesStore = valuesStore;
        this.storiesFacade = storiesFacade;
        this.daemon = daemon;
    }

    @GetMapping(value = "/")
    public Response root() {
        return Response.temporaryRedirect(URI.create("swagger.json")).build();
    }

    @GetMapping(value = "/ping", produces = MediaType.TEXT_PLAIN)
    @ApiOperation(value = "Ping method answering 'pong'",
            notes = "Use this to monitor that Copper is up")
    public String test() {
        return "pong";
    }

    @GetMapping(value = "/screens", produces = MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Get embedded screens",
            notes = "A way to get the embedded screens")
    public Map<String, String> getScreens() {
        return diskHelper.findScreens();
    }


    @PostMapping("validation/story")
    @ApiOperation(value = "Validation of a posted story",
            notes = "Post a story to this service, and validate it without saving it")
    public StoryValidationResult postStory(String story) {
        return getStoriesFacade().validate(story);
    }


    @PostMapping(value = "story/{storyName}", produces = MediaType.TEXT_PLAIN)
    @ApiOperation(value = "Method to create a new story",
            notes = "Use this to store a story. If originalStoryName='new', a new story is saved and 'Ok' is returned. otherwise the story will be updated by storyName (originalStoryName)")
    public Response postStory(@PathVariable("storyName") String storyName, StoryPostDTO post) throws IOException, ConnectorException {
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

    @GetMapping(value = "values", produces = MediaType.TEXT_PLAIN)
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

    @GetMapping(value = "values/alerts", produces = MediaType.TEXT_PLAIN)
    @ApiOperation(value = "Find alerts on values volumetry", notes = "Use this to find values with too much store")
    public String getValuesAlerts() {
        return valuesStore.getValuesAlerts();
    }

    @DeleteMapping(value = "values/olderThanOneMonth", produces = MediaType.TEXT_PLAIN)
    @ApiOperation(value = "Delete values older than one month", notes = "Use this to clean data after some time")
    public String deleteValuesOlderThanOneMonth() {
        return valuesStore.deleteValuesOlderThanXDays(30);
    }


    @DeleteMapping(value = "values/olderThanThreeMonth", produces = MediaType.TEXT_PLAIN)
    @ApiOperation(value = "Delete values older than one month", notes = "Use this to clean data after some time")
    public String deleteValuesOlderThanThreeMonth() {
        return valuesStore.deleteValuesOlderThanXDays(90);
    }



    @DeleteMapping(value = "values/bykey/{key}", produces = MediaType.TEXT_PLAIN)
    @ApiOperation(value = "Delete values older than one month", notes = "Use this to clean data after some time")
    public String deleteValuesOfKey(@PathVariable String key) {
        return valuesStore.deleteValuesOfKey(key);
    }


    @GetMapping(value = "values/query")
    @ApiOperation(value = "Retrieve values between date",
            notes = "(from null means from 2000, to null means now). Warning, retrieving many dates could be time-consuming and generate high volume of store")
    public Response getValues(@QueryParam("from") String dateFrom,
                              @QueryParam("to") String dateTo,
                              @QueryParam("columns") String columns,
                              @DefaultValue("100") @QueryParam("maxvalues") Integer maxValues) {
        if (columns == null) return Response.serverError().entity("Missing 'columns'").build();
        Instant from = InstantHelper.findInstant(dateFrom, InstantHelper.INSTANT_2000, true);
        Instant to = InstantHelper.findInstant(dateTo, Instant.now(), false);

        try {
            List<String> cols = Arrays.asList(columns.split(","));
            return Response.ok(buildGson().toJson(valuesStore.queryValues(from, to, cols, maxValues)), MediaType.APPLICATION_JSON).build();
        } catch (RuntimeException e) {
            return Response.serverError().entity("SERVER ERROR\n" + e.getMessage()).build();
        }
    }

    @GetMapping(value = "instants/query")
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


    @GetMapping("stories")
    @ApiOperation(value = "Retrieve all stories",
            notes = "")
    public String getStories() {
        Gson gson = new GsonBuilder().registerTypeAdapter(StoryWEBDTO.class, new JsonStoryAdapter<>()).create();

        List<StoryWEBDTO> stories = getStoriesFacade().getStories(true).stream()
                .map(s -> new StoryWEBDTO(s))
                .collect(Collectors.toList());
        return gson.toJson(stories);
    }


    @GetMapping(value = "story/{storyName}/run", produces = MediaType.TEXT_PLAIN)
    @ApiOperation(value = "Ask to run a story",
            notes = "Story is run before 3''")
    public String getStoryRun(@PathVariable("storyName") String storyName) {
        daemon.runStory(storyName);
        return "Story " + storyName + " marked for execution";
    }

    @GetMapping(value = "story/{storyName}/delete", produces = MediaType.TEXT_PLAIN)
    @ApiOperation(value = "Delete story by name",
            notes = "")
    public String getStoryDelete(@PathVariable("storyName") String storyName) {
        getStoriesFacade().deleteStory(storyName);
        return "Story " + storyName + " deleted.";
    }

    @GetMapping(value = "/story/{storyName}")
    @ApiOperation(value = "Retrieve story by name",
            notes = "")
    public String getStory(@PathVariable("storyName") String storyName) {
        Story story = getStoriesFacade().getStoryByName(storyName);
        if (story == null) {
            throw new RuntimeException("Story not found");
        } else {
            Gson gson = new GsonBuilder().registerTypeAdapter(StoryWEBDTO.class, new JsonStoryAdapter()).create();
            return gson.toJson(new StoryWEBDTO(story));
        }
    }


    @GetMapping(value = "value/{valueName}", produces = MediaType.TEXT_PLAIN)
    @ApiOperation(value = "Retrieve a single value",
            notes = "")
    public String getValue(@PathVariable("valueName") String valueName) {
        StoreValue storeValue = valuesStore.getValues().get(valueName);
        if (storeValue == null) {
            throw new RuntimeException("Value not found: " + valueName);
        }
        return storeValue.getValue();
    }


    @PostMapping(value = "value/{valueName}", produces = MediaType.TEXT_PLAIN)
    public String posttValue(@PathVariable("valueName") String valueName, String message) {
        valuesStore.put(valueName, message);
        StoreValue storeValue = valuesStore.getValues().get(valueName);

        if (storeValue == null) {
            throw new RuntimeException("Value not found: " + valueName);
        }
        return storeValue.getValue();
    }


    @GetMapping("overview")
    @ApiOperation(value = "View stories name and next run",
            notes = "")
    public String getOverview() {
        return buildGson().toJson(buildOverview());
    }


    @GetMapping(value = "values/query/png", produces = MediaType.APPLICATION_OCTET_STREAM)
    @ApiOperation(value = "Retrieve values between date",
            notes = "(from null means from 2000, to null means now). Warning, retrieving many dates could be time-consuming and generate high volume of store")
    public Response getValuesAsPNG(@QueryParam("from") String dateFrom,
                                   @QueryParam("to") String dateTo,
                                   @QueryParam("columns") String columns,
                                   @QueryParam("ytitle") String yTitle,
                                   @DefaultValue("100") @QueryParam("maxvalues") Integer maxValues,
                                   @DefaultValue("600") @QueryParam("width") Integer width,
                                   @DefaultValue("400") @QueryParam("height") Integer height) {
        if (columns == null) return Response.serverError().entity("Missing 'columns'").build();
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
        } catch (RuntimeException | IOException e) {
            e.printStackTrace();
            return Response.serverError().entity("SERVER ERROR\n" + e.getMessage()).build();
        }
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

    public StoriesFacade getStoriesFacade() {
        return storiesFacade;
    }
}