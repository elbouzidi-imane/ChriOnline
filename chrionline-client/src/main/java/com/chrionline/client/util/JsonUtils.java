package com.chrionline.client.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;

import java.lang.reflect.Type;
import java.text.DateFormat;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public final class JsonUtils {
    public static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(Date.class, new FlexibleDateDeserializer())
            .create();

    private JsonUtils() {
    }

    private static final class FlexibleDateDeserializer implements JsonDeserializer<Date> {
        private final List<DateFormat> formats = List.of(
                new SimpleDateFormat("MMM d, yyyy, h:mm:ss a", Locale.ENGLISH),
                new SimpleDateFormat("MMM d, yyyy HH:mm:ss", Locale.ENGLISH),
                new SimpleDateFormat("MMM d, yyyy, h:mm:ss a", Locale.FRENCH),
                new SimpleDateFormat("MMM d, yyyy HH:mm:ss", Locale.FRENCH),
                new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ROOT),
                new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX", Locale.ROOT),
                new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX", Locale.ROOT),
                DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.DEFAULT, Locale.ENGLISH),
                DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.DEFAULT, Locale.FRENCH),
                new SimpleDateFormat("MMM d, yyyy", Locale.ENGLISH),
                new SimpleDateFormat("MMM. d, yyyy", Locale.ENGLISH),
                new SimpleDateFormat("MMM d, yyyy", Locale.FRENCH),
                new SimpleDateFormat("MMM. d, yyyy", Locale.FRENCH),
                new SimpleDateFormat("yyyy-MM-dd", Locale.ROOT),
                DateFormat.getDateInstance(DateFormat.DEFAULT, Locale.ENGLISH),
                DateFormat.getDateInstance(DateFormat.DEFAULT, Locale.FRENCH)
        );

        private FlexibleDateDeserializer() {
            for (DateFormat format : formats) {
                format.setLenient(false);
            }
        }

        @Override
        public Date deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) {
            String value = json.getAsString();
            for (DateFormat format : formats) {
                ParsePosition position = new ParsePosition(0);
                Date parsed = format.parse(value, position);
                if (parsed != null && position.getIndex() == value.length()) {
                    return parsed;
                }
            }
            return null;
        }
    }
}
