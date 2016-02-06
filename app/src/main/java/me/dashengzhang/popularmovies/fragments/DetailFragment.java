package me.dashengzhang.popularmovies.fragments;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.util.Calendar;

import me.dashengzhang.popularmovies.ExpandedListView;
import me.dashengzhang.popularmovies.R;
import me.dashengzhang.popularmovies.Utility;
import me.dashengzhang.popularmovies.adapters.ReviewAdapter;
import me.dashengzhang.popularmovies.adapters.TrailerAdapter;
import me.dashengzhang.popularmovies.data.MovieContract;

/**
 * Created by Jonathan on 1/25/16.
 * Fragment of DetailActivity to show movie detail, reviews and trailers
 * Include Sharing Intent and Opens YouTube app when click on trailers
 */
public class DetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    public static final int COL_AUTHOR = 3;
    public static final int COL_CONTENT = 4;
    public static final int COL_NAME = 4;
    public static final String DETAIL_URI = "D_URI";
    public static final String DETAIL_REVIEW_URI = "R_URI";
    public static final String DETAIL_TRAILER_URI = "T_URI";
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
    static final int COL_URL = 5;
    static final int COL_TRAILER_MOVIE_ID = 1;
    static final int COL_TRAILER_ID = 2;
    static final int COL_KEY = 3;
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
    TextView mEmptyRView;
    TextView mEmptyTView;
    private TextView mTitle;
    private TextView mOverview;
    private TextView mDate;
    private TextView mRating;
    private ImageView mPoster;
    private ExpandedListView mReviewView;
    private ExpandedListView mTrailerView;
    private Uri mMovieUri;
    private Uri mReviewUri;
    private Uri mTrailerUri;
    private ReviewAdapter mReviewAdapter;
    private TrailerAdapter mTrailerAdapter;
    private ShareActionProvider mShareActionProvider;
    private String mShare;
    private ScrollView mScrollView;

    private Button mFavBtn;


    public DetailFragment() {
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Bundle arguments = getArguments();
        if (arguments != null) {
            mMovieUri = arguments.getParcelable(DetailFragment.DETAIL_URI);
            mReviewUri = arguments.getParcelable(DetailFragment.DETAIL_REVIEW_URI);
            mTrailerUri = arguments.getParcelable(DetailFragment.DETAIL_TRAILER_URI);
        }

        mReviewAdapter = new ReviewAdapter(getActivity(), null, 0);
        mTrailerAdapter = new TrailerAdapter(getActivity(), null, 0);

        // get the view first and then do things with it;
        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);

        mScrollView = (ScrollView) rootView.findViewById(R.id.scrollView);
        mTitle = (TextView) rootView.findViewById(R.id.title);
        mOverview = (TextView) rootView.findViewById(R.id.overview);
        mDate = (TextView) rootView.findViewById(R.id.date);
        mRating = (TextView) rootView.findViewById(R.id.rating);
        mPoster = (ImageView) rootView.findViewById(R.id.poster);
        mFavBtn = (Button) rootView.findViewById(R.id.favBtn);

        mReviewView = (ExpandedListView) rootView.findViewById(R.id.reviewListView);
        mEmptyRView = (TextView) rootView.findViewById(R.id.listView_reviews_empty);
        mReviewView.setEmptyView(mEmptyRView);
        mTrailerView = (ExpandedListView) rootView.findViewById(R.id.trailerListView);
        mEmptyTView = (TextView) rootView.findViewById(R.id.listView_trailers_empty);
        mTrailerView.setEmptyView(mEmptyTView);

        mReviewView.setAdapter(mReviewAdapter);
        mTrailerView.setAdapter(mTrailerAdapter);
        mTrailerView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Cursor cursor = (Cursor) parent.getItemAtPosition(position);
                if (cursor != null && cursor.getString(COL_SITE).equalsIgnoreCase("youtube")) {
                    startActivity(new Intent(Intent.ACTION_VIEW,
                            Uri.parse(getActivity().getResources().getString(R.string.youtube_url_base)
                                    + cursor.getString(COL_KEY))));
                } else {
                    Toast.makeText(getActivity(), getActivity().getResources().getString(R.string.toast_unrecognizable_trailer), Toast.LENGTH_SHORT).show();
                }
            }
        });

        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_detail_fragment, menu);
        MenuItem menuItem = menu.findItem(R.id.action_share);
        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);
        // If onLoadFinished happens before this, set the share intent
        if (mShare != null) {
            mShareActionProvider.setShareIntent(createShareIntent());
        }
    }

    // Inspired by Udacity course
    private Intent createShareIntent() {
        Cursor cursor = getActivity().getContentResolver().query(mTrailerUri, null, null, null, null);
        if (cursor.moveToFirst()) {
            mShare = getActivity().getResources().getString(R.string.share_trailer_1) +
                    mTitle.getText().toString() +
                    getActivity().getResources().getString(R.string.share_trailer_2) +
                    getActivity().getResources().getString(R.string.youtube_url_base) +
                    cursor.getString(COL_KEY);
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT, mShare + getActivity().getResources().getString(R.string.share_hashtag));
            return shareIntent;
        } else return null;
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
        if (mMovieUri != null && mReviewUri != null && mTrailerUri != null) {
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
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (!data.moveToFirst()) {
            return;
        }

        switch (loader.getId()) {
            case DETAIL_LOADER:
                final Long movieId = data.getLong(COL_MOVIE_ID);
                String movieTitle = data.getString(COL_MOVIE_TITLE);
                String movieOverview = data.getString(COL_MOVIE_OVERVIEW);
                String movieDate = data.getString(COL_MOVIE_DATE);
                String moviePosterPath = data.getString(COL_MOVIE_POSTER_PATH);
                double movieVote = data.getDouble(COL_MOVIE_VOTE);
                String voteDisplay = movieVote + "/10";

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

                boolean favorite = data.getInt(COL_MOVIE_FAVORITE) != 0;

                final String favText = getActivity().getResources().getString(R.string.favorite);
                final String removeFavText = getActivity().getResources().getString(R.string.remove_favorite);

                if (!favorite) {
                    // not fav yet
                    // add fav
                    mFavBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            mFavBtn.setText(removeFavText);

                            ContentValues movieValues = new ContentValues();
                            int time = (int) Calendar.getInstance().getTimeInMillis() / 1000;
                            movieValues.put(MovieContract.MovieEntry.COLUMN_FAVORITE, time);

                            getActivity().getContentResolver().update(
                                    MovieContract.MovieEntry.CONTENT_URI,
                                    movieValues,
                                    MovieContract.MovieEntry._ID + "=?",
                                    new String[]{Long.toString(movieId)});
                        }
                    });
                } else {
                    mFavBtn.setText(removeFavText);
                    // fav already
                    // remove fav
                    mFavBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            mFavBtn.setText(favText);

                            ContentValues movieValues = new ContentValues();
                            movieValues.putNull(MovieContract.MovieEntry.COLUMN_FAVORITE);

                            getActivity().getContentResolver().update(
                                    MovieContract.MovieEntry.CONTENT_URI,
                                    movieValues,
                                    MovieContract.MovieEntry._ID + "=?",
                                    new String[]{Long.toString(movieId)});
                        }
                    });
                }
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

        // if no data then no display
        mScrollView.setVisibility(View.VISIBLE);

        // If onCreateOptionsMenu has already happened, update the share intent
        if (mShareActionProvider != null) {
            mShareActionProvider.setShareIntent(createShareIntent());
        }

        Utility.updateEmptyView(getActivity(), mReviewAdapter, mEmptyRView,
                getActivity().getResources().getString(R.string.empty_review_list));
        Utility.updateEmptyView(getActivity(), mTrailerAdapter, mEmptyTView,
                getActivity().getResources().getString(R.string.empty_trailer_list));
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mReviewAdapter.swapCursor(null);
        mTrailerAdapter.swapCursor(null);
    }
}