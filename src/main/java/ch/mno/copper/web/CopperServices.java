package ch.mno.copper.web;

import ch.mno.copper.CopperMediator;
import ch.mno.copper.ValuesStore;
import ch.mno.copper.collect.connectors.ConnectorException;
import ch.mno.copper.helpers.SyntaxException;
import ch.mno.copper.stories.StoriesFacade;
import ch.mno.copper.stories.Story;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import it.sauronsoftware.cron4j.Predictor;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Path("/ws")
public class CopperServices {

    private final ValuesStore valueStore;

    public CopperServices() {
        this.valueStore = ValuesStore.getInstance();
    }
 
    @GET
    @Path("ping")
    @Produces(MediaType.TEXT_PLAIN)
    public String test() {
        return "pong";
    }


    @POST
    @Path("story/{storyName}")
    @Produces(MediaType.TEXT_PLAIN)
    @Consumes("application/json")
    public String postStory(@PathParam("storyName") String storyName, StoryPost post) throws IOException, ConnectorException {
        StoriesFacade sf = StoriesFacade.getInstance();

        // Create
        if (post.originalStoryName.equals("new")) {
            try {
                String msg = sf.saveNewStory(post.storyName, post.storyText);
                if (msg == "Ok") {
                    return "Ok";
                } else {
                    throw new RuntimeException("Cannot save story: " + msg);
                }
            } catch (SyntaxException e) {
                return e.getMessage();
            }
        }


        // Update
        Story story = sf.getStoryByName(storyName);
        if (story==null) {
            throw new RuntimeException("Story " + storyName + " was not found");
        } else {
                String msg = sf.updateStory(post.originalStoryName, post.storyName, post.storyText);
                if (msg=="Ok") {
                    return "Ok";
                } else {
                    throw new RuntimeException("Cannot save story: " + msg);
                }
            }
    }

    @GET
    @Path("values")
    @Produces(MediaType.TEXT_PLAIN)
    public String getValues() {
        Gson gson = new Gson();
        return gson.toJson(valueStore.getValues());
    }


    @GET
    @Path("stories")
    @Produces(MediaType.APPLICATION_JSON)
    public String getStories() {
        Gson gson = new GsonBuilder().registerTypeAdapter(Story.class, new MyStoryAdapter<Story>()).create();

        List<Story> stories = StoriesFacade.getInstance().getStories(true);
        return gson.toJson(stories);
    }


    @GET
    @Path("story/{storyName}/run")
    @Produces(MediaType.TEXT_PLAIN)
    public String getStoryRun(@PathParam("storyName") String storyName) {
        CopperMediator.getInstance().run(storyName);
        return "Story " + storyName + " marked for execution";
    }

    @GET
    @Path("story/{storyName}/delete")
    @Produces(MediaType.TEXT_PLAIN)
    public String getStoryDelete(@PathParam("storyName") String storyName) {
        StoriesFacade.getInstance().deleteStory(storyName);
        return "Story " + storyName + " deleted.";
    }

    @GET
    @Path("/story/{storyName}")
    @Produces(MediaType.APPLICATION_JSON)
    public String getStory(@PathParam("storyName") String storyName) {
        Story story = StoriesFacade.getInstance().getStoryByName(storyName);
        if (story==null) {
            throw new RuntimeException("Story not found");
        } else {
            Gson gson = new GsonBuilder().registerTypeAdapter(Story.class, new MyStoryAdapter<Story>()).create();
            return gson.toJson(story);
        }
    }


    @GET
    @Path("value/{valueName}")
    @Produces(MediaType.TEXT_PLAIN)
    public String getValue(@PathParam("valueName") String valueName) {
        ValuesStore.StoreValue storeValue = valueStore.getValues().get(valueName);
        if (storeValue == null) {
            throw new RuntimeException("Value not found: " + valueName);
        }
        return storeValue.getValue();
    }

    @GET
    @Path("overview")
    @Produces(MediaType.APPLICATION_JSON)
    public String getOverview() {
        Gson gson = new Gson();
        return gson.toJson(buildOverview());
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
            if (story.getCron()!=null) {
                nextRun = new Predictor(story.getCron()).nextMatchingTime();
            }
        }
    }

    private static class Overview {
        public List<OverviewStory> overviewStories;
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

}