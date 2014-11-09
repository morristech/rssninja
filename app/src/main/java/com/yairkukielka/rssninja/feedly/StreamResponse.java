package com.yairkukielka.rssninja.feedly;

import java.util.List;

public class StreamResponse {

    private String id;
    private String updated;
    private String continuation;
    private List<ListEntry> items;

    public StreamResponse() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUpdated() {
        return updated;
    }

    public void setUpdated(String updated) {
        this.updated = updated;
    }

    public String getContinuation() {
        return continuation;
    }

    public void setContinuation(String continuation) {
        this.continuation = continuation;
    }

    public List<ListEntry> getItems() {
        return items;
    }

    public void setItems(List<ListEntry> items) {
        this.items = items;
    }
}
