package com.yairkukielka.rssninja.login;

import android.app.ActionBar;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.yairkukielka.rssninja.R;
import com.yairkukielka.rssninja.common.BaseActivity;
import com.yairkukielka.rssninja.common.Constants;
import com.yairkukielka.rssninja.feedly.AuthRequest;
import com.yairkukielka.rssninja.feedly.AuthResponse;
import com.yairkukielka.rssninja.feedly.FeedlyConstants;
import com.yairkukielka.rssninja.interactors.FeedlyInteractor;

import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import de.keyboardsurfer.android.widget.crouton.Style;
import retrofit.Callback;
import retrofit.RetrofitError;

public class LoginActivity extends BaseActivity {

    private static final String TAG = LoginActivity.class.getSimpleName();
    public static int LOGIN_SUCCESS_CODE = 0;
    public static int LOGIN_ERROR_CODE = 1;
    private static String REDIRECT_URI = "http://localhost";
    @Inject
    FeedlyInteractor feedlyInteractor;
    @Inject
    SharedPreferences sharedPreferences;
    @InjectView(R.id.item_webview)
    WebView webview;
    private String accessToken;
    private String refreshToken;
    private String userId;

    @Override
    protected List<Object> getModules() {
        return Arrays.<Object>asList();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_layout);
        ButterKnife.inject(this);

        accessToken = sharedPreferences.getString(Constants.SHPREF_KEY_ACCESS_TOKEN, null);
        refreshToken = sharedPreferences.getString(Constants.SHPREF_KEY_REFRESH_TOKEN, null);

        if (refreshToken == null) {
            // delete cookies with the sessions
            CookieSyncManager.createInstance(this);
            CookieManager cookieManager = CookieManager.getInstance();
            cookieManager.removeAllCookie();

            WebSettings webSettings = webview.getSettings();
            webSettings.setJavaScriptEnabled(true);
            // need to get access token with OAuth2.0
            // set up webview for OAuth2 login
            webview.setWebViewClient(new WebViewClient() {
                @Override
                public boolean shouldOverrideUrlLoading(WebView view, String url) {
                    if (url.startsWith(REDIRECT_URI)) {

                        if (url.contains("error=")) {
//                            String error = extractError(url);
                            setResult(LOGIN_ERROR_CODE);
                            finish();
                        } else if (url.contains("code=")) {
                            String code = extractCode(url);
                            getTokens(false, code);
                        }
                        // don't go to redirectUri
                        return true;
                    }
                    // load the webpage from url (login and grant access)
                    return false;
                }
            });

            // do OAuth2 login
            webview.loadUrl(FeedlyConstants.ROOT_URL + FeedlyConstants.AUTH_URL);

        } else {
            // only do the refresh authentication token process
            getTokens(true, refreshToken);
        }

    }


    @Override
    public void setTitle(CharSequence title) {
        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setTitle(title);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    /**
     * Asks the Feedly interactor to get the access and refresh tokens
     *
     * @param code access code
     */
    private void getTokens(Boolean isRefresh, String code) {
        AuthRequest request = new AuthRequest();
        if (isRefresh) {
            request.setRefreshToken(code);
            request.setGrantType("refresh_token");
        } else {
            request.setCode(code);
            request.setState("state-of-rssninja");
            request.setGrantType("authorization_code");
            request.setRedirectUri(REDIRECT_URI);
        }
        request.setClientId(FeedlyConstants.CLIENT_ID);
        request.setClientSecret(FeedlyConstants.CLIENT_SECRET);

        Callback<AuthResponse> callback = getAuthCallback();
        feedlyInteractor.authToken(request, callback);

    }


    /**
     * Returns the authentication callback. After the callback executes the activity will be finished.
     *
     * @return the callback
     */
    private Callback<AuthResponse> getAuthCallback() {
        return new Callback<AuthResponse>() {
            @Override
            public void success(AuthResponse authResponse, retrofit.client.Response response) {
                userId = authResponse.getUserId();
                accessToken = authResponse.getAccessToken();
                refreshToken = authResponse.getRefreshToken();
                saveTokensInPreferences(userId, accessToken, refreshToken);
                setResult(LOGIN_SUCCESS_CODE);
                finish();
            }

            @Override
            public void failure(RetrofitError retrofitError) {
                showMessage(getResources().getString(R.string.error_login), Style.ALERT);
                setResult(LOGIN_ERROR_CODE);
                finish();
            }
        };
    }

    /**
     * Save the tokens in preferences and update the feedly interactor with the new accessToken
     *
     * @param userId       userId always the same for a user
     * @param accessToken  accessToken that will expire someday
     * @param refreshToken refreshToken that will be always kept
     */
    private void saveTokensInPreferences(String userId, String accessToken, String refreshToken) {
        feedlyInteractor.setAccessToken(accessToken);
        SharedPreferences.Editor e = sharedPreferences.edit();
        e.putString(Constants.SHPREF_KEY_USERID_TOKEN, userId);
        e.putString(Constants.SHPREF_KEY_ACCESS_TOKEN, accessToken);
        e.putString(Constants.SHPREF_KEY_REFRESH_TOKEN, refreshToken);
        e.apply();
    }


    /**
     * Extract the access token. Url has
     *
     * @param url the url from which to extract the code from. Is has the format:
     *            https://localhost/#access_token=<tokenstring>&token_type=Bearer&expires_in=315359999
     * @return the code (access token)
     */
    private String extractCode(String url) {
        String[] sArray = url.split("code=");
        return (sArray[1].split("&state"))[0];
    }

//    private String extractError(String url) {
//        // url has format
//        // https://localhost/#access_token=<tokenstring>&token_type=Bearer&expires_in=315359999
//        String[] sArray = url.split("error=");
//        return (sArray[1].split("&state"))[0];
//    }


}
