package com.dashengz.popularmovies.activities;

import android.content.ContentUris;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.dashengz.popularmovies.R;
import com.dashengz.popularmovies.Utility;
import com.dashengz.popularmovies.asynctasks.FetchReviewTask;
import com.dashengz.popularmovies.asynctasks.FetchTrailerTask;
import com.dashengz.popularmovies.data.MovieContract;
import com.dashengz.popularmovies.fragments.DetailFragment;
import com.dashengz.popularmovies.fragments.MovieFragment;
import com.dashengz.popularmovies.gcm.RegistrationIntentService;
import com.dashengz.popularmovies.sync.PopularMoviesSyncAdapter;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

public class MainActivity extends AppCompatActivity implements MovieFragment.Callback {

    public static final String SENT_TOKEN_TO_SERVER = "sentTokenToServer";
    private static final String DETAILFRAGMENT_TAG = "DFTAG";
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private final String LOG_TAG = MainActivity.class.getSimpleName();
    private String mSorting;
    private String mVote;

    private boolean mTwoPane;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSorting = Utility.getPreferredSorting(this);
        mVote = Utility.getPreferredVote(this);
        setContentView(R.layout.activity_main);

        if (findViewById(R.id.movie_detail_container) != null) {
            mTwoPane = true;
            if (savedInstanceState == null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.movie_detail_container, new DetailFragment(), DETAILFRAGMENT_TAG)
                        .commit();
            }
        } else mTwoPane = false;

        PopularMoviesSyncAdapter.initializeSyncAdapter(this);

        // If Google Play Services is up to date, we'll want to register GCM. If it is not, we'll
        // skip the registration and this device will not receive any downstream messages from
        // our fake server. Because weather alerts are not a core feature of the app, this should
        // not affect the behavior of the app, from a user perspective.
        if (checkPlayServices()) {
            // Because this is the initial creation of the app, we'll want to be certain we have
            // a token. If we do not, then we will start the IntentService that will register this
            // application with GCM.
            SharedPreferences sharedPreferences =
                    PreferenceManager.getDefaultSharedPreferences(this);
            boolean sentToken = sharedPreferences.getBoolean(SENT_TOKEN_TO_SERVER, false);
            if (!sentToken) {
                Intent intent = new Intent(this, RegistrationIntentService.class);
                startService(intent);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        String sorting = Utility.getPreferredSorting(this);
        String vote = Utility.getPreferredVote(this);

        if (!sorting.equals(mSorting)
                || sorting.equals(getString(R.string.pref_sorting_rating)) && !vote.equals(mVote)) {
            MovieFragment movieFragment = (MovieFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_movie);
            if (null != movieFragment) {
                movieFragment.onPrefChanged();
            }
            mSorting = sorting;
            mVote = vote;
        }
    }

    @Override
    public void onItemSelected(Uri movieUri) {
        if (mTwoPane) {
            // In two-pane mode, show the detail view in this activity by
            // adding or replacing the detail fragment using a
            // fragment transaction.
            FetchReviewTask reviewTask = new FetchReviewTask(this, ContentUris.parseId(movieUri));
            FetchTrailerTask trailerTask = new FetchTrailerTask(this, ContentUris.parseId(movieUri));
            reviewTask.execute();
            trailerTask.execute();

            Bundle args = new Bundle();
            args.putParcelable(DetailFragment.DETAIL_URI, movieUri);
            args.putParcelable(DetailFragment.DETAIL_REVIEW_URI,
                    MovieContract.ReviewEntry.buildUriByMovieId(ContentUris.parseId(movieUri)));
            args.putParcelable(DetailFragment.DETAIL_TRAILER_URI,
                    MovieContract.TrailerEntry.buildUriByMovieId(ContentUris.parseId(movieUri)));

            DetailFragment fragment = new DetailFragment();
            fragment.setArguments(args);

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.movie_detail_container, fragment, DETAILFRAGMENT_TAG)
                    .commit();
        } else {
            Intent intent = new Intent(this, DetailActivity.class).setData(movieUri);
            startActivity(intent);
        }
    }

    /**
     * Check the device to make sure it has the Google Play Services APK. If
     * it doesn't, display a dialog that allows users to download the APK from
     * the Google Play Store or enable it in the device's system settings.
     */
    private boolean checkPlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(this, resultCode,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Log.i(LOG_TAG, "This device is not supported.");
                finish();
            }
            return false;
        }
        return true;
    }
}
