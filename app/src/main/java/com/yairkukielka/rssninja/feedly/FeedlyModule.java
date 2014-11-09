package com.yairkukielka.rssninja.feedly;

import android.content.SharedPreferences;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.yairkukielka.rssninja.FeedEntryFragment;
import com.yairkukielka.rssninja.app.App;
import com.yairkukielka.rssninja.common.Constants;
import com.yairkukielka.rssninja.interactors.FeedlyInteractor;
import com.yairkukielka.rssninja.interactors.FeedlyInteractorImpl;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import retrofit.RestAdapter;
import retrofit.converter.GsonConverter;

@Module(
        library = true,
        complete = false,
        injects = {
                FeedlyInteractorImpl.class,
                FeedEntryFragment.class
        }
)
public class FeedlyModule {

    @Provides
    @Singleton
    public Gson provideGson() {
        return new GsonBuilder()
                .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                .registerTypeAdapter(ListEntry.class, new ListEntry.ListEntryDeserializer())
                .registerTypeAdapter(Entry.class, new Entry.EntryDeserializer())
                .create();
    }

    @Provides
    @Singleton
    public FeedlyClient providesFeedlyClient(App application, Gson gson, SharedPreferences preferences) {
        RestAdapter restAdapter = new RestAdapter.Builder()
                .setEndpoint("https://cloud.feedly.com")
                .setLogLevel(RestAdapter.LogLevel.FULL)
                .setConverter(new GsonConverter(gson))
                .build();

        return restAdapter.create(FeedlyClient.class);
    }

    @Provides
    @Singleton
    public FeedlyInteractor providesFeedlyInteractor(FeedlyClient client, SharedPreferences preferences) {
        return new FeedlyInteractorImpl(client, preferences.getString(Constants.SHPREF_KEY_ACCESS_TOKEN, null));
    }

}
