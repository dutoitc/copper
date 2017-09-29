package ch.mno.copper.web;

import ch.mno.copper.CopperMediator;
import ch.mno.copper.collect.connectors.ConnectorException;
import ch.mno.copper.data.StoreValue;
import ch.mno.copper.data.ValuesStore;
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
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

@Path("/ws")
public class CopperServices {

    private final ValuesStore valuesStore;

    public CopperServices() {
        this.valuesStore = CopperMediator.getInstance().getValuesStore();
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
        return gson.toJson(valuesStore.getValues());
    }

    @GET
    @Path("values/query")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getValues(@QueryParam("from") String dateFrom, @QueryParam("to") String dateTo, @QueryParam("columns") String columns) {
        Gson gson = new Gson();
        Instant from = toInstant(dateFrom, true);
        Instant to;
        if (dateTo==null) {
            to = Instant.now();
        }  else {
            to =toInstant(dateTo, false);
        }
        try {
            return Response.ok(gson.toJson(valuesStore.queryValues(from, to, columns)), MediaType.APPLICATION_JSON).build();
        } catch (RuntimeException e) {
            return Response.serverError().entity("SERVER ERROR\n" + e.getMessage()).build();
        }
    }

    private Instant toInstant(String date, boolean am) {
        if (date==null || "null".equals(date)) return null;

        String[] formats = new String[] {"dd.MM.yyyy", "yyyy-MM-dd"};
        for (String format: formats) {
            try {
//                System.out.println("Parsing '"+date+"' with '"+format+"'");
                LocalDate ld = LocalDate.parse(date, DateTimeFormatter.ofPattern(format));
                if (am) return LocalDateTime.of(ld, LocalTime.of(0, 0)).toInstant(ZoneOffset.UTC);
                return LocalDateTime.of(ld, LocalTime.of(23, 59, 59)).toInstant(ZoneOffset.UTC);
            } catch (DateTimeParseException e) {
            }
        }

        formats = new String[] {"yyyy-MM-dd HH:mm", "yyyy-MM-dd'T'HH:mm", "dd.MM.yyyy'T'HH:mm"};
        for (String format: formats) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
            try {
                //return (LocalDateTime)formatter.parse(date);
                return LocalDateTime.parse(date, formatter).toInstant(ZoneOffset.UTC);
            } catch (DateTimeParseException e) {

            }
        }
        throw new RuntimeException("Cannot parse '" + date+"'");
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
        valuesStore.put(valueName,message);
        StoreValue storeValue = valuesStore.getValues().get(valueName);

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