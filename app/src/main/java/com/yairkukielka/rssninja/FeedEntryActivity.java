package com.yairkukielka.rssninja;

import android.app.ActionBar;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.android.volley.Request.Method;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.anprosit.android.dagger.ui.DaggerFragmentActivity;
import com.yairkukielka.rssninja.app.App;
import com.yairkukielka.rssninja.app.MyVolley;
import com.yairkukielka.rssninja.data.AppData;
import com.yairkukielka.rssninja.common.Constants;
import com.yairkukielka.rssninja.feedly.FeedlyConstants;
import com.yairkukielka.rssninja.feedly.ListEntry;
import com.yairkukielka.rssninja.interactors.FeedlyInteractor;
import com.yairkukielka.rssninja.network.JsonCustomRequest;
import com.yairkukielka.rssninja.toolbox.NetworkUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.ButterKnife;

public class FeedEntryActivity extends DaggerFragmentActivity implements FeedEntryFragment.FeedEntryListener {

    private static final String TAG = FeedEntryActivity.class.getSimpleName();
    private static final String MARK_READ = "markAsRead";
    private static final String MARK_KEEP_UNREAD = "keepUnread";
    private static final String MARKERS_PATH = "/v3/markers";
    private static final String ENTRIES_IDS = "entryIds";
    private static final String TEXT_PLAIN = "text/plain";
    @Inject
    App app;
    @Inject
    FeedlyInteractor feedlyInteractor;
    String accessToken;
    String streamId;
    Integer entryPositionInStream;
    /**
     * list entry obtained from MyPageChangeListener.
     */
    private ListEntry entry;
    private Menu actionBarmenu;
    private ViewPager mPager;
    private PagerAdapter mPagerAdapter;

    @Override
    protected List<Object> getModules() {
        return new ArrayList<Object>();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.feed_entry_container_layout);
        ButterKnife.inject(this);

        Intent intent = getIntent();
        accessToken = intent.getStringExtra("accessToken");
        streamId = intent.getStringExtra("streamId");
        entryPositionInStream = intent.getIntExtra("entryPosition", 0);

        // Instantiate a ViewPager and a PagerAdapter.
        mPager = (ViewPager) findViewById(R.id.pager);
        mPagerAdapter = new EntryPageAdapter(getSupportFragmentManager(), app.getAppData());
        mPager.setAdapter(mPagerAdapter);
        mPager.setOnPageChangeListener(new EntryPageChangeListener(app.getAppData()));
        mPager.setCurrentItem(entryPositionInStream);
        // action bar icon navagable up
        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            getActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    /**
     * Back button pressed. Go back to the article list.
     */
    @Override
    public void onBackPressed() {
        finish();
        overridePendingTransition(R.anim.open_main, R.anim.close_next);
        super.onBackPressed();
    }

    @Override
    public void setTitle(CharSequence title) {
        getActionBar().setTitle(title);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.entry, menu);
        actionBarmenu = menu;
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                overridePendingTransition(R.anim.open_main, R.anim.close_next);
                return true;
            case R.id.action_share:
                shareEntry();
                return true;
            case R.id.action_mark_saved:
                saveOrUnsaveEntry();
                return true;
            case R.id.action_mark_unread:
                markEntry(MARK_KEEP_UNREAD, getSuccessListener(getResources().getString(R.string.kept_as_unread)));
                return true;
            case R.id.action_open_in_browser:
                openEntryInBrowser();
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Share entry
     */
    private void shareEntry() {
        if (entry != null) {
            // only if entry has loaded
            Intent sharingIntent = new Intent(Intent.ACTION_SEND);
            sharingIntent.setType(TEXT_PLAIN);
            String shareBody = entry.getUrl();
            sharingIntent.putExtra(Intent.EXTRA_SUBJECT, entry.getTitle());
            sharingIntent.putExtra(Intent.EXTRA_TEXT, shareBody);
            startActivity(Intent.createChooser(sharingIntent, getResources().getString(R.string.share_text)));
        }
    }

    /**
     * Save or unsave an entry
     */
    private void saveOrUnsaveEntry() {
        if (entry != null) {
            // only if entry has loaded
            int method = Method.PUT;
            String successMessage;
            if (entry.isSaved()) {
                method = Method.DELETE;
                successMessage = getResources().getString(R.string.unsaved_article);
                entry.setSaved(false);
            } else {
                successMessage = getResources().getString(R.string.saved_article);
                entry.setSaved(true);
            }
            supportInvalidateOptionsMenu();

            SharedPreferences sprPreferences = PreferenceManager.getDefaultSharedPreferences(this);
            String accessToken = sprPreferences.getString(Constants.SHPREF_KEY_ACCESS_TOKEN, null);
            String userId = sprPreferences.getString(Constants.SHPREF_KEY_USERID_TOKEN, null);

            // send request
            RequestQueue queue = MyVolley.getRequestQueue();
            try {
                JSONObject jsonRequest = new JSONObject();
                if (accessToken != null && userId != null) {
                    StringBuilder url = new StringBuilder();
                    String userIdEncoded = URLEncoder.encode("/" + userId.toString() + "/tag/", FeedlyConstants.UTF_8);
                    url.append(FeedlyConstants.ROOT_URL).append(FeedlyConstants.TAGS_PATH).append("/user")
                            .append(userIdEncoded).append(FeedlyConstants.GLOBAL_SAVED);
                    if (method == Method.PUT) {
                        JSONArray entries = new JSONArray();
                        jsonRequest.put(ENTRIES_IDS, entries);
                        entries.put(entry.getId());
                        jsonRequest.put(ENTRIES_IDS, entries);
                        JsonCustomRequest myReq = NetworkUtils.getJsonCustomRequest(method, url.toString(),
                                jsonRequest, getSuccessListener(successMessage),
                                createMyReqErrorListener(ERROR_LISTNENER_ORIGIN.SAVING), accessToken);
                        queue.add(myReq);
                    } else {
                        String entryIdEncoded = URLEncoder.encode(entry.getId(), FeedlyConstants.UTF_8);
                        url.append("/" + entryIdEncoded);
                        JsonCustomRequest myReq = NetworkUtils.getJsonCustomRequest(method, url.toString(),
                                jsonRequest, getSuccessListener(successMessage),
                                createMyReqErrorListener(ERROR_LISTNENER_ORIGIN.UNSAVING), accessToken);
                        queue.add(myReq);
                    }
                }
            } catch (JSONException uex) {
                Log.e(TAG, "JSONException marking as read/unread entry");
            } catch (UnsupportedEncodingException uex) {
                Log.e(TAG, "Error encoding URL when saving/unsaving entry");
            }
        }
    }

