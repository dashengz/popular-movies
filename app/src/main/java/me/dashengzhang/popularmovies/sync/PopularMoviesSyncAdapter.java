package me.dashengzhang.popularmovies.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SyncRequest;
import android.content.SyncResult;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
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
import me.dashengzhang.popularmovies.R;
import me.dashengzhang.popularmovies.Utility;
import me.dashengzhang.popularmovies.activities.MainActivity;
import me.dashengzhang.popularmovies.data.MovieContract.MovieEntry;

/**
 * Movie Sync Adapter
 */
public class PopularMoviesSyncAdapter extends AbstractThreadedSyncAdapter {
    // Interval at which to sync with the api, in seconds.
    // 60 seconds (1 minute) * 180 = 3 hours
    public static final int SYNC_INTERVAL = 60 * 180;
    public static final int SYNC_FLEXTIME = SYNC_INTERVAL / 3;
    private static final long DAY_IN_MILLIS = 1000 * 60 * 60 * 24;
    private static final int MOVIE_NOTIFICATION_ID = 1103;
    private static final String[] NOTIFY_MOVIE_PROJECTION = new String[]{
            MovieEntry.COLUMN_TITLE,
            MovieEntry.COLUMN_VOTE,
    };
    // these indices must match the projection
    private static final int INDEX_TITLE = 0;
    private static final int INDEX_VOTE = 1;
    public final String LOG_TAG = PopularMoviesSyncAdapter.class.getSimpleName();
    String mSortBy;
    String mVoteCount;

