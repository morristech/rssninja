package com.yairkukielka.rssninja.feedly;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;

public class Tag {
    private static final String ID = "id";
    private static final String LABEL = "label";
    private String id;
    private String label;

    public Tag() {
    }

    public Tag(JSONObject jobject) throws JSONException {
        id = jobject.getString(ID);
        label = jobject.getString(LABEL);
    }

    public Tag(String id, String label) {
        this.id = id;
        this.label = label;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    @Override
    public String toString() {
        return "Category [id=" + id + ", label=" + label + "]";
    }

    public class TagDeserializer implements JsonDeserializer<Tag> {

        private String id;
        private String label;

        @Override
        public Tag deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext ctx) throws JsonParseException {
            JsonObject jobject = json.getAsJsonObject();
            id = jobject.get(ID).getAsString();
            label = jobject.get(LABEL).getAsString();
            return new Tag(id, label);
        }
    }
}
