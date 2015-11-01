package me.dashengzhang.popularmovies;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

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

    public static class DetailFragment extends Fragment {

        // private String movieId;
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

            // The detail Activity called via intent.  Inspect the intent for movie data.
            Intent intent = getActivity().getIntent();

            // continue if your intent is actually received;
            if (intent != null) {
                // movieId = intent.getStringExtra(MovieFragment.INTENT_MOVIE_ID);
                movieTitle = intent.getStringExtra(MovieFragment.INTENT_MOVIE_TITLE);
                movieOverview = intent.getStringExtra(MovieFragment.INTENT_MOVIE_OVERVIEW);
                movieDate = intent.getStringExtra(MovieFragment.INTENT_MOVIE_DATE);
                moviePosterPath = intent.getStringExtra(MovieFragment.INTENT_MOVIE_POSTER_PATH);
                movieVote = intent.getDoubleExtra(MovieFragment.INTENT_MOVIE_VOTE, 0);
                String voteDisplay = movieVote + "/10";

                ((TextView) rootView.findViewById(R.id.title)).setText(movieTitle);
                ((TextView) rootView.findViewById(R.id.overview)).setText(movieOverview);
                ((TextView) rootView.findViewById(R.id.date)).setText(movieDate);
                ((TextView) rootView.findViewById(R.id.rating)).setText(voteDisplay);

                Picasso.with(getActivity())
                        .load(moviePosterPath)
                        .placeholder(R.drawable.placeholder)
                        .error(R.drawable.placeholder)
                        .into((ImageView) rootView.findViewById(R.id.poster));
            }

            return rootView;
        }
    }
}
