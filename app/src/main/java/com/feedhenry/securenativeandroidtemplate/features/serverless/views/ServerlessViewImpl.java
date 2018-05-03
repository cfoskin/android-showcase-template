package com.feedhenry.securenativeandroidtemplate.features.serverless.views;

import android.app.Fragment;

import com.feedhenry.securenativeandroidtemplate.mvp.views.BaseAppView;

/**
 * Created by cfoskin on 03/05/18.
 */

public abstract class ServerlessViewImpl extends BaseAppView implements ServerlessView{
    public ServerlessViewImpl(Fragment fragment) {
        super(fragment);
    }
}