    /**
     * Mark entry as read or unread
     *
     * @param markOrUnmark    read or unread
     * @param successListener successListener
     */
    private void markEntry(String markOrUnmark, Listener<JSONObject> successListener) {
        if (entry != null) {
            // only if entry has loaded
            RequestQueue queue = MyVolley.getRequestQueue();
            try {
                if (markOrUnmark.equals(MARK_READ)) {
                    entry.setUnread(false);
                } else {
                    entry.setUnread(true);
                }
                JSONObject jsonRequest = new JSONObject();
                jsonRequest.put("action", markOrUnmark);
                jsonRequest.put("type", "entries");
                JSONArray entries = new JSONArray();
                entries.put(entry.getId());
                jsonRequest.put("entryIds", entries);
                JsonCustomRequest myReq = NetworkUtils.getJsonCustomRequest(Method.POST, FeedlyConstants.ROOT_URL
                                + MARKERS_PATH, jsonRequest, successListener,
                        createMyReqErrorListener(ERROR_LISTNENER_ORIGIN.MARKING), accessToken);
                queue.add(myReq);
            } catch (JSONException uex) {
                Log.e(TAG, "Error marking read or unread");
            }
        }
    }

    /* Called whenever we call invalidateOptionsMenu() */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (entry != null) {
            if (entry.isSaved()) {
                paintStar();
            } else {
                unpaintStar();
            }
            setTitle(entry.getOriginTitle());
        }
        return super.onPrepareOptionsMenu(menu);
    }

    private void paintStar() {
        MenuItem mItem = actionBarmenu.findItem(R.id.action_mark_saved);
        mItem.setIcon(getResources().getDrawable(R.drawable.star_big_on));
        mItem.setTitle(getResources().getString(R.string.mark_unsaved));
    }

    private void unpaintStar() {
        MenuItem mItem = actionBarmenu.findItem(R.id.action_mark_saved);
        mItem.setIcon(getResources().getDrawable(R.drawable.star_big_off));
        mItem.setTitle(getResources().getString(R.string.mark_saved));
    }

    /**
     * Open browser after clicking the title
     */
    @Override
    public void openEntryInBrowser() {
        if (entry != null && entry.getUrl() != null) {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(entry.getUrl()));
            startActivity(browserIntent);
        }
    }

    @Override
    public void onFinishedLoading() {
        markEntry(MARK_READ, getSuccessListener(null));
    }

    private Response.Listener<JSONObject> getSuccessListener(final String message) {
        return new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                if (message != null) {
                    Toast.makeText(FeedEntryActivity.this, message, Toast.LENGTH_SHORT).show();
                }
            }
        };
    }

    private Response.ErrorListener createMyReqErrorListener(final ERROR_LISTNENER_ORIGIN origin) {
        return new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                switch (origin) {
                    case LOADING:
                        Log.e(TAG, "Error loading entry");
                        break;
                    case SAVING:
                        Log.e(TAG, "Error saving entry");
                        break;
                    case UNSAVING:
                        Log.e(TAG, "Error unsaving entry");
                        break;
                    case MARKING:
                        Log.e(TAG, "Error marking entry");
                        break;

                    default:
                        break;
                }
                if (error != null && error.getMessage() != null) {
                    Log.e(TAG, error.getMessage());
                }
            }
        };
    }

    enum ERROR_LISTNENER_ORIGIN {
        LOADING, UNSAVING, SAVING, MARKING;
    }

    /**
     * FragmentStatePageAdapter
     *
     * @author superyair
     */
    public static class EntryPageAdapter extends FragmentStatePagerAdapter {

        AppData appData;

        public EntryPageAdapter(FragmentManager fm, AppData appData) {
            super(fm);
            this.appData = appData;
        }

        @Override
        public int getCount() {
            return appData.getLastStreamLoaded().size();
        }

        @Override
        public Fragment getItem(int position) {
            List<ListEntry> mEntries = appData.getLastStreamLoaded();
            ListEntry listEntry = mEntries.get(position);
            FeedEntryFragment myFragment = FeedEntryFragment.newInstance(listEntry.getId());
            return myFragment;
        }
    }

    private class EntryPageChangeListener extends ViewPager.SimpleOnPageChangeListener {
        AppData appData;

        public EntryPageChangeListener(AppData appData) {
            this.appData = appData;
        }

        @Override
        public void onPageSelected(int position) {
            entry = appData.getLastStreamLoaded().get(position);
            supportInvalidateOptionsMenu();
        }
    }
}
