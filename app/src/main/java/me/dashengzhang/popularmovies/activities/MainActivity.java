package me.dashengzhang.popularmovies.activities;

import android.content.ContentUris;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import me.dashengzhang.popularmovies.R;
import me.dashengzhang.popularmovies.Utility;
import me.dashengzhang.popularmovies.asynctasks.FetchMovieTask;
import me.dashengzhang.popularmovies.asynctasks.FetchReviewTask;
import me.dashengzhang.popularmovies.asynctasks.FetchTrailerTask;
import me.dashengzhang.popularmovies.data.MovieContract;
import me.dashengzhang.popularmovies.fragments.DetailFragment;
import me.dashengzhang.popularmovies.fragments.MovieFragment;

public class MainActivity extends AppCompatActivity implements MovieFragment.Callback {

    private static final String DETAILFRAGMENT_TAG = "DFTAG";
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

        if (savedInstanceState == null) {
            FetchMovieTask fetchMovieTask = new FetchMovieTask(this, mSorting, mVote);
            fetchMovieTask.execute(mSorting);
        }

        if (findViewById(R.id.movie_detail_container) != null) {
            mTwoPane = true;
            if (savedInstanceState == null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.movie_detail_container, new DetailFragment(), DETAILFRAGMENT_TAG)
                        .commit();
            }
        } else mTwoPane = false;
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
}
