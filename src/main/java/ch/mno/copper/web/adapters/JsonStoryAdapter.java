package ch.mno.copper.web.adapters;

import ch.mno.copper.stories.data.Story;
import ch.mno.copper.web.dto.StoryWEBDTO;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;

public class JsonStoryAdapter extends TypeAdapter<StoryWEBDTO> {
        public StoryWEBDTO read(JsonReader reader) {
            return null;
        }

        public void write(JsonWriter writer, StoryWEBDTO storyWebDTO) throws IOException {
            if (storyWebDTO == null) {
                writer.nullValue();
                return;
            }
            Story story = storyWebDTO.getStory();

            writer.beginObject();

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

            writer.endObject();
        }
    }