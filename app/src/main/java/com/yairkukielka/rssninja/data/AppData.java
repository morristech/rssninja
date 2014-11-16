package com.yairkukielka.rssninja.data;

import com.yairkukielka.rssninja.feedly.Category;
import com.yairkukielka.rssninja.feedly.ListEntry;
import com.yairkukielka.rssninja.feedly.Subscription;
import com.yairkukielka.rssninja.main.StreamId;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Activity data to retain in case of a change of configuration, e.g.
 * orientation of the device
 */
public final class AppData {
    private List<Category> categories = new ArrayList<Category>();
    private HashMap<String, List<Subscription>> categorySubscriptionsMap = new LinkedHashMap<String, List<Subscription>>();
    private HashMap<String, Subscription> subscriptionsMap = new HashMap<String, Subscription>();
    private ArrayList<ListEntry> lastStreamLoaded;
    private StreamId lastReadStreamId;

    public List<Subscription> findCategorySubscriptionsByCategoryLabel(String label) {
        return categorySubscriptionsMap.get(label);
    }

    public void addSubscription(Category cat, Subscription subscription) {
        List<Subscription> categorySubscriptions = findCategorySubscriptionsByCategoryLabel(cat.getLabel());
        if (categorySubscriptions == null) {
            categories.add(cat);
            categorySubscriptions = new ArrayList<Subscription>();
        }
        categorySubscriptions.add(subscription);

        categorySubscriptionsMap.put(cat.getLabel(), categorySubscriptions);
        subscriptionsMap.put(subscription.getId(), subscription);
    }

    public Collection<Subscription> findAllSubscriptions() {
        return subscriptionsMap.values();
    }

    public Subscription findSubscriptionById(String id) {
        return subscriptionsMap.get(id);
    }

    public Subscription findSubscriptionByCategoryAndSubPosition(int catPosition, int subscriptionPosition) {
        return categorySubscriptionsMap.get(getCategories().get(catPosition).getLabel()).get(subscriptionPosition);
    }

    public HashMap<String, Subscription> getSubscriptionsMap() {
        return subscriptionsMap;
    }

    public void setSubscriptionsMap(HashMap<String, Subscription> subscriptionsMap) {
        this.subscriptionsMap = subscriptionsMap;
    }

    public List<Category> getCategories() {
        return categories;
    }

    public void setCategories(List<Category> categories) {
        this.categories = categories;
    }

    public HashMap<String, List<Subscription>> getCategorySubscriptionsMap() {
        return categorySubscriptionsMap;
    }

    public void setCategorySubscriptionsMap(HashMap<String, List<Subscription>> categorySubscriptionsMap) {
        this.categorySubscriptionsMap = categorySubscriptionsMap;
    }

    public StreamId getLastReadStreamId() {
        return lastReadStreamId;
    }

    public void setLastReadStreamId(StreamId streamId) {
        this.lastReadStreamId = streamId;
    }

    public ArrayList<ListEntry> getLastStreamLoaded() {
        return lastStreamLoaded;
    }

    public void setLastStreamLoaded(ArrayList<ListEntry> lastStreamLoaded) {
        this.lastStreamLoaded = lastStreamLoaded;
    }

    /**
     * Clear existing data
     */
    public void clearData() {
        categories.clear();
        categorySubscriptionsMap.clear();
        subscriptionsMap.clear();
    }


    ;
}