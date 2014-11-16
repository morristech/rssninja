/**
 * Copyright 2013 Ognyan Bankov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.yairkukielka.rssninja.main;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

import com.nhaarman.listviewanimations.appearance.simple.SwingBottomInAnimationAdapter;
import com.yairkukielka.rssninja.data.AppData;
import com.yairkukielka.rssninja.FeedEntryActivity;
import com.yairkukielka.rssninja.R;
import com.yairkukielka.rssninja.app.App;
import com.yairkukielka.rssninja.app.Tools;
import com.yairkukielka.rssninja.common.BaseFragment;
import com.yairkukielka.rssninja.common.Constants;
import com.yairkukielka.rssninja.feedly.ItemType;
import com.yairkukielka.rssninja.feedly.ListEntry;
import com.yairkukielka.rssninja.feedly.MarkAction;
import com.yairkukielka.rssninja.feedly.StreamResponse;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import dagger.Lazy;
import retrofit.Callback;
import retrofit.RetrofitError;


/**
 * Shows the list of entries for a stream
 */
public class EntryListFragment extends BaseFragment {
    private static final String TAG = EntryListFragment.class.getSimpleName();
    public String continuation = null;
    @Inject
    App application;
    @Inject
    SharedPreferences preferences;
    @Inject
    Lazy<MainPresenter> presenter;
    @InjectView(R.id.lv_picasa)
    AbsListView entriesListView;
    @Inject
    MainView mainView;
    private ArrayList<ListEntry> mEntries = new ArrayList<ListEntry>();
    private ListViewEntryArrayAdapter mAdapter;
    private boolean initialized = false;
    private StreamId streamId;
    private String accessToken;

    public void onAttach(android.app.Activity activity) {
        super.onAttach(activity);
        Log.e(TAG, "attached");
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view = inflater.inflate(R.layout.feed_grid_view, container, false);
        ButterKnife.inject(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initialized = true;
        accessToken = preferences.getString(Constants.SHPREF_KEY_ACCESS_TOKEN, null);
    }

    @Override
    public void onActivityCreated(Bundle bundle) {
        super.onActivityCreated(bundle);
        StreamId streamToLoad = presenter.get().getStreamToLoad();
        setStreamId(streamToLoad);
    }


    public void setStreamId(final StreamId streamId) {
        this.streamId = streamId;
        if (initialized) {
            refreshItems();
        }
    }

    private void refreshItems() {
        if (streamId == null || accessToken == null) {
            Log.e(TAG, "No stream to load in EntryListFragment.refreshItems or activity is null");
            return;
        }
        mEntries.clear();
        mAdapter = new ListViewEntryArrayAdapter(getActivity(), 0, mEntries, preferences, presenter);
        SwingBottomInAnimationAdapter animationAdapter = new SwingBottomInAnimationAdapter(mAdapter);
        animationAdapter.setAbsListView(entriesListView);
        ((AdapterView) entriesListView).setAdapter(animationAdapter);
        entriesListView.setOnScrollListener(new EndlessScrollListener());
        showProgress();
        entriesListView.setOnItemClickListener(getOnItemClickListener());

        loadPage();
    }

    /**
     * Returns the item listener
     *
     * @return the item listener
     */
    private OnItemClickListener getOnItemClickListener() {
        return new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ListEntry listEntry = mEntries.get(position);
                if (mAdapter != null) {
                    // mark as read
                    presenter.get().markAs(listEntry.getStreamId(), ItemType.ENTRY, MarkAction.READ)
                            .subscribe(mAdapter.getMarkAsSubscriber(listEntry, MarkAction.READ, null, null));
                }
                Bundle b = new Bundle();
                b.putString("accessToken", accessToken);
                b.putString("entryId", listEntry.getId());
                b.putString("stream_id", streamId.getStreamId());
                b.putInt("entryPosition", position);
                Intent intent = new Intent(EntryListFragment.this.getActivity(), FeedEntryActivity.class);
                intent.putExtras(b);
                startActivity(intent);
                EntryListFragment.this.getActivity().overridePendingTransition(R.anim.open_next, R.anim.close_main);
            }
        };
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    /**
     * Loads a page
     */
    private void loadPage() {
        Map<String, String> params = new LinkedHashMap<String, String>();
        if (continuation != null) {
            params.put("continuation", continuation);
        }
        if (streamId.getMix()) {
            params.put("count", "20");
        } else {
            params.put("count", "30");
        }
        if (Tools.getPrefOnlyUnread(getActivity())) {
            params.put("unreadOnly", "true");
        }
        Callback<StreamResponse> callback = getListEntryCallback();
        presenter.get().streamListEntries(streamId.getStreamId(), params, callback);
    }

    private Callback<StreamResponse> getListEntryCallback() {
        return new Callback<StreamResponse>() {
            @Override
            public void success(StreamResponse resp, retrofit.client.Response response) {
                hideProgress();
                continuation = resp.getContinuation();
                mEntries.addAll(resp.getItems());
                mAdapter.notifyDataSetChanged();
                setLastStreamLoaded();
            }

            @Override
            public void failure(RetrofitError retrofitError) {
                hideProgress();
                Log.e(TAG, retrofitError.getMessage());
            }
        };
    }

    private void setLastStreamLoaded() {
        AppData appData = application.getAppData();
        appData.setLastStreamLoaded(mEntries);
    }

    private void showProgress() {
        Activity activity = getActivity();
        if (activity != null) {
            if (activity instanceof MainView) {
                MainView mainView = (MainView) activity;
                mainView.showProgress();
            }
        }
    }

    private void hideProgress() {
        Activity activity = getActivity();
        if (activity != null) {
            if (activity instanceof MainView) {
                MainView mainView = (MainView) activity;
                mainView.hideProgress();
            }
        }
    }

    /**
     * Detects when user is close to the end of the current page and starts
     * loading the next page so the user will not have to wait (that much) for
     * the next entries.
     */
    public class EndlessScrollListener implements OnScrollListener {
        // how many entries earlier to start loading next page
        private int visibleThreshold = 5;
        private int currentPage = 0;
        private int previousTotal = 0;
        private boolean loading = true;

        public EndlessScrollListener() {
        }

        @Override
        public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
            if (loading) {
                if (totalItemCount > previousTotal) {
                    loading = false;
                    hideProgress();

                    previousTotal = totalItemCount;
                    currentPage++;
                }
            }
            // continuation is null when Feedly API says there are no more entries to retrieve
            if (!loading && continuation != null
                    && (totalItemCount - visibleItemCount) <= (firstVisibleItem + visibleThreshold)) {
                loadPage();
                loading = true;
                showProgress();
            }
        }

        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {

        }

        public int getCurrentPage() {
            return currentPage;
        }
    }
}
