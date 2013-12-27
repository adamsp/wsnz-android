package speakman.whatsshakingnz.fragments;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.ProgressBar;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import com.google.android.gms.common.GooglePlayServicesUtil;

import speakman.whatsshakingnz.R;


/**
 * Created by Adam Speakman on 24/09/13.
 */
public class LicensesFragment extends DialogFragment {

    private AsyncTask<Void, Void, String> mLicenseLoader;
    
	private static final String GOOGLE_PLAY_SERVICES_LICENSE_TEXT_PLACEHOLDER = "GOOGLE_PLAY_SERVICES_LICENSE_TEXT_PLACEHOLDER";

    public static LicensesFragment newInstance() {
        return new LicensesFragment();
    }

    private WebView mWebView;
    private ProgressBar mIndeterminateProgress;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // TODO Extract this title out into your strings resource file.
        getDialog().setTitle("Open Source licenses");
        View view = inflater.inflate(R.layout.licenses_fragment, container, false);
        mIndeterminateProgress = (ProgressBar)view.findViewById(R.id.licensesFragmentIndeterminateProgress);
        mWebView = (WebView)view.findViewById(R.id.licensesFragmentWebView);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        loadLicenses();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mLicenseLoader != null) {
            mLicenseLoader.cancel(true);
        }
    }

    private void loadLicenses() {
        // Load asynchronously in case of a very large file.
        mLicenseLoader = new AsyncTask<Void, Void, String>() {

            @Override
            protected String doInBackground(Void... params) {
                InputStream rawResource = getActivity().getResources().openRawResource(R.raw.licenses);
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(rawResource));

                String line;
                StringBuilder sb = new StringBuilder();

                try {
                    while ((line = bufferedReader.readLine()) != null) {
                        sb.append(line);
                        sb.append("\n");
                    }
                    bufferedReader.close();
                } catch (IOException e) {
                    // TODO You may want to include some logging here.
                }

                String licenses = sb.toString();
                Context ctx = getActivity();
                if (ctx == null) {
                	return licenses;
                }
                
                String googlePlayLicenses = GooglePlayServicesUtil.getOpenSourceSoftwareLicenseInfo(ctx);
                if (googlePlayLicenses == null) {
                	googlePlayLicenses = "";
                }
                
                licenses = licenses.replace(GOOGLE_PLAY_SERVICES_LICENSE_TEXT_PLACEHOLDER, googlePlayLicenses);
                return licenses;
            }

            @Override
            protected void onPostExecute(String licensesBody) {
                super.onPostExecute(licensesBody);
                if (getActivity() == null || isCancelled()) return;
                mIndeterminateProgress.setVisibility(View.INVISIBLE);
                mWebView.setVisibility(View.VISIBLE);
                mWebView.loadDataWithBaseURL(null, licensesBody, "text/html", "utf-8", null);
                mLicenseLoader = null;
            }

        }.execute();
    }
}
