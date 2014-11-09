package com.yairkukielka.rssninja.feedly;


import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.yairkukielka.rssninja.toolbox.DateUtils;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;


public class ListEntry {

    private static final String SAVED_SUFFIX = "global.saved";
    private static final String TAGS = "tags";
    private static final String CATEGORIES = "categories";
    private static final String ENGAGEMENT = "engagement";
    private static final String AUTHOR = "author";
    private static final String ID = "id";
    private static final String TITLE = "title";
    private static final String SUMMARY = "summary";
    private static final String UNREAD = "unread";
    private static final String PUBLISHED = "published";
    private static final String VISUAL = "visual";
    private static final String CONTENT = "content";
    private static final String URL = "url";
    private static final String ORIGIN = "origin";
    private static final String STREAM_ID = "streamId";
    private static final String ALTERNATE = "alternate";
    private static final String HREF = "href";
    private String streamId;
	private String id;
	private String title;
	private boolean unread;
	private String originTitle = "";
	private List<Category> categories = new ArrayList<Category>();
	private List<Tag> tags = new ArrayList<Tag>();
	private Date published;
	private String author;
	private String content;
	private String visual;
	private String engagement;
	private boolean popular;
	private String url;
	private boolean saved;

	public ListEntry() {
	}


    public ListEntry(String streamId, String id, String title, boolean unread, String originTitle, List<Category> categories, List<Tag> tags, Date published, String author, String content, String visual, String engagement, boolean popular, String url, boolean saved) {
        this.streamId = streamId;
        this.id = id;
        this.title = title;
        this.unread = unread;
        this.originTitle = originTitle;
        this.categories = categories;
        this.tags = tags;
        this.published = published;
        this.author = author;
        this.content = content;
        this.visual = visual;
        this.engagement = engagement;
        this.popular = popular;
        this.url = url;
        this.saved = saved;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public boolean isUnread() {
        return unread;
    }

    public void setUnread(boolean unread) {
        this.unread = unread;
    }

    public List<Category> getCategories() {
        return categories;
    }

    public void setCategories(List<Category> categories) {
        this.categories = categories;
    }

    public Date getPublished() {
        return published;
    }

    public void setPublished(Date published) {
        this.published = published;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getVisual() {
        return visual;
    }

    public void setVisual(String visual) {
        this.visual = visual;
    }

    public String getEngagement() {
        return engagement;
    }

    public void setEngagement(String engagement) {
        this.engagement = engagement;
    }

    public boolean isPopular() {
        return popular;
    }

    public void setPopular(boolean popular) {
        this.popular = popular;
    }

    public List<Tag> getTags() {
        return tags;
    }

    public void setTags(List<Tag> tags) {
        this.tags = tags;
    }

    public boolean isSaved() {
        return saved;
    }

    public void setSaved(boolean saved) {
        this.saved = saved;
    }

    public String getOriginTitle() {
        return originTitle;
    }

    public void setOriginTitle(String originTitle) {
        this.originTitle = originTitle;
    }

    public String getStreamId() {
        return streamId;
    }

    public void setStreamId(String streamId) {
        this.streamId = streamId;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public static class ListEntryDeserializer implements JsonDeserializer<ListEntry> {

        @Override
        public ListEntry deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext ctx) throws JsonParseException {
            JsonObject jobject = json.getAsJsonObject();

            String id = jobject.get(ID).getAsString();
            String title = jobject.get(TITLE).getAsString();
            boolean unread = jobject.get(UNREAD).getAsBoolean();

            String originTitle = "";
            String streamId = null;
            if (jobject.has(ORIGIN)) {
                JsonObject originObject = jobject.getAsJsonObject(ORIGIN);
                if (originObject.has(TITLE)) {
                    originTitle = originObject.get(TITLE).getAsString();
                }
                if (originObject.has(STREAM_ID)) {
                    streamId = originObject.get(STREAM_ID).getAsString();
                }
            }
            String url = null;
            if (jobject.has(ALTERNATE)) {
                JsonArray canonicalArray = jobject.getAsJsonArray(ALTERNATE);
                if (canonicalArray.size() > 0) {
                    JsonObject canObject = canonicalArray.get(0).getAsJsonObject();
                    url = canObject.get(HREF).getAsString();
                }
            }
            List<Category> categories = new ArrayList<Category>();
            if (jobject.has(CATEGORIES)) {
                JsonArray jsonCategories = jobject.getAsJsonArray(CATEGORIES);
                Category[] categoriesArray = ctx.deserialize(jsonCategories, Category[].class);
                categories.addAll(Arrays.asList(categoriesArray));
            }
            List<Tag> tags = new ArrayList<Tag>();
            boolean saved = false;
            if (jobject.has(TAGS)) {
                JsonArray jsonTags = jobject.getAsJsonArray(TAGS);
                Tag[] tagsArray = ctx.deserialize(jsonTags, Tag[].class);
                for (int i = 0; i < tagsArray.length; i++) {
                    Tag t = tagsArray[i];
                    if (t.getId().endsWith(SAVED_SUFFIX)) {
                        saved = true;
                    }
                    tags.add(t);
                }
            }
            String engagement = null;
            boolean popular = false;
            if (jobject.has(ENGAGEMENT)) {
                engagement = jobject.get(ENGAGEMENT).getAsString();
                try {
                    Integer engInteger = Integer.valueOf(engagement);
                    if (engInteger > 18) {
                        popular = true;
                    }
                } catch (NumberFormatException e) {
                }
            }
            String author = null;
            if (jobject.has(AUTHOR)) {
                author = jobject.get(AUTHOR).getAsString();
            }
            Date published = null;
            if (jobject.has(PUBLISHED)) {
                published = DateUtils.getDateFromJson(jobject.get(PUBLISHED).getAsString());
            }
            String visual = null;
            if (jobject.has(VISUAL)) {
                String visualUrl = jobject.getAsJsonObject(VISUAL).get(URL).getAsString();
                // if no image, visual = "none"
                if (!"none".equalsIgnoreCase(visualUrl)) {
                    visual = visualUrl;
                }
            }
            String content = null;
            if (jobject.has(SUMMARY)) {
                JsonObject sumObject = jobject.getAsJsonObject(SUMMARY);
                content = sumObject.get(CONTENT).getAsString();
            }
            return new ListEntry(streamId, id, title, unread, originTitle, categories, tags, published, author, content, visual, engagement, popular, url, saved);
        }
    }

}
