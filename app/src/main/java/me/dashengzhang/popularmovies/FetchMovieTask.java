package me.dashengzhang.popularmovies;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Vector;

import me.dashengzhang.popularmovies.data.MovieContract.MovieEntry;

/**
 * Created by Jonathan on 11/16/15.
 */
public class FetchMovieTask extends AsyncTask<String, Void, Void> {

    // Error log name; Rather than a string, use this so that when refactoring no more errors;
    private final String LOG_TAG = FetchMovieTask.class.getSimpleName();

    private final Context mContext;
    private String mSortBy;
    private String mVoteCount;
//    private boolean DEBUG = true;

    public FetchMovieTask(Context context, String sortBy, String voteCount) {
        mContext = context;
        mSortBy = sortBy;
        mVoteCount = voteCount;
    }

    private void getMovieDataFromJson(String movieJsonStr)
            throws JSONException {

        // These are the names of the JSON objects that need to be extracted.
        final String TMD_RESULTS = "results";
        final String TMD_ID = "id";
        final String TMD_ORIGINAL_TITLE = "original_title";
        final String TMD_OVERVIEW = "overview";
        final String TMD_RELEASE_DATE = "release_date";
        final String TMD_POSTER_PATH = "poster_path";
        final String TMD_VOTE_AVERAGE = "vote_average";
        final String TMD_POPULARITY = "popularity";

        try {
            JSONObject movieJson = new JSONObject(movieJsonStr);
            JSONArray movieArray = movieJson.getJSONArray(TMD_RESULTS);

            Vector<ContentValues> cVVector = new Vector<>(movieArray.length());

            for (int i = 0; i < movieArray.length(); i++) {

                // Get the JSON object representing the movie item
                JSONObject movieObject = movieArray.getJSONObject(i);

                long id = movieObject.getLong(TMD_ID);
                String title = movieObject.getString(TMD_ORIGINAL_TITLE);
                String overview = movieObject.getString(TMD_OVERVIEW);
                String date = movieObject.getString(TMD_RELEASE_DATE);
                // poster path
                String path = mContext.getResources().getString(R.string.img_url_base)
                        + mContext.getResources().getString(R.string.img_size)
                        + movieObject.getString(TMD_POSTER_PATH);
                double vote = movieObject.getDouble(TMD_VOTE_AVERAGE);

                ContentValues movieValues = new ContentValues();
                movieValues.put(MovieEntry._ID, id);
                movieValues.put(MovieEntry.COLUMN_TITLE, title);
                movieValues.put(MovieEntry.COLUMN_OVERVIEW, overview);
                movieValues.put(MovieEntry.COLUMN_DATE, date);
                movieValues.put(MovieEntry.COLUMN_POSTER_PATH, path);
                movieValues.put(MovieEntry.COLUMN_VOTE, vote);

                if (mSortBy.equals(mContext.getString(R.string.pref_sorting_popularity))) {
                    // save the popularity of most popular movies into database
                    double popularity = movieObject.getDouble(TMD_POPULARITY);
                    movieValues.put(MovieEntry.COLUMN_POPULARITY, popularity);
                } else if (mSortBy.equals(mContext.getString(R.string.pref_sorting_rating))) {
                    // save the rating index of highest rating movies into database
                    // int i is the index
                    movieValues.put(MovieEntry.COLUMN_RATING, i);
                }

                cVVector.add(movieValues);
            }

            int inserted = 0;
            // add to database
            if (cVVector.size() > 0) {
                ContentValues[] cvArray = new ContentValues[cVVector.size()];
                cVVector.toArray(cvArray);
                inserted = mContext.getContentResolver().bulkInsert(MovieEntry.CONTENT_URI, cvArray);
            }
            Log.d(LOG_TAG, "FetchMovieTask Complete. " + inserted + " Inserted");
        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }
    }

    // referencing the Udacity Sunshine course code:
    @Override
    protected Void doInBackground(String... params) {

        // need to clean up the database before each fetch
        // because the movies (popularity and rating) could have been updated
        if (mSortBy.equals(mContext.getString(R.string.pref_sorting_popularity))) {
            // clean up most popular movies
            // delete movies that are no longer popular
            // but also make sure they are not also favorite or highest rated
            // then update all the movie entries and make their popularity null (cleaning up)
            ContentResolver contentResolver = mContext.getContentResolver();
            String selection = MovieEntry.COLUMN_RATING + " IS NULL AND " + MovieEntry.COLUMN_FAVORITE + " IS NULL";
            contentResolver.delete(MovieEntry.CONTENT_URI, selection, null);
            ContentValues contentValues = new ContentValues();
            contentValues.putNull(MovieEntry.COLUMN_POPULARITY);
            contentResolver.update(MovieEntry.CONTENT_URI, contentValues, null, null);
        } else if (mSortBy.equals(mContext.getString(R.string.pref_sorting_rating))) {
            // do the same with highest rated movies
            ContentResolver contentResolver = mContext.getContentResolver();
            String selection = MovieEntry.COLUMN_POPULARITY + " IS NULL AND " + MovieEntry.COLUMN_FAVORITE + " IS NULL";
            contentResolver.delete(MovieEntry.CONTENT_URI, selection, null);
            ContentValues contentValues = new ContentValues();
            contentValues.putNull(MovieEntry.COLUMN_RATING);
            contentResolver.update(MovieEntry.CONTENT_URI, contentValues, null, null);
        }

        if (params.length == 0) {
            return null;
        }

        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        // Will contain the raw JSON response as a string.
        String movieJsonStr = null;

        // Data is fetched based on popularity by default.
        // If user prefers to sort by vote_average, change from here.

        try {
            // Construct the URL for the query

            // build a URL with the params
            Uri builtUri;
            final String MOVIE_BASE_URL =
                    "https://api.themoviedb.org/3/discover/movie?";
            final String SORTING_PARAM = "sort_by";
            final String KEY_PARAM = "api_key";
            final String COUNT_PARAM = "vote_count.gte";

//            Log.e(LOG_TAG, mSortBy);

            if (mSortBy.equals(mContext.getString(R.string.pref_sorting_popularity))) {
                builtUri = Uri.parse(MOVIE_BASE_URL).buildUpon()
                        .appendQueryParameter(SORTING_PARAM, params[0])
                        .appendQueryParameter(KEY_PARAM, BuildConfig.THE_MOVIE_DATABASE_API_KEY)
                        .build();
            } else if (mSortBy.equals(mContext.getString(R.string.pref_sorting_rating))) {
                builtUri = Uri.parse(MOVIE_BASE_URL).buildUpon()
                        .appendQueryParameter(SORTING_PARAM, params[0])
                        .appendQueryParameter(COUNT_PARAM, mVoteCount)
                        .appendQueryParameter(KEY_PARAM, BuildConfig.THE_MOVIE_DATABASE_API_KEY)
                        .build();
            } else {
                // favorite no need to fetch
                return null;
            }

//            Log.e(LOG_TAG, builtUri.toString());

            URL url = new URL(builtUri.toString());

            // Create the request to TMD, and open the connection
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // Read the input stream into a String
            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            if (inputStream == null) {
                return null;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {

                buffer.append(line + "\n");
            }

            if (buffer.length() == 0) {
                return null;
            }
            movieJsonStr = buffer.toString();

        } catch (IOException e) {
            Log.e(LOG_TAG, "Error ", e);
            return null;
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    Log.e(LOG_TAG, "Error closing stream", e);
                }
            }
        }

        // try and catch errors;
        try {
            getMovieDataFromJson(movieJsonStr);
            Log.e(LOG_TAG, "Getting data...");
        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }
        return null;
    }
}
