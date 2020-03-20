package ch.mno.copper.web;

import ch.mno.copper.collect.connectors.ConnectorException;
import ch.mno.copper.daemon.CopperDaemon;
import ch.mno.copper.store.ValuesStore;
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
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping(value = "/ws/admin", produces = MediaType.APPLICATION_JSON, consumes = MediaType.WILDCARD)
public class CopperAdminServices {

    private final ValuesStore valuesStore;
    private final StoriesFacade storiesFacade;
    private final CopperDaemon daemon;

    @Autowired
    private DiskHelper diskHelper;

    public CopperAdminServices(ValuesStore valuesStore, final StoriesFacade storiesFacade, final CopperDaemon daemon) {
        this.valuesStore = valuesStore;
        this.storiesFacade = storiesFacade;
        this.daemon = daemon;
    }


    @PostMapping(value = "story/{storyName}", produces = MediaType.TEXT_PLAIN)
    @ApiOperation(value = "Method to create a new story",
            notes = "Use this to store a story. If originalStoryName='new', a new story is saved and 'Ok' is returned. otherwise the story will be updated by storyName (originalStoryName)")
    public ResponseEntity<String> postStory(@PathVariable("storyName") String storyName, @RequestBody StoryPostDTO post) throws IOException, ConnectorException {
        StoriesFacade sf = storiesFacade;

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
        } else {
            try {
                String msg = sf.updateStory(post.getOriginalStoryName(), post.getStoryName(), post.getStoryText());
                return ResponseEntity.of(Optional.of(msg));
            } catch (Exception e) {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
            }
        }
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
        storiesFacade.deleteStory(storyName);
        return "Story " + storyName + " deleted.";
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

    @GetMapping("stories")
    @ApiOperation(value = "Retrieve all stories",
            notes = "")

    public String getStories() {
        Gson gson = new GsonBuilder().registerTypeAdapter(StoryWEBDTO.class, new JsonStoryAdapter<>()).create();

        List<StoryWEBDTO> stories = storiesFacade.getStories(true).stream()
                .map(StoryWEBDTO::new)
                .collect(Collectors.toList());
        return gson.toJson(stories);
    }


    @GetMapping(value = "story/{storyName}")
    @ApiOperation(value = "Retrieve story by name",
            notes = "")
    public String getStory(@PathVariable("storyName") String storyName) {
        Story story = storiesFacade.getStoryByName(storyName);
        if (story == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Sory " + storyName + " not found");
        } else {
            Gson gson = new GsonBuilder().registerTypeAdapter(StoryWEBDTO.class, new JsonStoryAdapter()).create();
            return gson.toJson(new StoryWEBDTO(story));
        }
    }

    @GetMapping("overview")
    @ApiOperation(value = "View stories name and next run",
            notes = "")
    public String getOverview() {
        return buildGson().toJson(buildOverview());
    }


    @PostMapping("validation/story")
    @ApiOperation(value = "Validation of a posted story",
            notes = "Post a story to this service, and validate it without saving it")
    public StoryValidationResult validateStory(@RequestBody String story) {
        return storiesFacade.validate(story);
    }



    private OverviewDTO buildOverview() {
        OverviewDTO overview = new OverviewDTO();
        List<Story> stories = storiesFacade.getStories(true);
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