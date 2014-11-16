/*
 *
 *  *
 *  *  * Copyright (C) 2014 Antonio Leiva Gordillo.
 *  *  *
 *  *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  *  * you may not use this file except in compliance with the License.
 *  *  * You may obtain a copy of the License at
 *  *  *
 *  *  *      http://www.apache.org/licenses/LICENSE-2.0
 *  *  *
 *  *  * Unless required by applicable law or agreed to in writing, software
 *  *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  *  * See the License for the specific language governing permissions and
 *  *  * limitations under the License.
 *  *
 *
 */

package com.yairkukielka.rssninja.main;


import android.content.SharedPreferences;
import android.util.Log;

import com.yairkukielka.rssninja.data.AppData;
import com.yairkukielka.rssninja.R;
import com.yairkukielka.rssninja.app.App;
import com.yairkukielka.rssninja.common.Constants;
import com.yairkukielka.rssninja.feedly.Category;
import com.yairkukielka.rssninja.feedly.FeedlyConstants;
import com.yairkukielka.rssninja.feedly.ItemType;
import com.yairkukielka.rssninja.feedly.ListEntry;
import com.yairkukielka.rssninja.feedly.MarkAction;
import com.yairkukielka.rssninja.feedly.StreamResponse;
import com.yairkukielka.rssninja.feedly.Subscription;
import com.yairkukielka.rssninja.feedly.UnreadList;
import com.yairkukielka.rssninja.interactors.FeedlyInteractor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import de.keyboardsurfer.android.widget.crouton.Style;
import retrofit.Callback;
import retrofit.RetrofitError;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class MainPresenterImpl implements MainPresenter, OnFinishedListener {

    private static final String TAG = MainPresenterImpl.class.getSimpleName();
    private MainView mainView;
    private FeedlyInteractor feedlyInteractor;
    private SharedPreferences preferences;
    private App application;

    private List<Subscription> subscriptions;
    private List<UnreadList.Unread> unreads;
    private StreamId streamToLoad;

    public MainPresenterImpl(MainView mainView, FeedlyInteractor feedlyInteractor,
                             SharedPreferences preferences, App application) {
        this.mainView = mainView;
        this.feedlyInteractor = feedlyInteractor;
        this.preferences = preferences;
        this.application = application;
    }

    @Override
    public void onFinished(List<String> items) {
        mainView.hideProgress();
    }


    public void showMessage(String s, Style style) {
        mainView.showMessage(s, style);
    }


    public void markEntry(ListEntry entry, MarkAction action) {
        switch (action) {
            case SAVE:
                entry.setSaved(true);
                break;
            case UNSAVE:
                entry.setSaved(true);
                break;
            case READ:
                entry.setUnread(false);
                break;
            case UNREAD:
                entry.setUnread(true);
                break;
            default:
                break;
        }
    }

    public void notifySubscriptionsChange(String streamId, MarkAction action) {
        Collection<Subscription> subscriptions = application.getAppData().findAllSubscriptions();
        for (Subscription s : subscriptions) {
            if (s.getId().equals(streamId)) {
                if (action.equals(MarkAction.READ)) {
                    s.setUnread(s.getUnread() - 1);
                } else {
                    s.setUnread(s.getUnread() + 1);
                }
                break;
            }
        }
        mainView.notifyDataSetChanged();
    }

    public Observable<Void> markAs(String streamId, ItemType itemType, MarkAction action) {
        return feedlyInteractor.markAs(streamId, itemType, action)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }


    /**
     * Connects to the internet to access the subscriptions and global feeds
     */
    public void startConnection() {
        String accessToken = preferences.getString(Constants.SHPREF_KEY_ACCESS_TOKEN, null);
        if (accessToken != null) {
            application.getAppData().clearData();
            getSubscriptions();
        } else {
            mainView.startLoginActivity();
        }
    }

    /**
     * Get the subscriptions
     */
    private void getSubscriptions() {
        subscriptions = new ArrayList<>();
        unreads = new ArrayList<>();
        streamToLoad = getStreamToLoad();
        // replace fragment in mainView
        mainView.replaceContentWithEntryListFragment();

        // the next two callbacks call showUnreadSubscriptions
        feedlyInteractor.getSubscriptions(getSubscriptionsCallback());
        feedlyInteractor.getUnreadSubscriptions(getUnreadSubscriptionsCallback());
    }

    /**
     * Gets the userId from preferences
     * @return the userId
     */
    private String getUserId() {
        return preferences.getString(Constants.SHPREF_KEY_USERID_TOKEN, null);
    }

    /**
     * Gets the stream to load
     *
     * @return the stream to load
     */
    public StreamId getStreamToLoad() {
        AppData appData = application.getAppData();
        // check if there was a last-read-stream
        if (appData != null && appData.getLastReadStreamId() != null) {
            streamToLoad = appData.getLastReadStreamId();
        }
        if (streamToLoad == null) {
            // show in the main fragment the global.all entries or the last streamId loaded
            String userId = getUserId();
            streamToLoad = new StreamId(FeedlyConstants.USERS_PREFIX_PATH + userId + FeedlyConstants.GLOBAL_ALL_SUFFIX, false);
        }
        return streamToLoad;
    }

    /**
     * Get the callback for getSubscriptions
     */
    private Callback<List<Subscription>> getSubscriptionsCallback() {
        return new Callback<List<Subscription>>() {
            @Override
            public void success(List<Subscription> subscriptionsResponse, retrofit.client.Response response) {
                subscriptions.addAll(subscriptionsResponse);
                prepareSubscriptionsData(subscriptions);
                showUnreadSubscriptions();
            }

            @Override
            public void failure(RetrofitError retrofitError) {
                mainView.collapseRefreshMenuItem();
                String errorMessage = application.getResources().getString(R.string.receiving_subscriptions_exception)
                        + retrofitError.getMessage();
                Log.e(TAG, errorMessage);
                if (retrofitError.isNetworkError()) {
                    mainView.showMessage(application.getString(R.string.no_internet_connection), Style.ALERT);
                } else {
                    // usually here we must refresh the access token because it has expired
                    mainView.startLoginActivity();
                }
            }
        };
    }

    private void showUnreadSubscriptions() {
        // if getSubscriptions and getUnreadSubscriptions have returned
        if (!subscriptions.isEmpty() && !unreads.isEmpty()) {
            String popularItemsTitle = application.getResources().getString(R.string.drawer_popular);
            for (UnreadList.Unread unread : unreads) {
                Subscription s = application.getAppData().findSubscriptionById(unread.getId());
                if (s != null && !s.getTitle().contains(popularItemsTitle)) {
                    s.setUnread(unread.getCount());
                }
            }
            mainView.paintDrawerSubscriptions();
        }
    }

    /**
     * Receives the unread counts and sets them in the subscription objects
     *
     * @return listener
     */
    private Callback<UnreadList> getUnreadSubscriptionsCallback() {
        return new Callback<UnreadList>() {
            @Override
            public void success(UnreadList unreadList, retrofit.client.Response response) {
                unreads = unreadList.getUnreadcounts();
                showUnreadSubscriptions();
            }

            @Override
            public void failure(RetrofitError retrofitError) {
                String errorMessage = application.getResources().getString(R.string.receiving_unread_subscriptions_exception)
                        + retrofitError.getMessage();
                Log.e(TAG, errorMessage);
            }
        };
    }


    /**
     * Initialize data for the adapter of the ExpandableListAdapter
     */
    private void prepareSubscriptionsData(List<Subscription> subs) {
        String userId = getUserId();
        // add global.all subscription to feedly categories
        Subscription allSubscription = new Subscription();
        allSubscription.setId(FeedlyConstants.USERS_PREFIX_PATH + userId + FeedlyConstants.GLOBAL_ALL_SUFFIX);
        String allLabel = application.getResources().getString(R.string.drawer_all);
        allSubscription.setTitle(allLabel);

        // add must read subscription to feedly categories
        Subscription mustReadSubscription = new Subscription();
        // here, global.all is used with the /v3/mixes/contents endpoint
        mustReadSubscription.setId(FeedlyConstants.USERS_PREFIX_PATH + userId + FeedlyConstants.GLOBAL_ALL_SUFFIX);
        String mustReadTitle = application.getResources().getString(R.string.drawer_popular);
        mustReadSubscription.setTitle(mustReadTitle);

        // add saved subscription to feedly categories
        Subscription savedSubscription = new Subscription();
        savedSubscription.setId(FeedlyConstants.USERS_PREFIX_PATH + userId + FeedlyConstants.TAG_URL_PART + FeedlyConstants.GLOBAL_SAVED);
        String savedLabel = application.getResources().getString(R.string.drawer_saved);
        savedSubscription.setTitle(savedLabel);

        // add global.uncategorized subscription to feedly categories
        Subscription uncategorizedSubscription = new Subscription();
        uncategorizedSubscription.setId(FeedlyConstants.USERS_PREFIX_PATH + userId + FeedlyConstants.GLOBAL_UNCATEGORIZED_SUFFIX);
        String uncategorizedLabel = application.getResources().getString(R.string.drawer_uncategorized);
        uncategorizedSubscription.setTitle(uncategorizedLabel);

        // add them to the feedlyCategory subscriptions list
        List<Subscription> feedlyCategoriesSubscriptions = new ArrayList<Subscription>();
        feedlyCategoriesSubscriptions.add(mustReadSubscription);
        feedlyCategoriesSubscriptions.add(savedSubscription);
        feedlyCategoriesSubscriptions.add(allSubscription);
        feedlyCategoriesSubscriptions.add(uncategorizedSubscription);
        // feedly categories
        Category feedlyCategory = new Category();
        feedlyCategory.setId(FeedlyConstants.FEEDLY_CATEGORIES);
        String feedlyLabel = FeedlyConstants.FEEDLY_CATEGORIES;
        feedlyCategory.setLabel(feedlyLabel);
        AppData appData = application.getAppData();
        appData.addSubscription(feedlyCategory, mustReadSubscription);
        appData.addSubscription(feedlyCategory, savedSubscription);
        appData.addSubscription(feedlyCategory, allSubscription);
        appData.addSubscription(feedlyCategory, uncategorizedSubscription);

        for (Subscription s : subs) {
            for (Category cat : s.getCategories()) {
                appData.addSubscription(cat, s);
            }
        }
    }

    public void streamListEntries(String streamId, Map<String, String> params, Callback<StreamResponse> cb) {
        feedlyInteractor.streamListEntries(streamId, params, cb);
    }

}
