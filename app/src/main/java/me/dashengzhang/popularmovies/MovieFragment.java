package me.dashengzhang.popularmovies;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

import me.dashengzhang.popularmovies.data.MovieContract;

public class MovieFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    static final int COL_MOVIE_ID = 0;
    static final int COL_MOVIE_TITLE = 1;
    static final int COL_MOVIE_OVERVIEW = 2;
    static final int COL_MOVIE_DATE = 3;
    static final int COL_MOVIE_POSTER_PATH = 4;
    static final int COL_MOVIE_VOTE = 5;
    static final int COL_MOVIE_POPULARITY = 6;
    static final int COL_MOVIE_RATING = 7;
    static final int COL_MOVIE_FAVORITE = 8;
    private static final int MOVIE_LOADER = 0;
    // For the forecast view we're showing only a small subset of the stored data.
    // Specify the columns we need.
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
    private MovieAdapter mMovieAdapter;

    public MovieFragment() {
        // nothing yet
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mMovieAdapter = new MovieAdapter(getActivity(), null, 0);

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        GridView gridView = (GridView) rootView.findViewById(R.id.gridView_movies);
        gridView.setAdapter(mMovieAdapter);

        // onClick to DetailActivity

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                // CursorAdapter returns a cursor at the correct position for getItem(), or null
                // if it cannot seek to that position.
                Cursor cursor = (Cursor) adapterView.getItemAtPosition(position);
                if (cursor != null) {
                    Uri onClickUri = MovieContract.MovieEntry.buildMovieUri(cursor.getLong(COL_MOVIE_ID));
                    Intent intent = new Intent(getActivity(), DetailActivity.class).setData(onClickUri);
                    startActivity(intent);
                }
            }
        });

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(MOVIE_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    void onPrefChanged() {
        String newSorting = Utility.getPreferredSorting(getActivity());
        String newVoteCount = Utility.getPreferredVote(getActivity());
        FetchMovieTask movieTask = new FetchMovieTask(getActivity(), newSorting, newVoteCount);
        movieTask.execute(newSorting);

        getLoaderManager().restartLoader(MOVIE_LOADER, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String sortOrder;
        String selection;
        String sorting = Utility.getPreferredSorting(getActivity());
        if (sorting == null) {
            Log.e("Error", "Error loading from database.");
            return null;
        }
        if (sorting.equals(getString(R.string.pref_sorting_rating))) {
            sortOrder = MovieContract.MovieEntry.COLUMN_RATING + " DESC";
            selection = MovieContract.MovieEntry.COLUMN_RATING + " IS NOT NULL";
        } else {
            sortOrder = MovieContract.MovieEntry.COLUMN_POPULARITY + " ASC";
            selection = MovieContract.MovieEntry.COLUMN_POPULARITY + " IS NOT NULL";
        }
        Uri movieUri = MovieContract.MovieEntry.CONTENT_URI;

        return new CursorLoader(getActivity(),
                movieUri,
                MOVIE_COLUMNS,
                selection,
                null,
                sortOrder);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mMovieAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mMovieAdapter.swapCursor(null);
    }
}
