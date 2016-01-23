package me.dashengzhang.popularmovies;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends AppCompatActivity {

    private final String LOG_TAG = MainActivity.class.getSimpleName();
    private final String MOVIEFRAGMENT_TAG = "MFTAG";

    private String mSorting;
    private String mVote;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mSorting = Utility.getPreferredSorting(this);
        mVote = Utility.getPreferredVote(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new MovieFragment(), MOVIEFRAGMENT_TAG)
                    .commit();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
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

    @Override
    protected void onResume() {
        super.onResume();
        String sorting = Utility.getPreferredSorting(this);
        String vote = Utility.getPreferredVote(this);

        if (!sorting.equals(mSorting)
                || sorting.equals(getString(R.string.pref_sorting_rating)) && !vote.equals(mVote)) {
            MovieFragment movieFragment = (MovieFragment) getSupportFragmentManager().findFragmentByTag(MOVIEFRAGMENT_TAG);
            if (null != movieFragment) {
                movieFragment.onPrefChanged();
            }
            mSorting = sorting;
            mVote = vote;
        }
    }

}
