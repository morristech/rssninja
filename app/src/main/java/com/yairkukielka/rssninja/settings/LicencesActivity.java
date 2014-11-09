package com.yairkukielka.rssninja.settings;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.MenuItem;
import android.widget.TextView;

import com.yairkukielka.rssninja.R;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class LicencesActivity extends FragmentActivity {
    // private static final String TAG = LicencesActivity.class.getSimpleName();

    private static final String VERSION_UNAVAILABLE = "N/A";
    /**
     * version textview
     */
    @InjectView(R.id.app_version)
    TextView versionView;
    /**
     * licences textview
     */
    @InjectView(R.id.licencesTextView)
    TextView licencesBodyView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.licences_layout);
        ButterKnife.inject(this);

        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        // Get app version
        PackageManager pm = getPackageManager();
        String packageName = getPackageName();
        String versionName;
        try {
            PackageInfo info = pm.getPackageInfo(packageName, 0);
            versionName = info.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            versionName = VERSION_UNAVAILABLE;
        }
        versionView.setText(Html.fromHtml(getString(R.string.about_version_template, versionName)));
        licencesBodyView.setText(Html.fromHtml(getString(R.string.licences_text)));
        licencesBodyView.setMovementMethod(new LinkMovementMethod());
    }


    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            animate();
        }
    }

    @TargetApi(14)
    private void animate() {
        licencesBodyView.setAlpha(0);
        licencesBodyView.animate().alpha(1).setStartDelay(250).setDuration(1000);

        versionView.setAlpha(0);
        versionView.animate().alpha(1).setStartDelay(250).setDuration(1000);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                overridePendingTransition(R.anim.open_main, R.anim.close_next);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
