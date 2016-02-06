package me.dashengzhang.popularmovies;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.widget.CursorAdapter;
import android.widget.TextView;

/**
 * Created by Jonathan on 11/16/15.
 * Utility class for getting preference strings
 */
public class Utility {
    public static String getPreferredSorting(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(context.getString(R.string.pref_sorting_key),
                context.getString(R.string.pref_sorting_popularity));
    }

    public static String getPreferredVote(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(context.getString(R.string.pref_vote_count_key),
                context.getString(R.string.pref_vote_count_default));
    }

    /**
     * Returns true if the network is available or about to become available.
     *
     * @param c Context used to get the ConnectivityManager
     * @return
     */
    public static boolean isNetworkAvailable(Context c) {
        ConnectivityManager cm =
                (ConnectivityManager) c.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
    }

    /*
        Updates the empty list view with contextually relevant information that the user can
        use to determine why they aren't seeing any movie.
     */
    public static void updateEmptyView(
            Context context, CursorAdapter cursorAdapter, TextView textView, String message) {
        if (cursorAdapter.getCount() == 0) {
            if (null != textView) {
                if (!isNetworkAvailable(context)) {
                    message += context.getResources().getString(R.string.empty_no_network);
                }
                textView.setText(message);
            }
        }
    }
}
