package com.yairkukielka.rssninja.app;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.yairkukielka.rssninja.FeedEntryActivity;
import com.yairkukielka.rssninja.main.ListViewEntryArrayAdapter;
import com.yairkukielka.rssninja.feedly.FeedlyModule;
import com.yairkukielka.rssninja.interactors.InteractorsModule;
import com.yairkukielka.rssninja.login.LoginActivity;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module(
//        library = true,
        injects = {
                App.class,
                FeedEntryActivity.class,
                ListViewEntryArrayAdapter.class,
                LoginActivity.class
        }
        ,
        includes = {
                FeedlyModule.class,
                InteractorsModule.class
        }

)
public class AppModule {

    private App app;

    public AppModule(App app) {
        this.app = app;
    }

    @Provides
    @Singleton
    public App providesApp() {
        return app;
    }

    @Provides
    @Singleton
    public SharedPreferences provideSharedPreferences() {
        return PreferenceManager.getDefaultSharedPreferences(app);
    }

//    @Provides
//    @Singleton
//    public LayoutInflater provideLayoutInflater() {
//        return (LayoutInflater) app.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//    }
//    @Provides @Singleton
//    Resources provideResources() {
//        return app.getResources();
//    }

}
