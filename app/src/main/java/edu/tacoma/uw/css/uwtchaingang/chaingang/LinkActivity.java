package edu.tacoma.uw.css.uwtchaingang.chaingang;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;

import chain.Chain;
import link.Link;

import static android.support.constraint.Constraints.TAG;
/**
 * The Link behavior and its content is handled in the Link class.
 *
 * @author Michael Quandt
 * @author James E Johnston
 * @author Denis Yakovlev
 * @version 20 May 2017
 */
public class LinkActivity extends AppCompatActivity {

    public static final String LINK_ACTIVITY = "LINK_ACTIVITY";

    public final static String CHAIN_LINKS_URL =
            "http://chaingangwebservice.us-west-2.elasticbeanstalk.com/chains/links?";

    public static final String NOTIFY_WARDEN_URL =
            "http://chaingangwebservice.us-west-2.elasticbeanstalk.com/update/link?";

    private Link mLink;
    private Chain mChain;
    private CheckBox mTaskCheckerA;
    private TextView mExtResA;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_link);
        setTitle(R.string.link_activity_title);
        Intent intent = getIntent();
        mLink = (Link) intent.getSerializableExtra(ChainActivity.EXTRA_LINK);
        mChain = (Chain) intent.getSerializableExtra(ChainActivity.EXTRA_CHAIN);
        mTaskCheckerA = (CheckBox) findViewById(R.id.taskCheckerA);
        mTaskCheckerA.setText(mLink.getmLinkInst());
        mExtResA = (TextView) findViewById(R.id.externalResA);
        mExtResA.setText(mLink.getmExtSiteName());
        mExtResA.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri webpage = Uri.parse(mLink.getmExtURL());
                Intent intent = new Intent(Intent.ACTION_VIEW, webpage);
                startActivity(intent);
            }
        });
        mExtResA.setMovementMethod(LinkMovementMethod.getInstance());
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Button completeButton = findViewById(R.id.completeButton);
        if (mLink.ismIsCompleted()) {
            completeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mTaskCheckerA.isChecked()) {
                        ConnectivityManager connMgr
                                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

                        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
                        if (networkInfo != null && networkInfo.isConnected()) {
                            String notifyURL = buildNotifyWardenURL();
                            NotifyAsyncTask notifyAsync = new NotifyAsyncTask();
                            notifyAsync.execute(new String[]{notifyURL});
                            getSupportFragmentManager().popBackStackImmediate();
                        }
                    }
                }
            });
        }
    }

    private void loadUpdatedChain() {
        Intent intent = new Intent(this, ChainActivity.class);
        intent.putExtra(ChainListFragment.CHAIN_SELECTED, mChain);
        startActivity(intent);
        finish();
    }
    /**
     * Building the url to obtain updated links for this member.
     *
     * @return string with the chain data.
     */
    private String buildLinksURL() {

        StringBuilder sb = new StringBuilder(CHAIN_LINKS_URL);

        try {

            String email = mChain.getmMemberID();
            sb.append("member=");
            sb.append(URLEncoder.encode(email, "UTF-8"));


            String chainTitle = mChain.getmChainTitle();
            sb.append("&chainTitle=");
            sb.append(URLEncoder.encode(chainTitle, "UTF-8"));

            Log.i(TAG, sb.toString());

        }
        catch(Exception e) {
            Toast.makeText(this, "Something wrong with the url" + e.getMessage(), Toast.LENGTH_LONG)
                    .show();
        }
        return sb.toString();
    }

    /**
     * Building the string member url
     * member=abc@abc.com&chainTitle=Title%20of%20this%20Chain&link_id=1
     *
     * @return string with user name and password
     */
    private String buildNotifyWardenURL() {

        StringBuilder sb = new StringBuilder(NOTIFY_WARDEN_URL);

        try {

            String email = mChain.getmMemberID();
            sb.append("member=");
            sb.append(URLEncoder.encode(email, "UTF-8"));


            String chainTitle = mChain.getmChainTitle();
            sb.append("&chainTitle=");
            sb.append(URLEncoder.encode(chainTitle, "UTF-8"));

            Integer nextLinkId = new Integer(mLink.getmLinkID() + 1);
            sb.append("&link_id=");
            sb.append(URLEncoder.encode(nextLinkId.toString(), "UTF-8"));

            Log.i(TAG, sb.toString());

        }
        catch(Exception e) {
            Toast.makeText(this, "Something wrong with the url" + e.getMessage(), Toast.LENGTH_LONG)
                    .show();
        }
        return sb.toString();
    }



    /**
     * Class to get a list of chains, synchronized with the member database
     */
    private class LinkAsyncTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... urls) {
            Log.i("", "Doinbackground");
            String response = "";
            HttpURLConnection urlConnection = null;

            for (String url : urls) {
                try {
                    URL urlObject = new URL(url);
                    urlConnection = (HttpURLConnection) urlObject.openConnection();

                    Log.i("Async Task Tag", url);

                    InputStream content = urlConnection.getInputStream();

                    BufferedReader buffer = new BufferedReader(new InputStreamReader(content));
                    String s = "";
                    while ((s = buffer.readLine()) != null) {
                        response += s;
                    }

                } catch (Exception e) {
                    response = "No Network";
                }
                finally {
                    if (urlConnection != null)
                        urlConnection.disconnect();
                }
            }
            Log.i("Async Task Tag", "doInBackground: " + response);
            return response;
        }

        /**
         * It checks the list of chains
         *
         * @param result
         */
        @Override
        protected void onPostExecute(String result) {
            Log.i("", "onPostExecute");

            if (result.startsWith("No Network")) {
                Toast.makeText(getApplicationContext(), result, Toast.LENGTH_LONG)
                        .show();
                return;
            }
            try {
                mChain.setMchainsInLink( Link.parseLinkJSON(result));
                loadUpdatedChain();

            }
            catch (JSONException e) {
                Log.i("onPostExecute", e.getMessage());
                Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG)
                        .show();
                return;
            }
        }

    }

    /**
     * Class to get a list of chains, synchronized with the member database
     */
    private class NotifyAsyncTask extends AsyncTask<String, Void, String> {


        @Override
        protected String doInBackground(String... urls) {
            Log.i("", "Doinbackground");
            String response = "";
            HttpURLConnection urlConnection = null;

            for (String url : urls) {
                try {
                    URL urlObject = new URL(url);
                    urlConnection = (HttpURLConnection) urlObject.openConnection();

                    Log.i("Async Task Tag", url);

                    InputStream content = urlConnection.getInputStream();

                    BufferedReader buffer = new BufferedReader(new InputStreamReader(content));
                    String s = "";
                    while ((s = buffer.readLine()) != null) {
                        response += s;
                    }

                } catch (Exception e) {
                    response = "No Network";
                }
                finally {
                    if (urlConnection != null)
                        urlConnection.disconnect();
                }
            }
            Log.i("Async Task Tag", "doInBackground: " + response);
            return response;
        }

        /**
         * It checks the list of chains
         *
         * @param result
         */
        @Override
        protected void onPostExecute(String result) {
            Log.i("postEx_Notify", result);

            if (result.startsWith("No Network")) {
                Toast.makeText(getApplicationContext(), result, Toast.LENGTH_LONG)
                        .show();
                return;
            }

            String linksURL = buildLinksURL();
            LinkAsyncTask linkAsyncTask = new LinkAsyncTask();
            linkAsyncTask.execute(new String[]{linksURL});
            return;

        }

    }

}
