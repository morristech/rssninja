
package com.yairkukielka.rssninja.interactors;


import com.yairkukielka.rssninja.app.App;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module(
        library = true,
        complete = false
)
public class InteractorsModule {

    @Provides
    @Singleton
    public NetworkInteractor providesNetworkInteractor(App application) {
        return new NetworkInteractorImpl(application);
    }

}
