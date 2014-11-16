package com.yairkukielka.rssninja.main;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;

import com.yairkukielka.rssninja.data.AppData;
import com.yairkukielka.rssninja.R;
import com.yairkukielka.rssninja.app.App;
import com.yairkukielka.rssninja.common.BaseActivity;
import com.yairkukielka.rssninja.common.Constants;
import com.yairkukielka.rssninja.feedly.Category;
import com.yairkukielka.rssninja.feedly.FeedlyConstants;
import com.yairkukielka.rssninja.feedly.ItemType;
import com.yairkukielka.rssninja.feedly.MarkAction;
import com.yairkukielka.rssninja.feedly.Subscription;
import com.yairkukielka.rssninja.login.LoginActivity;
import com.yairkukielka.rssninja.ratememaybe.RateMeMaybe;
import com.yairkukielka.rssninja.settings.PreferencesActivity;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import dagger.Lazy;
import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;
import rx.Subscriber;

public class MainActivity extends BaseActivity implements MainView, NavigationDrawerFragment.NavigationDrawerCallbacks {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int PREF_CODE_ACTIVITY = 0;
    private static final int LOGIN_CODE_ACTIVITY = 1;
    @Inject
    SharedPreferences sharedPreferences;
    @Inject
    Lazy<MainPresenter> presenter;
    @Inject
    App application;
    @InjectView(R.id.progress)
    ProgressBar progressBar;
    private boolean preferencesChanged = false;
    private Menu actionBarmenu;
    private MenuItem reloadMenuItem;
    private CharSequence mTitle;
    private boolean startConnection = true;
    private NavigationDrawerFragment mNavigationDrawerFragment;


    @Override
    protected List<Object> getModules() {
        return Arrays.<Object>asList(new MainModule(this));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.drawer_layout);
        ButterKnife.inject(this);

