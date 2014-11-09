package com.yairkukielka.rssninja.feedly;

import java.util.List;
import java.util.Map;

import retrofit.Callback;
import retrofit.http.Body;
import retrofit.http.GET;
import retrofit.http.Header;
import retrofit.http.POST;
import retrofit.http.Path;
import retrofit.http.QueryMap;
import rx.Observable;

/**
 * Feedly client rest adapter
 */
public interface FeedlyClient {

    @POST("/v3/auth/token")
    void authToken(@Body AuthRequest body, Callback<AuthResponse> cb);

    @GET("/v3/subscriptions")
    void getSubscriptions(@Header("Authorization") String auth, Callback<List<Subscription>> cb);

    @GET("/v3/markers/counts")
    void getUnreadSubscriptions(@Header("Authorization") String auth, Callback<UnreadList> cb);

    @GET("/v3/streams/{streamId}/contents")
    void streamListEntries(@Header("Authorization") String auth, @Path("streamId") String streamId, @QueryMap Map<String, String> params, Callback<StreamResponse> cb);

    @GET("/v3/entries/{entryId}")
    void getEntry(@Header("Authorization") String auth, @Path("entryId") String entryId, Callback<List<Entry>> cb);

    @POST("/v3/markers")
    Observable<Void> markAs(@Header("Authorization") String auth, @Body MarkAs body);


}
