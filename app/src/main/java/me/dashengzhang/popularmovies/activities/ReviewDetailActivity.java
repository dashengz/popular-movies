package me.dashengzhang.popularmovies.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import me.dashengzhang.popularmovies.R;
import me.dashengzhang.popularmovies.fragments.DetailFragment;

public class ReviewDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review_detail);

        Intent intent = getIntent();
        ((TextView) findViewById(R.id.reviewAuthorField)).setText(intent.getStringExtra(DetailFragment.AUTHOR_INTENT));
        ((TextView) findViewById(R.id.reviewContentField)).setText(intent.getStringExtra(DetailFragment.CONTENT_INTENT));
    }

}
