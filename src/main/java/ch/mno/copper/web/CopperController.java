package ch.mno.copper.web;

import io.swagger.annotations.ApiOperation;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.MediaType;

import org.jfree.chart.JFreeChart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

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

@RestController
@RequestMapping(value = "/ws", produces = MediaType.APPLICATION_JSON, consumes = MediaType.WILDCARD)
public class CopperController {

    private static Logger LOG = LoggerFactory.getLogger(CopperController.class);

    private final ValuesStore valuesStore;
    private final StoriesFacade storiesFacade;
    private final CopperDaemon daemon;

    @Autowired
    private DiskHelper diskHelper;

    public CopperController(ValuesStore valuesStore, final StoriesFacade storiesFacade, final CopperDaemon daemon) {
        this.valuesStore = valuesStore;
        this.storiesFacade = storiesFacade;
        this.daemon = daemon;
    }

    @GetMapping(value = "/")
    public String root() {
        // See https://stackoverflow.com/questions/32184175/how-to-use-spring-redirect-if-controller-method-returns-responseentity
        return "redirect:swagger.json";
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
    public StoryValidationResult postStory(@RequestBody String story) {
        return getStoriesFacade().validate(story);
    }


    @PostMapping(value = "story/{storyName}", produces = MediaType.TEXT_PLAIN)
    @ApiOperation(value = "Method to create a new story",
            notes = "Use this to store a story. If originalStoryName='new', a new story is saved and 'Ok' is returned. otherwise the story will be updated by storyName (originalStoryName)")
    public ResponseEntity<String> postStory(@PathVariable("storyName") String storyName, @RequestBody StoryPostDTO post) {
        StoriesFacade sf = getStoriesFacade();

        // Create
        if (post.isNew()) {
            try {
                String ret = sf.saveNewStory(post.getStoryName(), post.getStoryText());
                return ResponseEntity.of(Optional.of(ret));
            } catch (Exception e) {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
            }
        }

        // Update
        Story story = sf.getStoryByName(storyName);
        if (story == null) {
            throw new RuntimeException("Story " + storyName + " was not found");
        }
        try {
            String msg = sf.updateStory(post.getOriginalStoryName(), post.getStoryName(), post.getStoryText());
            return ResponseEntity.of(Optional.of(msg));
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    @GetMapping(value = "values", produces = MediaType.TEXT_PLAIN)
    @ApiOperation(value = "Convenience way to retrieve all valid values from Copper",
            notes = "Use this to extract many values, example for remote webservice, angular service, ...")
    public String getValues() {
        try {
            Map<String, StoreValue> values = valuesStore.getValues();
            return buildGson().toJson(values);
        } catch (Exception e) {
            LOG.error("Error", e);
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
    public ResponseEntity<String> getValues(@RequestParam(value = "from", required = false) String dateFrom,
                              @RequestParam(value = "to", required = false) String dateTo,
                              @RequestParam(value = "columns", required = false) String columns,
                              @RequestParam(value = "maxvalues", required = false, defaultValue = "100") Integer maxValues) {
        if (columns == null) {
            return new ResponseEntity<>("Missing 'columns'", HttpStatus.NOT_ACCEPTABLE);
        }
        Instant from = InstantHelper.findInstant(dateFrom, InstantHelper.INSTANT_2000, true);
        Instant to = InstantHelper.findInstant(dateTo, Instant.now(), false);

        try {
            List<String> cols = Arrays.asList(columns.split(","));
            String ret = buildGson().toJson(valuesStore.queryValues(from, to, cols, maxValues));
            return ResponseEntity.of(Optional.of(ret));

        } catch (RuntimeException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    @GetMapping(value = "instants/query")
    @ApiOperation(value = "Retrieve values between date", notes = "")
    public ResponseEntity<String> getValues(@RequestParam(value = "from", required = false) String dateFrom,
                              @RequestParam(value = "to", required = false) String dateTo,
                              @RequestParam(value = "columns", required = false) String columns,
                              @RequestParam(value = "intervalSeconds", required = false) long intervalSeconds,
                              @RequestParam(value = "maxvalues", required = false, defaultValue = "100") Integer maxValues) {

        Instant from = InstantHelper.findInstant(dateFrom, InstantHelper.INSTANT_2000, true);
        Instant to = InstantHelper.findInstant(dateTo, Instant.now(), false);
        try {
            List<String> cols = Arrays.asList(columns.split(","));
            List<InstantValues> values = valuesStore.queryValues(from, to, intervalSeconds, cols, maxValues);
            String ret = buildGson().toJson(values);
            return ResponseEntity.of(Optional.of(ret));
        } catch (RuntimeException e) {
            LOG.error("Error", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }


    @GetMapping("stories")
    @ApiOperation(value = "Retrieve all stories", notes = "")
    public String getStories() {
        Gson gson = new GsonBuilder().registerTypeAdapter(StoryWEBDTO.class, new JsonStoryAdapter<>()).create();

        List<StoryWEBDTO> stories = getStoriesFacade().getStories(true).stream()
                .map(StoryWEBDTO::new)
                .collect(Collectors.toList());
        return gson.toJson(stories);
    }


    @GetMapping(value = "story/{storyName}/run", produces = MediaType.TEXT_PLAIN)
    @ApiOperation(value = "Ask to run a story", notes = "Story is run before 3''")
    public String getStoryRun(@PathVariable("storyName") String storyName) {
        daemon.runStory(storyName);
        return "Story " + storyName + " marked for execution";
    }

    @GetMapping(value = "story/{storyName}/delete", produces = MediaType.TEXT_PLAIN)
    @ApiOperation(value = "Delete story by name", notes = "")
    public String getStoryDelete(@PathVariable("storyName") String storyName) {
        getStoriesFacade().deleteStory(storyName);
        return "Story " + storyName + " deleted.";
    }

    @GetMapping(value = "/story/{storyName}")
    @ApiOperation(value = "Retrieve story by name", notes = "")
    public String getStory(@PathVariable("storyName") String storyName) {
        Story story = getStoriesFacade().getStoryByName(storyName);
        if (story == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Sory " + storyName + " not found");
        }
        Gson gson = new GsonBuilder().registerTypeAdapter(StoryWEBDTO.class, new JsonStoryAdapter<>()).create();
        return gson.toJson(new StoryWEBDTO(story));
    }


    @GetMapping(value = "value/{valueName}", produces = MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Retrieve a single value", notes = "")
    public ResponseEntity<String> getValue(@PathVariable("valueName") String valueName) {
        StoreValue storeValue = valuesStore.getValues().get(valueName);

        if (storeValue == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Value " + valueName + " not found");
        }
        return new ResponseEntity<>(storeValue.getValue(), HttpStatus.OK);
    }


    @PostMapping(value = "value/{valueName}", produces = MediaType.APPLICATION_JSON)
    public ResponseEntity<String> postValue(@PathVariable("valueName") String valueName, @RequestBody String message) {
        valuesStore.put(valueName, message);
        return getValue(valueName);
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
    public HttpEntity<?> getValuesAsPNG(@RequestParam(value = "from", required = false) String dateFrom,
                                        @RequestParam(value = "to", required = false) String dateTo,
                                        @RequestParam(value = "columns", required = false) String columns,
                                        @RequestParam(value = "ytitle", required = false) String yTitle,
                                        @RequestParam(value = "maxvalues", required = false, defaultValue = "100") Integer maxValues,
                                        @RequestParam(value = "width", required = false, defaultValue = "600") Integer width,
                                        @RequestParam(value = "height", required = false, defaultValue = "400") Integer height,
                                        HttpServletResponse response) {
        if (columns == null) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Missing 'columns'");
        }

        Instant from = InstantHelper.findInstant(dateFrom, InstantHelper.INSTANT_2000, true);
        Instant to = InstantHelper.findInstant(dateTo, Instant.now(), false);

        try {
            // Query
            List<String> cols = Arrays.asList(columns.split(","));
            List<StoreValue> storeValues = valuesStore.queryValues(from, to, cols, maxValues);

            // Graph
            JFreeChart chart = GraphHelper.createChart(storeValues, columns, yTitle);
            byte[] png = GraphHelper.toPNG(chart, width, height);

            response.setContentType(org.springframework.http.MediaType.IMAGE_PNG_VALUE);
            StreamUtils.copy(png, response.getOutputStream());
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (RuntimeException | IOException e) {
            LOG.error("Error", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
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