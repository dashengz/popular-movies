package com.dashengz.popularmovies.asynctasks;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import com.dashengz.popularmovies.BuildConfig;
import com.dashengz.popularmovies.data.MovieContract.ReviewEntry;

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

/**
 * Created by Jonathan on 11/16/15.
 * AsyncTask for fetching review data from TMD
 */
public class FetchReviewTask extends AsyncTask<String, Void, Void> {

    // Error log name; Rather than a string, use this so that when refactoring no more errors;
    private final String LOG_TAG = FetchReviewTask.class.getSimpleName();

    private final Context mContext;
    private long mMovieId;
    private Uri mReviewUri;

    public FetchReviewTask(Context context, long movieId) {
        mContext = context;
        mMovieId = movieId;
        mReviewUri = ReviewEntry.buildUriByMovieId(movieId);
    }

    private void getReviewDataFromJson(String reviewJsonStr)
            throws JSONException {

        // These are the names of the JSON objects that need to be extracted.
        final String TMD_RESULTS = "results";
        final String TMD_MOVIE_ID = "id";
        final String TMD_REVIEW_ID = "id";
        final String TMD_AUTHOR = "author";
        final String TMD_CONTENT = "content";
        final String TMD_URL = "url";

        try {
            JSONObject reviewJson = new JSONObject(reviewJsonStr);
            long movieId = reviewJson.getLong(TMD_MOVIE_ID);
            JSONArray reviewArray = reviewJson.getJSONArray(TMD_RESULTS);

            Vector<ContentValues> cVVector = new Vector<>(reviewArray.length());

            for (int i = 0; i < reviewArray.length(); i++) {

                // Get the JSON object representing the review item
                JSONObject reviewObject = reviewArray.getJSONObject(i);

                String reviewId = reviewObject.getString(TMD_REVIEW_ID);
                String author = reviewObject.getString(TMD_AUTHOR);
                String content = reviewObject.getString(TMD_CONTENT);
                String url = reviewObject.getString(TMD_URL);

                ContentValues reviewValues = new ContentValues();
                reviewValues.put(ReviewEntry.COLUMN_MOVIE_ID, movieId);
                reviewValues.put(ReviewEntry.COLUMN_REVIEW_ID, reviewId);
                reviewValues.put(ReviewEntry.COLUMN_AUTHOR, author);
                reviewValues.put(ReviewEntry.COLUMN_CONTENT, content);
                reviewValues.put(ReviewEntry.COLUMN_URL, url);

                cVVector.add(reviewValues);
            }

            for (ContentValues review : cVVector) {
                Uri insertUri = mContext.getContentResolver().insert(mReviewUri, review);
                if (insertUri == null) {
                    mContext.getContentResolver().update(
                            mReviewUri,
                            review,
                            ReviewEntry.COLUMN_REVIEW_ID + "=?",
                            new String[]{review.getAsString(ReviewEntry.COLUMN_REVIEW_ID)});
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
        mContext.getContentResolver().delete(mReviewUri, null, null);

        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        // Will contain the raw JSON response as a string.
        String reviewJsonStr = null;

        try {
            // Construct the URL for the query

            // build a URL with the params
            Uri builtUri;
            final String MOVIE_BASE_URL =
                    "https://api.themoviedb.org/3/movie";
            final String MOVIE_ID = Long.toString(mMovieId);
            final String REVIEW_PATH = "reviews";
            final String KEY_PARAM = "api_key";

            builtUri = Uri.parse(MOVIE_BASE_URL).buildUpon()
                    .appendPath(MOVIE_ID)
                    .appendPath(REVIEW_PATH)
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
            reviewJsonStr = buffer.toString();
            getReviewDataFromJson(reviewJsonStr);
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