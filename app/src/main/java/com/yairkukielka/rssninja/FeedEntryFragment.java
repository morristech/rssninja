package com.yairkukielka.rssninja;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Html;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings.LayoutAlgorithm;
import android.webkit.WebSettings.PluginState;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.anprosit.android.dagger.ui.DaggerFragment;
import com.squareup.picasso.Picasso;
import com.yairkukielka.rssninja.feedly.Entry;
import com.yairkukielka.rssninja.interactors.FeedlyInteractor;
import com.yairkukielka.rssninja.settings.PreferencesActivity;
import com.yairkukielka.rssninja.toolbox.DateUtils;

import java.util.List;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import retrofit.Callback;
import retrofit.RetrofitError;

/**
 * Feed entry fragment
 */
public class FeedEntryFragment extends DaggerFragment {

    public static final String ACCESS_TOKEN = "accessToken";
    private static final String TAG = FeedEntryFragment.class.getSimpleName();
    private static final String ENTRY_PATH = "/v3/entries/";
    private static final String ENTRY_ID = "entryId";
    private static final String BY = " by ";
    private static final String encoding = "utf-8";
    private static final String TEXT_HTML = "text/html";
    private static final String HTML_OPEN_TAG = "<html>";
    private static final String BODY_CLOSE_HTML_CLOSE_TAGS = "</body></html>";
    private static final String HTML_BODY_TAG = "<body>";
    private static final String HTML_HEAD = "<head>"
            + "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">"
            + "<style>@font-face {font-family: 'myFont';src: url('file:///android_asset/fonts/Roboto-Light.ttf');}"
            + "body {font-family: 'myFont';line-height:150%;}a:link {color:#70B002;}img{max-width: 100%; width:auto; height: auto;}"
            + "iframe{max-width: 100%; width:auto; height: auto;}</style></head>";
    private static final String DIV_PREFIX = "<div style='background-color:transparent;padding-left: 10px;padding-right: 10px;padding-bottom: 30px;color:#888;font-family: myFont';>";
    private static final String DIV_SUFIX = "</div>";
    @Inject
    FeedlyInteractor feedlyInteractor;
    String entryId;
    @InjectView(R.id.entry_webview)
    WebView webView;
    @InjectView(R.id.transparent_view)
    View transparentView;
    @InjectView(R.id.entry_bg_image_view)
    ImageView bgImage;
    @InjectView(R.id.entry_layout)
    LinearLayout entryLayout;
    @InjectView(R.id.feed_entry_title_layout)
    RelativeLayout titleLayout;
    /**
     * title text view
     */
    @InjectView(R.id.entry_title)
    TextView tvTitle;
    /**
     * date text view
     */
    @InjectView(R.id.entry_date)
    TextView tvDate;
    /**
     * author text view
     */
    @InjectView(R.id.entry_author)
    TextView tvAuthor;
    // activity
    FeedEntryListener mCallback;
    // the feed entry
    private Entry entry;
    // animation to show the feed content after the loading fragment shows
    private Animation webviewContentPushUpAnimation;
    // animation to show the title fading in
    private Animation titleFadeInAnimation;

    public static FeedEntryFragment newInstance(String entryId) {
        FeedEntryFragment f = new FeedEntryFragment();

        // Supply index input as an argument.
        Bundle args = new Bundle();
        args.putString("entryId", entryId);
        f.setArguments(args);

        return f;
    }

    public static float dipToPixels(Context context, float dipValue) {
        if (context != null) {
            DisplayMetrics metrics = context.getResources().getDisplayMetrics();
            return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dipValue, metrics);
        } else {
            return 60;
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
//        ImageView imageView = (ImageView)findViewById(R.id.my_image);
        View view = inflater.inflate(R.layout.feed_entry_layout, container, false);
        ButterKnife.inject(this, view);

        getArgs();
        webView.setBackgroundColor(0x00000000);
        // animate webview content
        webviewContentPushUpAnimation = AnimationUtils.loadAnimation(getActivity(), R.anim.push_up_in);
        titleFadeInAnimation = AnimationUtils.loadAnimation(getActivity(), R.anim.fade_in_title);
        titleLayout.setAnimation(titleFadeInAnimation);


        return view;
    }

    @OnClick(R.id.feed_entry_title_layout)
    void onEntryClick(View v) {
        if (entry != null && entry.getUrl() != null) {
            mCallback.openEntryInBrowser();
        }

    }

    private void getArgs() {
        Bundle args = getArguments();
        entryId = args.getString("entryId");
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        feedlyInteractor.getEntry(entryId, getEntryCallback());
    }

