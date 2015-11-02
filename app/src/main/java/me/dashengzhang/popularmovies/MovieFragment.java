package me.dashengzhang.popularmovies;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class MovieFragment extends Fragment {

    // Intent keys
    public static final String INTENT_MOVIE_ID = "movieId";
    public static final String INTENT_MOVIE_TITLE = "movieTitle";
    public static final String INTENT_MOVIE_OVERVIEW = "movieOverview";
    public static final String INTENT_MOVIE_DATE = "movieDate";
    public static final String INTENT_MOVIE_POSTER_PATH = "moviePosterPath";
    public static final String INTENT_MOVIE_VOTE = "movieVote";
    public static final String MOVIE_KEY = "movie_key";

    mMovieImgAdapter adapter;
    ArrayList<MovieInfo> mMovieInfo = new ArrayList<>();
    String sorting;

    public MovieFragment() {
    }

    @Override
    public void onResume() {
        super.onResume();
        // if preference is changed, then re-grab info from api
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String newSorting = prefs.getString(getString(R.string.pref_sorting_key),
                getString(R.string.pref_sorting_popularity));
        if (newSorting != null && sorting != null && !newSorting.equals(sorting)) {
            FetchMovieTask movieTask = new FetchMovieTask();
            movieTask.execute(newSorting);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        sorting = prefs.getString(getString(R.string.pref_sorting_key),
                getString(R.string.pref_sorting_popularity));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String sorting = prefs.getString(getString(R.string.pref_sorting_key),
                getString(R.string.pref_sorting_popularity));

        // if there's existing matching savedInstanceState then don't need to re-grab
        if (savedInstanceState == null || !savedInstanceState.containsKey(MOVIE_KEY)) {
            FetchMovieTask movieTask = new FetchMovieTask();
            movieTask.execute(sorting);
        } else if (savedInstanceState.containsKey(MOVIE_KEY)) {
            mMovieInfo = savedInstanceState.getParcelableArrayList(MovieFragment.MOVIE_KEY);
        }

        GridView gridView = (GridView) rootView.findViewById(R.id.gridView_movies);
        adapter = new mMovieImgAdapter(getActivity());
        gridView.setAdapter(adapter);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v,
                                    int position, long id) {
                // the Strings stored in the mMovieInfo ArrayList
                long movieId;
                String movieTitle;
                String movieOverview;
                String movieDate;
                String moviePosterPath;
                double movieVote;

                movieId = mMovieInfo.get(position).getId();
                movieTitle = mMovieInfo.get(position).getTitle();
                movieOverview = mMovieInfo.get(position).getOverview();
                movieDate = mMovieInfo.get(position).getDate();
                moviePosterPath = mMovieInfo.get(position).getPoster();
                movieVote = mMovieInfo.get(position).getVote();

                Intent intent = new Intent(getActivity(), DetailActivity.class)
                        .putExtra(INTENT_MOVIE_ID, movieId)
                        .putExtra(INTENT_MOVIE_TITLE, movieTitle)
                        .putExtra(INTENT_MOVIE_OVERVIEW, movieOverview)
                        .putExtra(INTENT_MOVIE_DATE, movieDate)
                        .putExtra(INTENT_MOVIE_POSTER_PATH, moviePosterPath)
                        .putExtra(INTENT_MOVIE_VOTE, movieVote);
                startActivity(intent);
            }
        });

        return rootView;
    }

    // save parcelable to instance state
    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelableArrayList(MOVIE_KEY, mMovieInfo);
        super.onSaveInstanceState(outState);
    }

    public class mMovieImgAdapter extends BaseAdapter {

        private Context mContext;

        public mMovieImgAdapter(Context c) {
            this.mContext = c;
        }

        public int getCount() {
            return mMovieInfo.size();
        }

        public Object getItem(int position) {
            return null;
        }

        public long getItemId(int position) {
            return 0;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            ImageView imageView;
            if (convertView == null) {
                imageView = new ImageView(mContext);
            } else {
                imageView = (ImageView) convertView;
            }

            // Picasso
            Picasso.with(getActivity())
                    .load(mMovieInfo.get(position).getPoster())
                    .placeholder(R.drawable.placeholder)
                    .error(R.drawable.placeholder)
                    .fit()
                    .into(imageView);

            return imageView;
        }
    }

    // fetch data from TMD
    public class FetchMovieTask extends AsyncTask<String, Void, ArrayList<MovieInfo>> {

        // Error log name; Rather than a string, use this so that when refactoring no more errors;
        private final String LOG_TAG = FetchMovieTask.class.getSimpleName();

        private ArrayList<MovieInfo> getMovieDataFromJson(String movieJsonStr)
                throws JSONException {

            // These are the names of the JSON objects that need to be extracted.
            final String TMD_RESULTS = "results";
            final String TMD_ID = "id";
            final String TMD_ORIGINAL_TITLE = "original_title";
            final String TMD_OVERVIEW = "overview";
            final String TMD_RELEASE_DATE = "release_date";
            final String TMD_POSTER_PATH = "poster_path";
            final String TMD_VOTE_AVERAGE = "vote_average";

            JSONObject movieJson = new JSONObject(movieJsonStr);
            JSONArray movieArray = movieJson.getJSONArray(TMD_RESULTS);

            ArrayList<MovieInfo> resultStrs = new ArrayList<>();

            for (int i = 0; i < movieArray.length(); i++) {

                // Get the JSON object representing the movie item
                JSONObject movieObject = movieArray.getJSONObject(i);

                long id = movieObject.getLong(TMD_ID);
                String title = movieObject.getString(TMD_ORIGINAL_TITLE);
                String overview = movieObject.getString(TMD_OVERVIEW);
                String date = movieObject.getString(TMD_RELEASE_DATE);
                // poster path
                String path = getResources().getString(R.string.img_url_base) + getResources().getString(R.string.img_size) + movieObject.getString(TMD_POSTER_PATH);
                double vote = movieObject.getDouble(TMD_VOTE_AVERAGE);

                MovieInfo movieDetail = new MovieInfo(id, title, overview, date, path, vote);
                resultStrs.add(movieDetail);

            }
            return resultStrs;

        }

        // referencing the Udacity Sunshine course code:
        @Override
        protected ArrayList<MovieInfo> doInBackground(String... params) {

            if (params.length == 0) {
                return null;
            }

            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain the raw JSON response as a string.
            String movieJsonStr = null;

            // Data is fetched based on popularity by default.
            // If user prefers to sort by vote_average, change from here.

            SharedPreferences sharedPrefs =
                    PreferenceManager.getDefaultSharedPreferences(getActivity());
            String sortBy = sharedPrefs.getString(
                    getString(R.string.pref_sorting_key),
                    getString(R.string.pref_sorting_popularity));

            try {
                // Construct the URL for the query

                // build a URL with the params
                Uri builtUri;
                final String MOVIE_BASE_URL =
                        "http://api.themoviedb.org/3/discover/movie?";
                final String SORTING_PARAM = "sort_by";
                final String KEY_PARAM = "api_key";
                final String COUNT_PARAM = "vote_count.gte";

                if (sortBy.equals(getString(R.string.pref_sorting_popularity))) {
                    builtUri = Uri.parse(MOVIE_BASE_URL).buildUpon()
                            .appendQueryParameter(SORTING_PARAM, params[0])
                            .appendQueryParameter(KEY_PARAM, getResources().getString(R.string.api_key))
                            .build();
                } else {
                    builtUri = Uri.parse(MOVIE_BASE_URL).buildUpon()
                            .appendQueryParameter(SORTING_PARAM, params[0])
                            .appendQueryParameter(COUNT_PARAM, getResources().getString(R.string.vote_count))
                            .appendQueryParameter(KEY_PARAM, getResources().getString(R.string.api_key))
                            .build();
                }

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
                return getMovieDataFromJson(movieJsonStr);
            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }

            return null;
        }

        // onPostExecute(Result), invoked on the UI thread after the background computation finishes.
        // The result of the background computation is passed to this step as a parameter.
        @Override
        protected void onPostExecute(ArrayList<MovieInfo> result) {

            if (result != null) {

                mMovieInfo.clear();
                mMovieInfo.addAll(result);

                // debug log
                // Log.e(LOG_TAG, "New Pull!");

                // update adapter;
                adapter.notifyDataSetChanged();
            }
        }
    }
}