        // ensures that your application is properly initialized with default settings
        //TODO Crashlytics.start(this);
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        configureNavigationDrawer();
        configureActionBar();
        //tvAboutDeveloper.setOnClickListener(mAboutDeveloperOnClickListener);
//        mDrawerTitle = getApplicationName();
        setTitle(getApplicationName());
        if (savedInstanceState == null) {
            startConnection = true;
        } else {
            startConnection = false;
        }
        RateMeMaybe.rateApp(this);

    }

    @Override
    public void startLoginActivity() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivityForResult(intent, LOGIN_CODE_ACTIVITY);
    }

    /**
     * Paints the drawer subscriptions
     */
    @Override
    public void paintDrawerSubscriptions() {
        mNavigationDrawerFragment.notifyDataSetChanged();
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                showSettings();
                return true;
            case R.id.action_refresh:
                refresh();
                return true;
            case R.id.action_mark_read:
                markAsReadLastFeedOrCategory();
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Mark the feed or category as read
     */
    private void markAsReadLastFeedOrCategory() {
        AppData appData = application.getAppData();
        if (appData != null) {
            StreamId lastReadStreamId = appData.getLastReadStreamId();
            if (lastReadStreamId != null) {
                ItemType type = isCategory(lastReadStreamId.getStreamId()) ? ItemType.CATEGORY : ItemType.FEED;
                presenter.get().markAs(lastReadStreamId.getStreamId(), type, MarkAction.READ).subscribe(getMarkAsSubscriber());
            }
        }
    }

    /**
     * True if the stream is a category
     *
     * @param id stream id
     * @return True if the stream is a category
     */
    private boolean isCategory(String id) {
        return id != null && id.contains("category");
    }

    /**
     * A markAsRead subscriber
     *
     * @return markAsRead subscriber
     */
    private Subscriber<Void> getMarkAsSubscriber() {
        return new Subscriber<Void>() {
            @Override
            public void onNext(Void v) {
            }

            @Override
            public void onCompleted() {
                showMessage(application.getResources().getString(R.string.marked_all_as_read_success_message), Style.CONFIRM);
            }

            @Override
            public void onError(Throwable e) {
                showMessage("Error", Style.ALERT);
            }
        };
    }

    @Override
    public void notifyDataSetChanged() {
        mNavigationDrawerFragment.notifyDataSetChanged();
    }


    /**
     * Refresh feeds
     */
    private void refresh() {
        presenter.get().startConnection();
        StreamId lastStreamIdLoaded = presenter.get().getStreamToLoad();
        showEntriesFragment(lastStreamIdLoaded);
    }

    /**
     * Expand and change the refresh icon
     */
    @Override
    public void expandRefreshMenuItem() {
        if (actionBarmenu != null) {
            reloadMenuItem = actionBarmenu.findItem(R.id.action_refresh);
        }
        if (reloadMenuItem != null) {
            reloadMenuItem.setActionView(R.layout.progressbar);
            reloadMenuItem.expandActionView();
        }
    }

    /**
     * Collapse and change the refresh icon
     */
    @Override
    public void collapseRefreshMenuItem() {
        if (actionBarmenu != null) {
            reloadMenuItem = actionBarmenu.findItem(R.id.action_refresh);
        }
        if (reloadMenuItem != null) {
            reloadMenuItem.setActionView(null);
            reloadMenuItem.collapseActionView();
        }
    }

    @Override
    public void onBackPressed() {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            mNavigationDrawerFragment.openDrawer();
        } else {
            new AlertDialog.Builder(this).setTitle(getResources().getString(R.string.exit_confirmation))
                    .setMessage(getResources().getString(R.string.exit_confirmation_text))
                    .setNegativeButton(android.R.string.no, null)
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface arg0, int arg1) {
                            MainActivity.this.finish();
                        }
                    }).create().show();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            getMenuInflater().inflate(R.menu.main, menu);
            actionBarmenu = menu;
            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    /**
     * Show settings activity
     */
    private void showSettings() {
        startActivityForResult(new Intent(this, PreferencesActivity.class), PREF_CODE_ACTIVITY);
    }

    /**
     * Callback for started activities
     */
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PREF_CODE_ACTIVITY) {
            if (resultCode == PreferencesActivity.PREFERENCES_CODE) {
                preferencesChanged = true;
            }
        } else if (requestCode == LOGIN_CODE_ACTIVITY) {
            if (resultCode == LoginActivity.LOGIN_SUCCESS_CODE) {
                startConnection = true;
            }
        }
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        if (preferencesChanged) {
            preferencesChanged = false;
            if (PreferencesActivity.LOG_OUT) {
                logout();
            }
            refresh();
        } else if (startConnection) {
            startConnection = false;
            presenter.get().startConnection();
        }
    }

    private void logout() {
        // log out. Clean accessToken, refreshToken and userId Token
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(Constants.SHPREF_KEY_ACCESS_TOKEN, null);
        editor.putString(Constants.SHPREF_KEY_REFRESH_TOKEN, null);
        editor.putString(Constants.SHPREF_KEY_USERID_TOKEN, null);
        editor.apply();
        PreferencesActivity.LOG_OUT = false;
    }
