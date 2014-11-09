package com.yairkukielka.rssninja.main;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.yairkukielka.rssninja.AppData;
import com.yairkukielka.rssninja.DeveloperFragment;
import com.yairkukielka.rssninja.ExpandableListAdapter;
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

public class MainActivity extends BaseActivity implements MainView, ActionBar.OnNavigationListener {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int PREF_CODE_ACTIVITY = 0;
    private static final int LOGIN_CODE_ACTIVITY = 1;
    @Inject
    SharedPreferences sharedPreferences;
    @Inject
    Lazy<MainPresenter> presenter;
    @Inject
    App application;
    @InjectView(R.id.tv_about_developer)
    TextView tvAboutDeveloper;
    @InjectView(R.id.linear_layout)
    LinearLayout linearLayout;
    @InjectView(R.id.progress)
    ProgressBar progressBar;
    @InjectView(R.id.expandable)
    ExpandableListView mDrawerList;
    @InjectView(R.id.drawer_layout)
    DrawerLayout mDrawerLayout;
    private boolean preferencesChanged = false;
    private Menu actionBarmenu;
    private MenuItem reloadMenuItem;
    private ExpandableListAdapter drawerListAdapter;
    private ActionBarDrawerToggle mDrawerToggle;
    private CharSequence mDrawerTitle;
    private CharSequence mTitle;
    private boolean startConnection = true;

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
        tvAboutDeveloper.setOnClickListener(mAboutDeveloperOnClickListener);
        mDrawerTitle = getApplicationName();
        setTitle(getApplicationName());
        startConnection = true;
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
        drawerListAdapter = new ExpandableListAdapter(this, getCategories(), getCategorySubscriptionsMap());
        mDrawerList.setAdapter(drawerListAdapter);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (mDrawerLayout.isDrawerOpen(linearLayout)) {
                    mDrawerLayout.closeDrawer(linearLayout);
                } else {
                    mDrawerLayout.openDrawer(linearLayout);
                }
                return true;
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
        drawerListAdapter.notifyDataSetChanged();
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
        if (!mDrawerLayout.isDrawerOpen(linearLayout)) {
            mDrawerLayout.openDrawer(linearLayout);
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

    /* Called whenever we call invalidateOptionsMenu() */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // If the nav drawer is open, hide action items related to the content
        // view
        boolean drawerOpen = mDrawerLayout.isDrawerOpen(linearLayout);
        hideMenuItems(menu, !drawerOpen);
        return super.onPrepareOptionsMenu(menu);
    }

    private void hideMenuItems(Menu menu, boolean visible) {
        for (int i = 0; i < menu.size(); i++) {
            menu.getItem(i).setVisible(visible);
        }
    }

    /**
     * A Subscription is selected
     */
    private void selectItem(ExpandableListView parent, int groupPosition, int childPosition) {
        Subscription chosenSub = getAppData().findSubscriptionByCategoryAndSubPosition(groupPosition, childPosition);
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

        int index = parent.getFlatListPosition(ExpandableListView.getPackedPositionForChild(groupPosition,
                childPosition));
        parent.setItemChecked(index, true);
        mDrawerLayout.closeDrawer(linearLayout);
    }

    /**
     * A category is selected
     */
    private void selectItem(ExpandableListView parent, int groupPosition) {
        Category category = getCategories().get(groupPosition);
        if (!FeedlyConstants.FEEDLY_CATEGORIES.equals(category.getId())) {
            // if feedly categories chosen, don't look for items
            showEntriesFragment(new StreamId(category.getId(), false));
        }
        // Highlight the selected item, update the title, and close the drawer
        setTitle(category.getLabel());
        int index = parent.getFlatListPosition(ExpandableListView.getPackedPositionForGroup(groupPosition));
        parent.setItemChecked(index, true);
    }

    @Override
    public boolean onNavigationItemSelected(int itemPosition, long itemId) {
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        actionBarmenu = menu;
        return true;
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

    /**
     * Configures the navigation drawer
     */
    private void configureNavigationDrawer() {
        mDrawerToggle = new ActionBarDrawerToggle(this, /* host Activity */
                mDrawerLayout, /* DrawerLayout object */
                R.drawable.ic_drawer, /* nav drawer icon to replace 'Up' caret */
                R.string.drawer_open, /* "open drawer" description */
                R.string.drawer_close /* "close drawer" description */
        ) {

            /**
             * Called when a drawer has settled in a completely closed state.
             */
            public void onDrawerClosed(View view) {
                ActionBar actionBar = getActionBar();
                if (actionBar != null) {
                    actionBar.setTitle(mTitle);
                }
                supportInvalidateOptionsMenu(); // creates call to
                // onPrepareOptionsMenu()
            }

            /**
             * Called when a drawer has settled in a completely open state.
             */
            public void onDrawerOpened(View drawerView) {
                ActionBar actionBar = getActionBar();
                if (actionBar != null) {
                    actionBar.setTitle(mDrawerTitle);
                }
                supportInvalidateOptionsMenu(); // creates call to
                // onPrepareOptionsMenu()
            }
        };
        // Set the drawer toggle as the DrawerListener
        mDrawerLayout.setDrawerListener(mDrawerToggle);
        mDrawerLayout.setFocusableInTouchMode(false);

        // Listview on child click listener
        mDrawerList.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                showEntryListFragment();
                selectItem(parent, groupPosition, childPosition);
                return true;
            }
        });
        // Listview on child click listener
        mDrawerList.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
                showEntryListFragment();
                selectItem(parent, groupPosition);
                return false;
            }
        });

        // set a custom shadow that overlays the main content when the drawer
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
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


    /**
     * Listener for the 'about developer' item
     */
    private final View.OnClickListener mAboutDeveloperOnClickListener = new View.OnClickListener() {
        public void onClick(View v) {
            if (v == tvAboutDeveloper) {
                Fragment developerFragment = getDeveloperFragment();
                if (developerFragment == null) {
                    developerFragment = new DeveloperFragment();
                    FragmentManager fragmentManager = getSupportFragmentManager();
                    fragmentManager.beginTransaction().replace(R.id.content_frame, developerFragment, "developerFragment").commit();
                    setTitle(getResources().getString(R.string.about_developer));
                }
                mDrawerLayout.closeDrawer(linearLayout);

            }
        }
    };

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

    private AppData getAppData() {
        App applicState = ((App) getApplicationContext());
        return applicState.getAppData();
    }

    private List<Category> getCategories() {
        return getAppData().getCategories();
    }

    private HashMap<String, List<Subscription>> getCategorySubscriptionsMap() {
        return getAppData().getCategorySubscriptionsMap();
    }
}
