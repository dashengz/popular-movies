package me.dashengzhang.popularmovies.activities;

import android.content.ContentUris;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import me.dashengzhang.popularmovies.R;
import me.dashengzhang.popularmovies.asynctasks.FetchReviewTask;
import me.dashengzhang.popularmovies.asynctasks.FetchTrailerTask;
import me.dashengzhang.popularmovies.fragments.DetailFragment;

public class DetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        if (savedInstanceState == null) {
            Intent intent = getIntent();
            FetchReviewTask reviewTask = new FetchReviewTask(this, ContentUris.parseId(intent.getData()));
            FetchTrailerTask trailerTask = new FetchTrailerTask(this, ContentUris.parseId(intent.getData()));
            reviewTask.execute();
            trailerTask.execute();
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
}