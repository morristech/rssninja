package com.yairkukielka.rssninja.interactors;


import com.yairkukielka.rssninja.feedly.AuthRequest;
import com.yairkukielka.rssninja.feedly.AuthResponse;
import com.yairkukielka.rssninja.feedly.Entry;
import com.yairkukielka.rssninja.feedly.ItemType;
import com.yairkukielka.rssninja.feedly.MarkAction;
import com.yairkukielka.rssninja.feedly.StreamResponse;
import com.yairkukielka.rssninja.feedly.Subscription;
import com.yairkukielka.rssninja.feedly.UnreadList;

import java.util.List;
import java.util.Map;

import retrofit.Callback;
import rx.Observable;

/**
 * Feedly Api interface
 */
public interface FeedlyInteractor {

    void streamListEntries(String streamId, Map<String, String> params, Callback<StreamResponse> cb);

    void getEntry(String entryId, Callback<List<Entry>> cb);

    void authToken(AuthRequest body, Callback<AuthResponse> cb);

    void getSubscriptions(Callback<List<Subscription>> cb);

    void getUnreadSubscriptions(Callback<UnreadList> cb);

    Observable<Void> markAs(String streamId, ItemType itemType, MarkAction markAction);

    void setAccessToken(String accessToken);
}
