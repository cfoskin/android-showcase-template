package com.feedhenry.securenativeandroidtemplate.features.serverless;
import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.TextView;

import com.feedhenry.securenativeandroidtemplate.R;
import com.feedhenry.securenativeandroidtemplate.domain.configurations.AppConfiguration;
import com.feedhenry.securenativeandroidtemplate.features.serverless.presenters.ServerlessPresenter;
import com.feedhenry.securenativeandroidtemplate.features.serverless.views.ServerlessView;
import com.feedhenry.securenativeandroidtemplate.features.serverless.views.ServerlessViewImpl;
import com.feedhenry.securenativeandroidtemplate.mvp.views.BaseFragment;
import com.feedhenry.securenativeandroidtemplate.navigation.Navigator;

import org.aerogear.mobile.core.MobileCore;
import org.aerogear.mobile.core.configuration.ServiceConfiguration;
import org.aerogear.mobile.core.http.HttpRequest;
import org.aerogear.mobile.core.http.HttpResponse;
import org.aerogear.mobile.security.SecurityService;

import java.net.URLDecoder;
import java.util.Base64;
import java.util.Map;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import dagger.android.AndroidInjection;
import okhttp3.Credentials;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


/**
 * Created by cfoskin on 03/05/18.
 */

public class ServerlessFragment extends BaseFragment<ServerlessPresenter, ServerlessView> {
    public static final String TAG = "serverless";

    @Inject
    ServerlessPresenter mServerlessPresenter;

    @Inject
    MobileCore mobileCore;

    @Inject
    Context context;

    @Inject
    Navigator navigator;

    @Inject
    AppConfiguration appConfiguration;

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
        callOpenwhisk();
        return view;
    }

    private void callOpenwhisk() {
        ServiceConfiguration serviceConfig = mobileCore.getServiceConfiguration("serverless");
        Map<String, String > serviceConfigProperties = serviceConfig.getProperties();
        String credentials = serviceConfigProperties.get("credentials");
        String url = serviceConfig.getUrl();
        String action = serviceConfigProperties.get("action");
        String openwhiskUrl = url + action;
        Log.e(TAG, "Openwhisk url : " + openwhiskUrl);
        serverlessButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //send request
                new ServerlessService(openwhiskUrl, credentials).execute();
            }
        });


    }

    class ServerlessService extends AsyncTask<Void, Void, Void> {
        String url, credentials;

        protected ServerlessService(String url, String credentials){
            this.credentials = credentials;
            this.url = url;
        }
        @Override
        protected Void doInBackground(Void... voids) {
            String apiUrl = this.url;
            String credentials = this.credentials;
            String authToken = android.util.Base64.encodeToString(credentials.getBytes(), android.util.Base64.NO_WRAP);
            Log.d(TAG, authToken);
            try {

                HttpUrl.Builder urlBuilder = HttpUrl.parse(apiUrl).newBuilder();
                urlBuilder.addQueryParameter("blocking", "true");
                urlBuilder.addQueryParameter("result", "true");
                String url  = urlBuilder.build().toString();
                MediaType JSON
                        = MediaType.parse("application/json; charset=utf-8");
                RequestBody body = RequestBody.create(JSON, "{\"name\":\"olleH\"}" );

                Request request = new Request.Builder()
                        .addHeader("Authorization", "Basic Nzg5YzQ2YjEtNzFmNi00ZWQ1LThjNTQtODE2YWE0ZjhjNTAyOkFMQkNzaVRzTUF2SllHZEp3cEZkV0lHMGE4NWxOOFBRdzJYeTZQM2N0cVhkdkJjNWY0SE9CQ1RMSEVRUGVhazU="  )
                        .addHeader("Content-Type", "application/json")
                        .url(url)
                        .post(body)
                        .build();
                OkHttpClient client = new OkHttpClient();

                Response response = client.newCall(request).execute();
                Log.d(TAG,  response.body().string());
            } catch (Exception e) {
                Log.e(TAG, "Error - Exception", e);
            }
            return null;
        }
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
