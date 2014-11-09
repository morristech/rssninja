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

package com.yairkukielka.rssninja.interactors;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.yairkukielka.rssninja.app.App;


public class NetworkInteractorImpl implements NetworkInteractor {

    private static final String TAG = NetworkInteractorImpl.class.getSimpleName();
    App application;

    public NetworkInteractorImpl(App app) {
        this.application = app;
    }


    @Override
    public boolean isInternetAvailable() {
        NetworkInfo info = ((ConnectivityManager) application.getSystemService(Context.CONNECTIVITY_SERVICE))
                .getActiveNetworkInfo();

        if (info == null) {
            Log.d(TAG, "no internet connection");
            return false;
        } else {
            if (info.isConnected()) {
                return true;
            } else {
                Log.d(TAG, " internet connection ??");
                return true;
            }
        }
    }
}
