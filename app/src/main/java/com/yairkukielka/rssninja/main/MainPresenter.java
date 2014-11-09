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

import com.yairkukielka.rssninja.feedly.ItemType;
import com.yairkukielka.rssninja.feedly.ListEntry;
import com.yairkukielka.rssninja.feedly.MarkAction;
import com.yairkukielka.rssninja.feedly.StreamResponse;

import java.util.Map;

import de.keyboardsurfer.android.widget.crouton.Style;
import retrofit.Callback;
import rx.Observable;

public interface MainPresenter {

    void startConnection();

    Observable<Void> markAs(String streamId, ItemType itemType, MarkAction action);

    void streamListEntries(String streamId, Map<String, String> params, Callback<StreamResponse> cb);

    StreamId getStreamToLoad();

    void showMessage(String s, Style style);

    /**
     * Notify the subscriptions in the drawer of the change
     *
     * @param streamId entry id
     * @param action   action (read or unread)
     */
    void notifySubscriptionsChange(String streamId, MarkAction action);

    void markEntry(ListEntry entry, MarkAction action);
}
