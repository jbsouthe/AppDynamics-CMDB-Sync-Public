package com.appdynamics.controller.apidata.cmdb;

import com.google.gson.*;
import java.lang.reflect.Type;

public class Tag {
    public String key;
    public String value;  // This can be either String or Long

    public Tag(String key, String value) {
        this.key = key;
        this.value = value;
    }

    /* taken out, we are only going to send String
    public static class TagDeserializer implements JsonDeserializer<Tag> {
        @Override
        public Tag deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
                throws JsonParseException {
            JsonObject jsonObject = json.getAsJsonObject();
            JsonElement key = jsonObject.get("key");
            JsonElement value = jsonObject.get("value");

            // Determine the type of 'value'
            if (value.isJsonPrimitive()) {
                JsonPrimitive primitive = value.getAsJsonPrimitive();
                if (primitive.isString()) {
                    return new Tag(key.getAsString(), value.getAsString());
                } else if (primitive.isNumber()) {
                    return new Tag(key.getAsString(), value.getAsLong());
                }
            }
            throw new JsonParseException("Unexpected type for value");
        }
    }

    public static class TagSerializer implements JsonSerializer<Tag> {
        @Override
        public JsonElement serialize(Tag src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("key", src.key);
            if (src.value instanceof String) {
                jsonObject.addProperty("value", (String) src.value);
            } else if (src.value instanceof Long) {
                jsonObject.addProperty("value", (Long) src.value);
            } else {
                throw new JsonParseException("Unexpected type for value");
            }
            return jsonObject;
        }
    }
     */
}

