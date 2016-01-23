package me.dashengzhang.popularmovies;

import android.content.Intent;
import android.database.Cursor;
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
import android.widget.LinearLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

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
        private static final String LOG_TAG = DetailFragment.class.getSimpleName();
        private static final int DETAIL_LOADER = 0;
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
        private long movieId;
        private String movieTitle;
        private String movieOverview;
        private String movieDate;
        private String moviePosterPath;
        private double movieVote;

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            // get the view first and then do things with it;
            View rootView = inflater.inflate(R.layout.fragment_detail, container, false);

            // Review and Trailer Linear Layouts
            LinearLayout trailerView = (LinearLayout) rootView.findViewById(R.id.trailerLinearLayout);

            ArrayList<Trailer> trailers = new ArrayList<>();
            trailers.add(new Trailer(1, "a", "Trailer 1", "key1", "Youtube"));
            trailers.add(new Trailer(2, "b", "Trailer 2", "key2", "Youtube"));
            trailers.add(new Trailer(3, "c", "Trailer 3", "key3", "Youtube"));

            // Inflaters
            for (Trailer trailer : trailers) {
                View eachTrailer = inflater.inflate(R.layout.list_item_trailers, trailerView, false);
                ((TextView) eachTrailer.findViewById(R.id.trailerNameField)).setText(trailer.getName());
                trailerView.addView(eachTrailer);
            }

            LinearLayout reviewView = (LinearLayout) rootView.findViewById(R.id.reviewLinearLayout);
            // need to use substrings() to make the review shorter (when displayed here)
            // and also store the original one and display later in another activity.
            ArrayList<Review> reviews = new ArrayList<>();
            reviews.add(new Review(1, "a", "Author 1", "Review 1"));
            reviews.add(new Review(2, "b", "Author 2", "Review 2"));
            reviews.add(new Review(3, "c", "Author 3", "Review 3"));

            // Inflaters
            for (Review review : reviews) {
                View eachReview = inflater.inflate(R.layout.list_item_reviews, reviewView, false);
                ((TextView) eachReview.findViewById(R.id.reviewAuthorField)).setText(review.getAuthor());
                ((TextView) eachReview.findViewById(R.id.reviewContentField)).setText(review.getContent());
                trailerView.addView(eachReview);
            }

            return rootView;
        }

        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            getLoaderManager().initLoader(DETAIL_LOADER, null, this);
            super.onActivityCreated(savedInstanceState);
        }

        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            Intent intent = getActivity().getIntent();
            if (intent == null) {
                return null;
            }

            // Now create and return a CursorLoader that will take care of
            // creating a Cursor for the data being displayed.
            return new CursorLoader(
                    getActivity(),
                    intent.getData(),
                    MOVIE_COLUMNS,
                    null,
                    null,
                    null
            );
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
            if (!data.moveToFirst()) {
                return;
            }

            movieId = data.getLong(COL_MOVIE_ID);
            movieTitle = data.getString(COL_MOVIE_TITLE);
            movieOverview = data.getString(COL_MOVIE_OVERVIEW);
            movieDate = data.getString(COL_MOVIE_DATE);
            moviePosterPath = data.getString(COL_MOVIE_POSTER_PATH);
            movieVote = data.getDouble(COL_MOVIE_VOTE);
            String voteDisplay = movieVote + "/10";

//            Log.e(LOG_TAG, String.valueOf(movieId));

            ((TextView) getView().findViewById(R.id.title)).setText(movieTitle);
            ((TextView) getView().findViewById(R.id.overview)).setText(movieOverview);
            ((TextView) getView().findViewById(R.id.date)).setText(movieDate);
            ((TextView) getView().findViewById(R.id.rating)).setText(voteDisplay);

            Picasso.with(getActivity())
                    .load(moviePosterPath)
                    .placeholder(R.drawable.placeholder)
                    .error(R.drawable.placeholder)
                    .fit()
                    .centerInside()
                    .into((ImageView) getView().findViewById(R.id.poster));
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {
            // nothing yet
        }
    }
}
