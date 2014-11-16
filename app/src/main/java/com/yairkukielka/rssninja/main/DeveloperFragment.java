package com.yairkukielka.rssninja.main;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.yairkukielka.rssninja.LoadingFragment;
import com.yairkukielka.rssninja.R;
import com.yairkukielka.rssninja.common.BaseFragment;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class DeveloperFragment extends BaseFragment {
    private static final String ABOUT_ME_URL = "http://www.about.me/yair.kukielka";
    @InjectView(R.id.developer_webview)
    WebView webView;
    // animation to show the feed content after the loading fragment shows
    private Animation webViewAnimation;
    // fragment that shows while loading the entry content
    private Fragment loadingFragment;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.developer_layout, container, false);
        ButterKnife.inject(this, view);

        return view;
    }

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        webView.setVisibility(View.INVISIBLE);
        webView.getSettings().setJavaScriptEnabled(true);
        // animate webview content
        webViewAnimation = AnimationUtils.loadAnimation(getActivity(), R.anim.push_up_in);
        loadingFragment = new LoadingFragment();
        getFragmentManager().beginTransaction().replace(R.id.developer_frame_webview, loadingFragment).attach(loadingFragment).addToBackStack(null).commitAllowingStateLoss();

        webView.loadUrl(ABOUT_ME_URL);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                if (getActivity() != null) {
                    FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                    if (fragmentManager.findFragmentById(loadingFragment.getId()) != null) {
                        fragmentManager.beginTransaction().remove(loadingFragment).commitAllowingStateLoss();
                    }
                }
                webView.setAnimation(webViewAnimation);
                webView.setVisibility(View.VISIBLE);
            }
        });
    }


}
