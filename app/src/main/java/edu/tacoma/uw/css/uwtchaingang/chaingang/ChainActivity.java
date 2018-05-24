package edu.tacoma.uw.css.uwtchaingang.chaingang;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import chain.Chain;
import link.Link;

/**
 * Activity to process a member tasks
 */
public class ChainActivity extends AppCompatActivity
        implements ChainListFragment.OnChainListFragmentInteractionListener,
        LinkListFragment.OnLinkListFragmentInteractionListener {

    public static final String CHAIN_ACTIVITY = "CHAIN_ACTIVITY";
    public static final String EXTRA_LINK = "extra_link";

    /**
     * Initialize the ChainList Fragment
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chain);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ChainListFragment chainListFragment = new ChainListFragment();
        getSupportFragmentManager().beginTransaction()
                .add(R.id.chain_container, chainListFragment)
                .commit();

    }

    /**
     * Implementation of OnChainListFragmentInteractionListener interface
     *
     * @param chain for reference of its links.
     */
    @Override
    public void onChainListFragmentInteraction(Chain chain) {
        LinkListFragment linkListFragment = new LinkListFragment();
        Bundle args = new Bundle();
        args.putSerializable(ChainListFragment.CHAIN_SELECTED, chain);
        linkListFragment.setArguments(args);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.chain_container, linkListFragment)
                .addToBackStack(null)
                .commit();

    }

    /**
     * Implementation of OnLinkListFragmentInteractionListener interface
     *
     * @param link for reference of its links.
     */
    @Override
    public void onLinkSelected(Link link) {
        Intent intent = new Intent(this, LinkActivity.class);
        intent.putExtra(EXTRA_LINK, link);
        Log.i(CHAIN_ACTIVITY, Boolean.toString(link.ismIsCompleted()));
        startActivity(intent);

    }


    // ************************************************************************
    // adding logout option

    /**
     * Method creates options menu
     *
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_course, menu);

        return true;
    }

    /**
     * Method handles logout menu option
     *
     * @param item selected option
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_logout) {
            SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.LOGIN_PREFS)
                    , Context.MODE_PRIVATE);
            sharedPreferences.edit().putBoolean(getString(R.string.LOGGEDIN)
                    , false)
                    .commit();

            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    // ************************************************************************

}
