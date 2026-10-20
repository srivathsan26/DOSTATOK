package com.dostatok.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.lang.reflect.Type;

/**
 * Standard JSON serialization and deserialization utility using Google Gson.
 */
public final class JsonUtil {
    private static final Gson GSON = new GsonBuilder()
            .setDateFormat("yyyy-MM-dd HH:mm:ss")
            .serializeNulls()
            .create();

    private JsonUtil() {
        // Prevent instantiation
    }

    public static String toJson(Object object) {
        return GSON.toJson(object);
    }

    public static <T> T fromJson(String json, Class<T> classOfT) {
        return GSON.fromJson(json, classOfT);
    }

    public static <T> T fromJson(String json, Type typeOfT) {
        return GSON.fromJson(json, typeOfT);
    }

    public static JsonObject parseObject(String json) {
        if (json == null || json.trim().isEmpty()) {
            return new JsonObject();
        }
        JsonElement element = JsonParser.parseString(json);
        if (element.isJsonObject()) {
            return element.getAsJsonObject();
        }
        return new JsonObject();
    }
}

