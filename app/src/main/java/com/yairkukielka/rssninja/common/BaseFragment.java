package com.yairkukielka.rssninja.common;

import android.os.Bundle;

import com.anprosit.android.dagger.ui.DaggerFragment;

/**
 * Base Fragment
 */
public abstract class BaseFragment extends DaggerFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

}
