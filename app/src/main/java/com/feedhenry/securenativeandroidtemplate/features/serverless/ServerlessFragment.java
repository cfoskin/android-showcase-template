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
import org.aerogear.mobile.security.SecurityService;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import dagger.android.AndroidInjection;
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

    @BindView(R.id.serverlessTextToSend)
    TextInputEditText serverlessTextToSend;

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

        //get openwhisk configuration from mobile services json
        Map<String,String> openwhiskConfig = ConfigureOpenwhisk();
        // set up listener
        serverlessButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //send request
                new ServerlessService(openwhiskConfig.get("url"), openwhiskConfig.get("authString")).execute();
            }
        });
        return view;
    }

    /**
     *
     * @return Map of the openwhisk configuration
     */
    private Map<String, String> ConfigureOpenwhisk() {
        Map<String,String> openwhiskConfig = new HashMap<>();
        ServiceConfiguration serviceConfig = mobileCore.getServiceConfiguration("serverless");
        Map<String, String > serviceConfigProperties = serviceConfig.getProperties();
        String authString = serviceConfigProperties.get("encoded_secret");
        String openwhiskBaseUrl = serviceConfig.getUrl();
        String action = serviceConfigProperties.get("action_name");
        String openwhiskUrl = "https://" + openwhiskBaseUrl  +  "/api/v1/namespaces/_/actions/" + action;
        openwhiskConfig.put("url", openwhiskUrl);
        openwhiskConfig.put("authString", authString);
        return  openwhiskConfig;
    }

    /**
     * Class for doing the http request and updating the UI with the response
     */
    class ServerlessService extends AsyncTask<Void, Void, Void> {
        String url, authString;

        private ServerlessService(String url, String authString){
            this.authString = authString;
            this.url = url;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            String apiUrl = this.url;
            String credentials = this.authString;
            String token = "Basic "  + credentials;
            try {
                HttpUrl.Builder urlBuilder = HttpUrl.parse(apiUrl).newBuilder();
                urlBuilder.addQueryParameter("blocking", "true");
                urlBuilder.addQueryParameter("result", "true");
                String url  = urlBuilder.build().toString();
                MediaType JSON
                        = MediaType.parse("application/json; charset=utf-8");
                String input = String.format("{\"name\":\"%s\"}", serverlessTextToSend.getText().toString());
                RequestBody body = RequestBody.create(JSON, input );
                Request request = new Request.Builder()
                        .addHeader("Authorization", token)
                        .addHeader("Content-Type", "application/json")
                        .url(url)
                        .post(body)
                        .build();
                OkHttpClient client = new OkHttpClient();

                Response response = client.newCall(request).execute();
                if (response.isSuccessful()) {
                    String responseText = response.body().string();
                    //update the UI
                    updateUi(responseText);
                    response.close();
                }
            } catch (Exception e) {
                Log.e(TAG, "Error calling Openwhisk", e);
            }
            return null;
        }

        /**
         *
         * @param responseText Update the Ui using the response body
         */
        private void updateUi(String responseText){
            try {
                JSONObject jsonObject = new JSONObject(responseText);
                String textToDisplay = jsonObject.get("result").toString();
                serverlessButton.setText(textToDisplay);

            } catch (Throwable t) {
                Log.e(TAG, "Could not parse malformed JSON: \"" + responseText + "\"");
            }
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