    public PopularMoviesSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
    }

    /**
     * Helper method to schedule the sync adapter periodic execution
     */
    public static void configurePeriodicSync(Context context, int syncInterval, int flexTime) {
        Account account = getSyncAccount(context);
        String authority = context.getString(R.string.content_authority);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // we can enable inexact timers in our periodic sync
            SyncRequest request = new SyncRequest.Builder().
                    syncPeriodic(syncInterval, flexTime).
                    setSyncAdapter(account, authority).
                    setExtras(new Bundle()).build();
            ContentResolver.requestSync(request);
        } else {
            ContentResolver.addPeriodicSync(account,
                    authority, new Bundle(), syncInterval);
        }
    }

    /**
     * Helper method to have the sync adapter sync immediately
     *
     * @param context The context used to access the account service
     */
    public static void syncImmediately(Context context) {
        Bundle bundle = new Bundle();
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        ContentResolver.requestSync(getSyncAccount(context),
                context.getString(R.string.content_authority), bundle);
    }

    /**
     * Helper method to get the fake account to be used with SyncAdapter, or make a new one
     * if the fake account doesn't exist yet.  If we make a new account, we call the
     * onAccountCreated method so we can initialize things.
     *
     * @param context The context used to access the account service
     * @return a fake account.
     */
    public static Account getSyncAccount(Context context) {
        // Get an instance of the Android account manager
        AccountManager accountManager =
                (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);

        // Create the account type and default account
        Account newAccount = new Account(
                context.getString(R.string.app_name), context.getString(R.string.sync_account_type));

        // If the password doesn't exist, the account doesn't exist
        if (null == accountManager.getPassword(newAccount)) {

        /*
         * Add the account and account type, no password or user data
         * If successful, return the Account object, otherwise report an error.
         */
            if (!accountManager.addAccountExplicitly(newAccount, "", null)) {
                return null;
            }
            /*
             * If you don't set android:syncable="true" in
             * in your <provider> element in the manifest,
             * then call ContentResolver.setIsSyncable(account, AUTHORITY, 1)
             * here.
             */
            onAccountCreated(newAccount, context);
        }
        return newAccount;
    }

    private static void onAccountCreated(Account newAccount, Context context) {
        /*
         * Since we've created an account
         */
        PopularMoviesSyncAdapter.configurePeriodicSync(context, SYNC_INTERVAL, SYNC_FLEXTIME);

        /*
         * Without calling setSyncAutomatically, our periodic sync will not be enabled.
         */
        ContentResolver.setSyncAutomatically(newAccount, context.getString(R.string.content_authority), true);

        /*
         * Finally, let's do a sync to get things started
         */
        syncImmediately(context);
    }

    public static void initializeSyncAdapter(Context context) {
        getSyncAccount(context);
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {
        Log.d(LOG_TAG, "Starting sync");
        mSortBy = Utility.getPreferredSorting(getContext());
        mVoteCount = Utility.getPreferredVote(getContext());

        clearDatabaseCache();

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

            if (mSortBy.equals(getContext().getString(R.string.pref_sorting_popularity))) {
                builtUri = Uri.parse(MOVIE_BASE_URL).buildUpon()
                        .appendQueryParameter(SORTING_PARAM, mSortBy)
                        .appendQueryParameter(KEY_PARAM, BuildConfig.THE_MOVIE_DATABASE_API_KEY)
                        .build();
            } else if (mSortBy.equals(getContext().getString(R.string.pref_sorting_rating))) {
                builtUri = Uri.parse(MOVIE_BASE_URL).buildUpon()
                        .appendQueryParameter(SORTING_PARAM, mSortBy)
                        .appendQueryParameter(COUNT_PARAM, mVoteCount)
                        .appendQueryParameter(KEY_PARAM, BuildConfig.THE_MOVIE_DATABASE_API_KEY)
                        .build();
            } else {
                // favorite no need to fetch
                return;
            }

            URL url = new URL(builtUri.toString());

            // Create the request to TMD, and open the connection
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // Read the input stream into a String
            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            if (inputStream == null) return;
            reader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while ((line = reader.readLine()) != null)
                buffer.append(line).append("\n");
            if (buffer.length() == 0) return;

            movieJsonStr = buffer.toString();
            getMovieDataFromJson(movieJsonStr);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error ", e);
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
    }

    private void getMovieDataFromJson(String movieJsonStr) throws JSONException {
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
                String path = getContext().getResources().getString(R.string.img_url_base)
                        + getContext().getResources().getString(R.string.img_size)
                        + movieObject.getString(TMD_POSTER_PATH);
                double vote = movieObject.getDouble(TMD_VOTE_AVERAGE);

                ContentValues movieValues = new ContentValues();
                movieValues.put(MovieEntry._ID, id);
                movieValues.put(MovieEntry.COLUMN_TITLE, title);
                movieValues.put(MovieEntry.COLUMN_OVERVIEW, overview);
                movieValues.put(MovieEntry.COLUMN_DATE, date);
                movieValues.put(MovieEntry.COLUMN_POSTER_PATH, path);
                movieValues.put(MovieEntry.COLUMN_VOTE, vote);

                if (mSortBy.equals(getContext().getString(R.string.pref_sorting_popularity))) {
                    // save the popularity of most popular movies into database
                    double popularity = movieObject.getDouble(TMD_POPULARITY);
                    movieValues.put(MovieEntry.COLUMN_POPULARITY, popularity);
                } else if (mSortBy.equals(getContext().getString(R.string.pref_sorting_rating))) {
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
                inserted = getContext().getContentResolver().bulkInsert(MovieEntry.CONTENT_URI, cvArray);

                notifyMovie();
            }
            Log.d(LOG_TAG, "PopularMovies Sync Complete. " + inserted + " Inserted");
        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }
    }

    private void notifyMovie() {
        Context context = getContext();
        //checking the last update and notify if it' the first of the day
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String displayNotificationsKey = context.getString(R.string.pref_enable_notifications_key);
        boolean displayNotifications = prefs.getBoolean(displayNotificationsKey,
                Boolean.parseBoolean(context.getString(R.string.pref_enable_notifications_default)));

        if (displayNotifications) {
            String lastNotificationKey = context.getString(R.string.pref_last_notification);
            long lastSync = prefs.getLong(lastNotificationKey, 0);

            if (System.currentTimeMillis() - lastSync >= DAY_IN_MILLIS) {
                // Last sync was more than 1 day ago, let's send a notification with the latest hot movie.
                Uri movieUri = MovieEntry.CONTENT_URI;

                // we'll query our contentProvider, as always
                Cursor cursor = context.getContentResolver().query(movieUri, NOTIFY_MOVIE_PROJECTION,
                        null, null, MovieEntry.COLUMN_POPULARITY + " ASC");

                if (cursor.moveToFirst()) {
                    String movieTitle = cursor.getString(INDEX_TITLE);
                    double movieVote = cursor.getDouble(INDEX_VOTE);

                    String title = context.getString(R.string.app_name);

                    // Define the text of the forecast.
                    String contentText = String.format(context.getString(R.string.format_notification),
                            movieTitle, movieVote + "/10");

                    //build your notification here.

                    // NotificationCompatBuilder is a very convenient way to build backward-compatible
                    // notifications.  Just throw in some data.
                    NotificationCompat.Builder mBuilder =
                            new NotificationCompat.Builder(getContext())
                                    .setSmallIcon(R.mipmap.ic_launcher)
                                    .setLargeIcon(BitmapFactory.decodeResource(getContext().getResources(),
                                            R.mipmap.ic_launcher))
                                    .setContentTitle(title)
                                    .setContentText(contentText);

                    // Make something interesting happen when the user clicks on the notification.
                    // In this case, opening the app is sufficient.
                    Intent resultIntent = new Intent(context, MainActivity.class);

                    // The stack builder object will contain an artificial back stack for the
                    // started Activity.
                    // This ensures that navigating backward from the Activity leads out of
                    // your application to the Home screen.
                    TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
                    stackBuilder.addNextIntent(resultIntent);
                    PendingIntent resultPendingIntent =
                            stackBuilder.getPendingIntent(
                                    0,
                                    PendingIntent.FLAG_UPDATE_CURRENT
                            );
                    mBuilder.setContentIntent(resultPendingIntent);

                    NotificationManager mNotificationManager =
                            (NotificationManager) getContext().getSystemService(Context.NOTIFICATION_SERVICE);
                    // MOVIE_NOTIFICATION_ID allows you to update the notification later on.
                    mNotificationManager.notify(MOVIE_NOTIFICATION_ID, mBuilder.build());

                    //refreshing last sync
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putLong(lastNotificationKey, System.currentTimeMillis());
                    editor.commit();
                }
                cursor.close();
            }
        }
    }

    /**
     * Helper method for clearing database cache
     */
    private void clearDatabaseCache() {
        // need to clean up the database before each fetch
        // because the movies (popularity and rating) could have been updated
        if (mSortBy.equals(getContext().getString(R.string.pref_sorting_popularity))) {
            // clean up most popular movies
            // delete movies that are no longer popular
            // but also make sure they are not also favorite or highest rated
            // then update all the movie entries and make their popularity null (cleaning up)
            ContentResolver contentResolver = getContext().getContentResolver();
            String selection = MovieEntry.COLUMN_RATING + " IS NULL AND " + MovieEntry.COLUMN_FAVORITE + " IS NULL";
            contentResolver.delete(MovieEntry.CONTENT_URI, selection, null);
            ContentValues contentValues = new ContentValues();
            contentValues.putNull(MovieEntry.COLUMN_POPULARITY);
            contentResolver.update(MovieEntry.CONTENT_URI, contentValues, null, null);
        } else if (mSortBy.equals(getContext().getString(R.string.pref_sorting_rating))) {
            // do the same with highest rated movies
            ContentResolver contentResolver = getContext().getContentResolver();
            String selection = MovieEntry.COLUMN_POPULARITY + " IS NULL AND " + MovieEntry.COLUMN_FAVORITE + " IS NULL";
            contentResolver.delete(MovieEntry.CONTENT_URI, selection, null);
            ContentValues contentValues = new ContentValues();
            contentValues.putNull(MovieEntry.COLUMN_RATING);
            contentResolver.update(MovieEntry.CONTENT_URI, contentValues, null, null);
        }
    }
}
