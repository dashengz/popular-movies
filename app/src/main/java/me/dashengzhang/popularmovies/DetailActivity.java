package me.dashengzhang.popularmovies;

import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import me.dashengzhang.popularmovies.data.MovieContract;

public class DetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new DetailFragment())
                    .commit();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Intent intent = getIntent();
        FetchReviewTask reviewTask = new FetchReviewTask(this, ContentUris.parseId(intent.getData()));
        FetchTrailerTask trailerTask = new FetchTrailerTask(this, ContentUris.parseId(intent.getData()));
        reviewTask.execute();
        trailerTask.execute();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_detail, menu);
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

    public static class DetailFragment extends Fragment implements LoaderCallbacks<Cursor> {
        static final int COL_MOVIE_ID = 0;
        static final int COL_MOVIE_TITLE = 1;
        static final int COL_MOVIE_OVERVIEW = 2;
        static final int COL_MOVIE_DATE = 3;
        static final int COL_MOVIE_POSTER_PATH = 4;
        static final int COL_MOVIE_VOTE = 5;
        static final int COL_MOVIE_POPULARITY = 6;
        static final int COL_MOVIE_RATING = 7;
        static final int COL_MOVIE_FAVORITE = 8;
        static final int COL_REVIEW_MOVIE_ID = 1;
        static final int COL_REVIEW_ID = 2;
        static final int COL_AUTHOR = 3;
        static final int COL_CONTENT = 4;
        static final int COL_URL = 5;
        static final int COL_TRAILER_MOVIE_ID = 1;
        static final int COL_TRAILER_ID = 2;
        static final int COL_KEY = 3;
        static final int COL_NAME = 4;
        static final int COL_SITE = 5;
        static final int COL_TYPE = 6;
        private static final String LOG_TAG = DetailFragment.class.getSimpleName();
        private static final int DETAIL_LOADER = 0;
        private static final int TRAILER_LOADER = 1;
        private static final int REVIEW_LOADER = 2;
        private static final String[] MOVIE_COLUMNS = {
                MovieContract.MovieEntry.TABLE_NAME + "." + MovieContract.MovieEntry._ID,
                MovieContract.MovieEntry.COLUMN_TITLE,
                MovieContract.MovieEntry.COLUMN_OVERVIEW,
                MovieContract.MovieEntry.COLUMN_DATE,
                MovieContract.MovieEntry.COLUMN_POSTER_PATH,
                MovieContract.MovieEntry.COLUMN_VOTE,
                MovieContract.MovieEntry.COLUMN_POPULARITY,
                MovieContract.MovieEntry.COLUMN_RATING,
                MovieContract.MovieEntry.COLUMN_FAVORITE
        };
        private static final String[] REVIEW_COLUMNS = {
                MovieContract.ReviewEntry.TABLE_NAME + "." + MovieContract.ReviewEntry._ID,
                MovieContract.ReviewEntry.COLUMN_MOVIE_ID,
                MovieContract.ReviewEntry.COLUMN_REVIEW_ID,
                MovieContract.ReviewEntry.COLUMN_AUTHOR,
                MovieContract.ReviewEntry.COLUMN_CONTENT,
                MovieContract.ReviewEntry.COLUMN_URL
        };
        private static final String[] TRAILER_COLUMNS = {
                MovieContract.TrailerEntry.TABLE_NAME + "." + MovieContract.TrailerEntry._ID,
                MovieContract.TrailerEntry.COLUMN_MOVIE_ID,
                MovieContract.TrailerEntry.COLUMN_TRAILER_ID,
                MovieContract.TrailerEntry.COLUMN_KEY,
                MovieContract.TrailerEntry.COLUMN_NAME,
                MovieContract.TrailerEntry.COLUMN_SITE,
                MovieContract.TrailerEntry.COLUMN_TYPE
        };
        private TextView mTitle;
        private TextView mOverview;
        private TextView mDate;
        private TextView mRating;
        private ImageView mPoster;
        private ExpandedListView mReviewView;
        private ExpandedListView mTrailerView;
        private TextView mReviewLabel;
        private TextView mTrailerLabel;

        private Uri mMovieUri;
        private Uri mReviewUri;
        private Uri mTrailerUri;

        private ReviewAdapter mReviewAdapter;
        private TrailerAdapter mTrailerAdapter;

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            mReviewAdapter = new ReviewAdapter(getActivity(), null, 0);
            mTrailerAdapter = new TrailerAdapter(getActivity(), null, 0);

            // get the view first and then do things with it;
            View rootView = inflater.inflate(R.layout.fragment_detail, container, false);

            mTitle = (TextView) rootView.findViewById(R.id.title);
            mOverview = (TextView) rootView.findViewById(R.id.overview);
            mDate = (TextView) rootView.findViewById(R.id.date);
            mRating = (TextView) rootView.findViewById(R.id.rating);
            mPoster = (ImageView) rootView.findViewById(R.id.poster);

            mReviewView = (ExpandedListView) rootView.findViewById(R.id.reviewListView);
            mTrailerView = (ExpandedListView) rootView.findViewById(R.id.trailerListView);

            mReviewLabel = (TextView) rootView.findViewById(R.id.reviewLabel);
            mTrailerLabel = (TextView) rootView.findViewById(R.id.trailerLabel);

            mReviewView.setAdapter(mReviewAdapter);
            mTrailerView.setAdapter(mTrailerAdapter);

            return rootView;
        }

        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
            getLoaderManager().initLoader(DETAIL_LOADER, null, this);
            getLoaderManager().initLoader(TRAILER_LOADER, null, this);
            getLoaderManager().initLoader(REVIEW_LOADER, null, this);
        }

        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            Intent intent = getActivity().getIntent();
            if (intent == null) {
                return null;
            }

            mMovieUri = intent.getData();
            mReviewUri = MovieContract.ReviewEntry.buildUriByMovieId(ContentUris.parseId(mMovieUri));
            mTrailerUri = MovieContract.TrailerEntry.buildUriByMovieId(ContentUris.parseId(mMovieUri));

            switch (id) {
                case DETAIL_LOADER:
                    return new CursorLoader(
                            getActivity(),
                            mMovieUri,
                            MOVIE_COLUMNS,
                            null,
                            null,
                            null
                    );
                case REVIEW_LOADER:
                    return new CursorLoader(
                            getActivity(),
                            mReviewUri,
                            REVIEW_COLUMNS,
                            null,
                            null,
                            null
                    );
                case TRAILER_LOADER:
                    return new CursorLoader(
                            getActivity(),
                            mTrailerUri,
                            TRAILER_COLUMNS,
                            null,
                            null,
                            null
                    );
                default:
                    throw new UnsupportedOperationException("Unknown loader:" + id);
            }
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
            if (!data.moveToFirst()) {
                return;
            }

            switch (loader.getId()) {
                case DETAIL_LOADER:
                    Long movieId = data.getLong(COL_MOVIE_ID);
                    String movieTitle = data.getString(COL_MOVIE_TITLE);
                    String movieOverview = data.getString(COL_MOVIE_OVERVIEW);
                    String movieDate = data.getString(COL_MOVIE_DATE);
                    String moviePosterPath = data.getString(COL_MOVIE_POSTER_PATH);
                    double movieVote = data.getDouble(COL_MOVIE_VOTE);
                    String voteDisplay = movieVote + "/10";

//                    Log.e(LOG_TAG, String.valueOf(movieId));

                    mTitle.setText(movieTitle);
                    mOverview.setText(movieOverview);
                    mDate.setText(movieDate);
                    mRating.setText(voteDisplay);

                    Picasso.with(getActivity())
                            .load(moviePosterPath)
                            .placeholder(R.drawable.placeholder)
                            .error(R.drawable.placeholder)
                            .fit()
                            .centerInside()
                            .into(mPoster);
                    break;
                case REVIEW_LOADER:
                    mReviewAdapter.swapCursor(data);
                    break;
                case TRAILER_LOADER:
                    mTrailerAdapter.swapCursor(data);
                    break;
                default:
                    throw new UnsupportedOperationException("Unknown loader:" + loader.getId());
            }

            if (mReviewAdapter.getCount() == 0) {
                mReviewLabel.setVisibility(View.INVISIBLE);
            } else {
                mReviewLabel.setVisibility(View.VISIBLE);
            }
            if (mTrailerAdapter.getCount() == 0) {
                mTrailerLabel.setVisibility(View.INVISIBLE);
            } else {
                mTrailerLabel.setVisibility(View.VISIBLE);
            }
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {
            mReviewAdapter.swapCursor(null);
            mTrailerAdapter.swapCursor(null);
        }
    }
}
