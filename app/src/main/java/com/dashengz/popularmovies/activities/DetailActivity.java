package com.dashengz.popularmovies.activities;

import android.content.ContentUris;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.dashengz.popularmovies.R;
import com.dashengz.popularmovies.asynctasks.FetchReviewTask;
import com.dashengz.popularmovies.asynctasks.FetchTrailerTask;
import com.dashengz.popularmovies.data.MovieContract;
import com.dashengz.popularmovies.fragments.DetailFragment;

public class DetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        if (savedInstanceState == null) {
            Bundle arguments = new Bundle();
            Uri movieUri = getIntent().getData();
            FetchReviewTask reviewTask = new FetchReviewTask(this, ContentUris.parseId(movieUri));
            FetchTrailerTask trailerTask = new FetchTrailerTask(this, ContentUris.parseId(movieUri));
            reviewTask.execute();
            trailerTask.execute();

            arguments.putParcelable(DetailFragment.DETAIL_URI, movieUri);
            arguments.putParcelable(DetailFragment.DETAIL_REVIEW_URI,
                    MovieContract.ReviewEntry.buildUriByMovieId(ContentUris.parseId(movieUri)));
            arguments.putParcelable(DetailFragment.DETAIL_TRAILER_URI,
                    MovieContract.TrailerEntry.buildUriByMovieId(ContentUris.parseId(movieUri)));

            DetailFragment fragment = new DetailFragment();
            fragment.setArguments(arguments);

            getSupportFragmentManager().beginTransaction()
                    .add(R.id.movie_detail_container, fragment)
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
}
