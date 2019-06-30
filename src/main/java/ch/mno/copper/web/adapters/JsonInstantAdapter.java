package ch.mno.copper.web.adapters;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class JsonInstantAdapter implements JsonSerializer<Instant> {

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