////////////////////////////////////////////////////////////////////////////






    public void restoreActionBar() {
        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
            actionBar.setDisplayShowTitleEnabled(true);
            actionBar.setTitle(mTitle);
        }
    }

    /**
     * Configures the navigation drawer
     */
    private void configureNavigationDrawer() {

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();
//        drawerListAdapter = new ExpandableListAdapter(this, getCategories(), getCategorySubscriptionsMap());
        //mDrawerList.setAdapter(drawerListAdapter);

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));
    }

    /**
     * A Subscription is selected
     */
    @Override
    public void onNavigationDrawerItemSelected(int groupPosition, int childPosition) {
        showEntryListFragment();
        Subscription chosenSub = application.getAppData().findSubscriptionByCategoryAndSubPosition(groupPosition, childPosition);
        // if it has to get contents from /v3/mixes/contents endpoint, we set the flag here
        Boolean mix;
        if (getResources().getString(R.string.drawer_popular).equals(chosenSub.getTitle())) {
            mix = true;
        } else {
            mix = false;
        }
        showEntriesFragment(new StreamId(chosenSub.getId(), mix));
        // Highlight the selected item, update the title, and close the drawer
        setTitle(chosenSub.getTitle());

    }


    /**
     * A Category is selected
     */
    @Override
    public void onNavigationDrawerGroupSelected(int groupPosition) {
        showEntryListFragment();
        Category category = getCategories().get(groupPosition);
        if (!FeedlyConstants.FEEDLY_CATEGORIES.equals(category.getId())) {
            // if feedly categories chosen, don't look for items
            showEntriesFragment(new StreamId(category.getId(), false));
        }
        // Highlight the selected item, update the title, and close the drawer
        setTitle(category.getLabel());
    }
    /**
     * If the developer fragment is being on screen, it has tu be replaced with the entry list fragment
     */
    private void showEntryListFragment() {
        Fragment fragment = getDeveloperFragment();
        if (fragment != null) {
            replaceContentWithEntryListFragment();
        }
    }

    @Override
    public void replaceContentWithEntryListFragment() {
        EntryListFragment fragment = getEntryListFragment();
        if (fragment == null) {
            fragment = new EntryListFragment();
            getSupportFragmentManager().beginTransaction().replace(R.id.content_frame, fragment, "entriesFragment").commit();
            getSupportFragmentManager().executePendingTransactions();
        }
    }

    /**
     * Tells he entries fragment to load entries from the {@code id} stream
     *
     * @param streamToLoad stream to load
     */
    private void showEntriesFragment(StreamId streamToLoad) {
        EntryListFragment entriesFragment = getEntryListFragment();
        application.getAppData().setLastReadStreamId(streamToLoad);
        if (entriesFragment != null) {
            entriesFragment.setStreamId(streamToLoad);
        } else {
            Log.e(TAG, "problem in showEntriesFragment (not found!)");
        }
    }

    /**
     * Gets the entry list fragment from the fragment manager
     *
     * @return the entry list fragment
     */
    private EntryListFragment getEntryListFragment() {
        Fragment f = getSupportFragmentManager().findFragmentByTag("entriesFragment");
        if (f instanceof EntryListFragment) {
            return (EntryListFragment) f;
        }
        return null;
    }

    private Fragment getDeveloperFragment() {
        return getSupportFragmentManager().findFragmentByTag("developerFragment");
    }

    @Override
    public void onNavigationDrawerAboutDeveloperSelected() {
        Fragment developerFragment = getDeveloperFragment();
        if (developerFragment == null) {
            developerFragment = new DeveloperFragment();
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction().replace(R.id.content_frame, developerFragment, "developerFragment").commit();
            setTitle(getResources().getString(R.string.about_developer));
        }
    }

//    /**
//     * Listener for the 'about developer' item
//     */
//    private final View.OnClickListener mAboutDeveloperOnClickListener = new View.OnClickListener() {
//        public void onClick(View v) {
//            if (v == tvAboutDeveloper) {
//                Fragment developerFragment = getDeveloperFragment();
//                if (developerFragment == null) {
//                    developerFragment = new DeveloperFragment();
//                    FragmentManager fragmentManager = getSupportFragmentManager();
//                    fragmentManager.beginTransaction().replace(R.id.content_frame, developerFragment, "developerFragment").commit();
//                    setTitle(getResources().getString(R.string.about_developer));
//                }
//                mDrawerLayout.closeDrawer(linearLayout);
//
//            }
//        }
//    };

    @Override
    public void setTitle(CharSequence title) {
        mTitle = title;
        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setTitle(mTitle);
        }
    }

    /**
     * Configures the action bar
     */
    private void configureActionBar() {
        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
        }
    }

    @Override
    public void showProgress() {
        expandRefreshMenuItem();
        progressBar.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideProgress() {
        collapseRefreshMenuItem();
        progressBar.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onDestroy() {
        Crouton.cancelAllCroutons();
        super.onDestroy();
    }

    /**
     * Get the application name
     *
     * @return the app name
     */
    private String getApplicationName() {
        int stringId = getApplicationInfo().labelRes;
        return getString(stringId);
    }

    private List<Category> getCategories() {
        return application.getAppData().getCategories();
    }

    private HashMap<String, List<Subscription>> getCategorySubscriptionsMap() {
        return application.getAppData().getCategorySubscriptionsMap();
    }
}