    /**
     * Get the callback for getSubscriptions
     */
    private Callback<List<Entry>> getEntryCallback() {
        return new Callback<List<Entry>>() {
            @Override
            public void success(List<Entry> entries, retrofit.client.Response response) {
                Entry entry = entries.get(0);
                tvTitle.setText(Html.fromHtml(entry.getTitle()));
                if (entry.getAuthor() != null) {
                    tvAuthor.setText(BY + entry.getAuthor());
                }
                if (entry.getPublished() != null) {
                    try {
                        tvDate.setText(DateUtils.dateToString(entry.getPublished()));
                    } catch (IllegalArgumentException ie) {
                    }
                }
                // set title for webview
                // entryLayout.removeView(titleLayout);
                // ((TitleBarWebView)
                // webView).setEmbeddedTitleBar(titleLayout);

                if (entry.getVisual() != null) {
                    Picasso.with(getActivity()).load(entry.getVisual()).into(bgImage);
                    //bgImage.setImageUrl(entry.getVisual(), MyVolley.getImageLoader());
                    bgImage.setAnimation(titleFadeInAnimation);
                    // height of the transparent view is 60dp
                    transparentView.getLayoutParams().height = (int) dipToPixels(getActivity(), 60);
                    // this animation is only for when there is an image
                    webView.setAnimation(webviewContentPushUpAnimation);
                }

                webView.getSettings().setJavaScriptEnabled(true);
                webView.getSettings().setBuiltInZoomControls(true);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                    webView.getSettings().setDisplayZoomControls(false);
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    webView.getSettings().setLayoutAlgorithm(LayoutAlgorithm.TEXT_AUTOSIZING);
                } else {
                    webView.getSettings().setLayoutAlgorithm(LayoutAlgorithm.NORMAL);
                    // for HTML5 videos
                    webView.getSettings().setPluginState(PluginState.ON);
                    webView.getSettings().setDomStorageEnabled(true);
                }
                int fontSizeDimension = getPrefTextSize();
                webView.getSettings().setDefaultFontSize(fontSizeDimension);
                webView.setWebChromeClient(new WebChromeClient());
                webView.setWebViewClient(new WebViewClient() {

                    @Override
                    public boolean shouldOverrideUrlLoading(WebView view, String url) {
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                        startActivity(intent);
                        return true;
                    }

                    @Override
                    public void onPageFinished(WebView view, String url) {
                        // mark entry as read
                        if (mCallback != null) {
                            mCallback.onFinishedLoading();
                        }
                    }
                });
                loadEntryInInnerBrowser(entry.getContent());

            }

            @Override
            public void failure(RetrofitError retrofitError) {
                Log.e(TAG, "Error getting entry");
                showErrorResponse(retrofitError, ERROR_LISTNENER_ORIGIN.LOADING);
            }
        };
    }

    public int getPrefTextSize() {
        Integer pageSize = 18;
        try {
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
            String sizeString = sharedPref.getString(PreferencesActivity.KEY_TEXT_SIZE, "18");
            pageSize = Integer.parseInt(sizeString);
        } catch (Exception e) {
            Log.e(TAG, "Error parsing text size. Using default 18");
        }
        return pageSize;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mCallback = (FeedEntryListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement OnHeadlineSelectedListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        try {
            mCallback = null;
        } catch (Exception e) {
            Log.e(TAG, "Error detaching activity from fragment");
        }
    }

    /**
     * For pausing youtube videos and other
     */
    @Override
    public void onPause() {
        super.onPause();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            webView.onPause();
        }
    }

    @Override
    public void onResume() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            webView.onResume();
        }
        super.onResume();
    }

    /**
     * Loads the entry in the internal browser
     */
    private void loadEntryInInnerBrowser(String content) {
        webView.loadDataWithBaseURL("file:///android_asset/", getHtmlData(content), TEXT_HTML, encoding, null);
    }

    private String getHtmlData(String data) {
        StringBuilder sBuilder = new StringBuilder(HTML_OPEN_TAG).append(HTML_HEAD).append(HTML_BODY_TAG)
                .append(DIV_PREFIX).append(data).append(DIV_SUFIX).append(BODY_CLOSE_HTML_CLOSE_TAGS);
        return sBuilder.toString();
    }

    public void showErrorResponse(RetrofitError error, ERROR_LISTNENER_ORIGIN origin) {
        switch (origin) {
            case LOADING:
                Log.e(TAG, "Error loading entry");
                break;
            case SAVING:
                Log.e(TAG, "Error saving entry");
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


    enum ERROR_LISTNENER_ORIGIN {
        LOADING, SAVING, MARKING;
    }

    // Container Activity must implement this interface
    public interface FeedEntryListener {

        public void openEntryInBrowser();

        public void onFinishedLoading();
    }


}
