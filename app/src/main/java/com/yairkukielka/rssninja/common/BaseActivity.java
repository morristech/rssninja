package com.yairkukielka.rssninja.common;

import com.anprosit.android.dagger.ui.DaggerFragmentActivity;

import java.util.ArrayList;
import java.util.List;

import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

/**
 * Base activity.
 */
public abstract class BaseActivity extends DaggerFragmentActivity implements BaseActivityView {

    @Override
    protected List<Object> getModules() {
        return new ArrayList<Object>();
    }

    public void showMessage(String message, Style style) {
        Crouton.makeText(this, message, style).show();
    }
}
