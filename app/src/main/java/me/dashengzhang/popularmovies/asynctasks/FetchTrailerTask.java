package me.dashengzhang.popularmovies.asynctasks;

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

import me.dashengzhang.popularmovies.BuildConfig;
import me.dashengzhang.popularmovies.data.MovieContract.TrailerEntry;

/**
 * Created by Jonathan on 11/16/15.
 * AsyncTask for fetching trailer data from TMD
 */
public class FetchTrailerTask extends AsyncTask<String, Void, Void> {

    // Error log name; Rather than a string, use this so that when refactoring no more errors;
    private final String LOG_TAG = FetchTrailerTask.class.getSimpleName();

    private final Context mContext;
    private long mMovieId;
    private Uri mTrailerUri;

    public FetchTrailerTask(Context context, long movieId) {
        mContext = context;
        mMovieId = movieId;
        mTrailerUri = TrailerEntry.buildUriByMovieId(movieId);
    }

    private void getTrailerDataFromJson(String trailerJsonStr)
            throws JSONException {

        // These are the names of the JSON objects that need to be extracted.
        final String TMD_RESULTS = "results";
        final String TMD_MOVIE_ID = "id";
        final String TMD_TRAILER_ID = "id";
        final String TMD_KEY = "key";
        final String TMD_NAME = "name";
        final String TMD_SITE = "site";
        final String TMD_TYPE = "type";

        try {
            JSONObject trailerJson = new JSONObject(trailerJsonStr);
            long movieId = trailerJson.getLong(TMD_MOVIE_ID);
            JSONArray trailerArray = trailerJson.getJSONArray(TMD_RESULTS);

            Vector<ContentValues> cVVector = new Vector<>(trailerArray.length());

            for (int i = 0; i < trailerArray.length(); i++) {

                // Get the JSON object representing the trailer item
                JSONObject trailerObject = trailerArray.getJSONObject(i);

                String trailerId = trailerObject.getString(TMD_TRAILER_ID);
                String key = trailerObject.getString(TMD_KEY);
                String name = trailerObject.getString(TMD_NAME);
                String site = trailerObject.getString(TMD_SITE);
                String type = trailerObject.getString(TMD_TYPE);

                ContentValues trailerValues = new ContentValues();
                trailerValues.put(TrailerEntry.COLUMN_MOVIE_ID, movieId);
                trailerValues.put(TrailerEntry.COLUMN_TRAILER_ID, trailerId);
                trailerValues.put(TrailerEntry.COLUMN_KEY, key);
                trailerValues.put(TrailerEntry.COLUMN_NAME, name);
                trailerValues.put(TrailerEntry.COLUMN_SITE, site);
                trailerValues.put(TrailerEntry.COLUMN_TYPE, type);

                cVVector.add(trailerValues);
            }

            for (ContentValues trailer : cVVector) {
                Uri insertUri = mContext.getContentResolver().insert(mTrailerUri, trailer);
                if (insertUri == null) {
                    mContext.getContentResolver().update(
                            mTrailerUri,
                            trailer,
                            TrailerEntry.COLUMN_TRAILER_ID + "=?",
                            new String[]{trailer.getAsString(TrailerEntry.COLUMN_TRAILER_ID)});
                }
            }
        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }
    }

    // referencing the Udacity Sunshine course code:
    @Override
    protected Void doInBackground(String... params) {

        // need to clean up the database before each fetch
        mContext.getContentResolver().delete(mTrailerUri, null, null);

        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        // Will contain the raw JSON response as a string.
        String trailerJsonStr = null;

        try {
            // Construct the URL for the query

            // build a URL with the params
            Uri builtUri;
            final String MOVIE_BASE_URL =
                    "https://api.themoviedb.org/3/movie";
            final String MOVIE_ID = Long.toString(mMovieId);
            final String TRAILER_PATH = "videos";
            final String KEY_PARAM = "api_key";

            builtUri = Uri.parse(MOVIE_BASE_URL).buildUpon()
                    .appendPath(MOVIE_ID)
                    .appendPath(TRAILER_PATH)
                    .appendQueryParameter(KEY_PARAM, BuildConfig.THE_MOVIE_DATABASE_API_KEY)
                    .build();


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
            trailerJsonStr = buffer.toString();
            getTrailerDataFromJson(trailerJsonStr);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error ", e);
            return null;
        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
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
        return null;
    }
}