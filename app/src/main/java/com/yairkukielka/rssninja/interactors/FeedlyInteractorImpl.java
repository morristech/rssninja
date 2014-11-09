package com.yairkukielka.rssninja.interactors;


import com.yairkukielka.rssninja.feedly.AuthRequest;
import com.yairkukielka.rssninja.feedly.AuthResponse;
import com.yairkukielka.rssninja.feedly.Entry;
import com.yairkukielka.rssninja.feedly.FeedlyClient;
import com.yairkukielka.rssninja.feedly.ItemType;
import com.yairkukielka.rssninja.feedly.MarkAction;
import com.yairkukielka.rssninja.feedly.MarkAs;
import com.yairkukielka.rssninja.feedly.StreamResponse;
import com.yairkukielka.rssninja.feedly.Subscription;
import com.yairkukielka.rssninja.feedly.UnreadList;

import java.util.List;
import java.util.Map;

import retrofit.Callback;
import rx.Observable;

public class FeedlyInteractorImpl implements FeedlyInteractor {

    private FeedlyClient client;
    private String accessToken;

    public FeedlyInteractorImpl(FeedlyClient client, String accessToken) {
        this.client = client;
        this.accessToken = accessToken;
    }

    public void authToken(AuthRequest body, Callback<AuthResponse> cb) {
        client.authToken(body, cb);
    }

    public void getSubscriptions(Callback<List<Subscription>> cb) {
        client.getSubscriptions(getAuthorizationHeader(), cb);
    }

    public void getUnreadSubscriptions(Callback<UnreadList> cb) {
        client.getUnreadSubscriptions(getAuthorizationHeader(), cb);
    }

    public void streamListEntries(String streamId, Map<String, String> params, Callback<StreamResponse> cb) {
        client.streamListEntries(getAuthorizationHeader(), streamId, params, cb);
    }

    public Observable<Void> markAs(String streamId, ItemType itemType, MarkAction markAction) {
        return client.markAs(getAuthorizationHeader(), new MarkAs(streamId, itemType, markAction));
    }


    public void getEntry(String entryId, Callback<List<Entry>> cb) {
        client.getEntry(getAuthorizationHeader(), entryId, cb);
    }


    private String getAuthorizationHeader() {
        if (accessToken != null) {
            return "OAuth " + accessToken;
        }
        throw new RuntimeException("No access token when needed to query Feedly");
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }
}
