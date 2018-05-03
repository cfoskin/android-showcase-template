package com.feedhenry.securenativeandroidtemplate.features.serverless;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.TextView;

import com.feedhenry.securenativeandroidtemplate.R;
import com.feedhenry.securenativeandroidtemplate.features.serverless.presenters.ServerlessPresenter;
import com.feedhenry.securenativeandroidtemplate.features.serverless.views.ServerlessView;
import com.feedhenry.securenativeandroidtemplate.features.serverless.views.ServerlessViewImpl;
import com.feedhenry.securenativeandroidtemplate.mvp.views.BaseFragment;

import org.aerogear.mobile.security.SecurityService;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import dagger.android.AndroidInjection;

/**
 * Created by cfoskin on 03/05/18.
 */

public class ServerlessFragment extends BaseFragment<ServerlessPresenter, ServerlessView> {
    public static final String TAG = "serverless";

    @Inject
    ServerlessPresenter mServerlessPresenter;

    @Inject
    Context context;

    @Inject
    SecurityService securityService;

    @BindView(R.id.serverlessInstruction)
    TextView serverlessInstruction;

    @BindView(R.id.serverlessHint)
    TextInputEditText serverlessHint;

    @BindView(R.id.serverlessButton)
    RadioButton serverlessButton;

    View view;

    public ServerlessFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_serverless, container, false);
        ButterKnife.bind(this, view);

        return view;
    }

    @Override
    protected ServerlessPresenter initPresenter() {
        return mServerlessPresenter;
    }

    @Override
    protected ServerlessView initView() {
        return new ServerlessViewImpl(this) {
        };
    }

    @Override
    public int getHelpMessageResourceId() {
        return R.string.popup_device_fragment;
    }

    @Override
    public void onAttach(Activity activity) {
        AndroidInjection.inject(this);
        super.onAttach(activity);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        this.mServerlessPresenter = null;
    }

}
