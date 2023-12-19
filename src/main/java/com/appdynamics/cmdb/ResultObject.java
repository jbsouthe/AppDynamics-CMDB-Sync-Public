package com.appdynamics.cmdb;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class ResultObject implements IResult {
    public List<Map<String, String>> result;
    private transient String keyField = "name";
    private transient long cacheStartTime = System.currentTimeMillis();
    private transient long cacheLastUpdateTime = System.currentTimeMillis();

    @Override
    public boolean isCacheValid (int expireMinutes) {
        return cacheLastUpdateTime - System.currentTimeMillis() + (expireMinutes * 60000) > 0;
    }
    @Override
    public void setKeyField (String key) { this.keyField=key; }
    @Override
    public String getKeyField() { return this.keyField; }
    @Override
    public Map<String,String> getResult(){
        if( result == null || result.isEmpty() ) return null;
        return result.get(0);
    }

    @Override
    public boolean isEmpty () {
        return result.isEmpty();
    }

    @Override
    public boolean update (IResult newResult) {
        this.cacheLastUpdateTime = System.currentTimeMillis();
        if( ! newResult.getResult().equals(this.getResult()) ) {
            this.result = Collections.singletonList(newResult.getResult());
            this.cacheStartTime = System.currentTimeMillis();
            return true;
        }
        return false;
    }

    /* taken out, we are only going to send Strings
    public static class ResultObjectDeserializer implements JsonDeserializer<ResultObject> {
        @Override
        public ResultObject deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
                throws JsonParseException {
            ResultObject resultObject = new ResultObject();
            resultObject.result = new ArrayList<>();

            JsonArray jsonArray = json.getAsJsonObject().getAsJsonArray("result");
            for (JsonElement elem : jsonArray) {
                Map<String, Object> map = new HashMap<>();
                for (Map.Entry<String, JsonElement> entry : elem.getAsJsonObject().entrySet()) {
                    JsonElement value = entry.getValue();
                    if (value.isJsonPrimitive()) {
                        JsonPrimitive primitive = value.getAsJsonPrimitive();
                        if (primitive.isString()) {
                            map.put(entry.getKey(), value.getAsString());
                        } else if (primitive.isNumber()) {
                            map.put(entry.getKey(), value.getAsLong());
                        }
                    }
                }
                resultObject.result.add(map);
            }
            return resultObject;
        }
    }

    public static class ResultObjectSerializer implements JsonSerializer<ResultObject> {
        @Override
        public JsonElement serialize(ResultObject src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject jsonObject = new JsonObject();
            JsonArray jsonArray = new JsonArray();
            for (Map<String, Object> map : src.result) {
                JsonObject mapObject = new JsonObject();
                for (Map.Entry<String, Object> entry : map.entrySet()) {
                    Object value = entry.getValue();
                    if (value instanceof String) {
                        mapObject.addProperty(entry.getKey(), (String) value);
                    } else if (value instanceof Long) {
                        mapObject.addProperty(entry.getKey(), (Long) value);
                    }
                }
                jsonArray.add(mapObject);
            }
            jsonObject.add("result", jsonArray);
            return jsonObject;
        }
    }
     */
}
