package com.yairkukielka.rssninja.feedly;

import com.google.gson.annotations.SerializedName;

import java.util.Date;

public class MarkAs {

    private String action;
    private String type;
    @SerializedName(("entryIds"))
    private String[] entryIds;
    @SerializedName("feedIds")
    private String[] feedIds;
    @SerializedName("categoryIds")
    private String[] categoryIds;
    private long asOf;

    public MarkAs() {
    }

    public MarkAs(String itemId, ItemType itemType, MarkAction markAction) {
        action = markAsActionToString(markAction);
        switch (itemType) {
            case CATEGORY:
                type = "categories";
                categoryIds = new String[]{itemId};
                asOf = new Date().getTime();
                break;
            case ENTRY:
                type = "entries";
                entryIds = new String[]{itemId};
                break;
            case FEED:
                type = "feeds";
                feedIds = new String[]{itemId};
                asOf = new Date().getTime();
                break;
        }
    }

    private String markAsActionToString(MarkAction markAction) {
        switch (markAction) {
            case READ:
                return "markAsRead";
            case UNREAD:
                return "keepUnread";
            case SAVE:
                return "markAsSaved";
            case UNSAVE:
                return "markAsUnsaved";
            default:
                return "markAsRead";
        }
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String[] getFeedIds() {
        return feedIds;
    }

    public void setFeedIds(String[] feedIds) {
        this.feedIds = feedIds;
    }

    public String[] getCategoryIds() {
        return categoryIds;
    }

    public void setCategoryIds(String[] categoryIds) {
        this.categoryIds = categoryIds;
    }

    public long getAsOf() {
        return asOf;
    }

    public void setAsOf(long asOf) {
        this.asOf = asOf;
    }

    public String[] getEntryIds() {
        return entryIds;
    }

    public void setEntryIds(String[] entryIds) {
        this.entryIds = entryIds;
    }
}
