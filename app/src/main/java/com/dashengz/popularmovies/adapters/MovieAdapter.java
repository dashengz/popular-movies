package com.dashengz.popularmovies.adapters;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;

import com.dashengz.popularmovies.R;
import com.dashengz.popularmovies.fragments.MovieFragment;
import com.squareup.picasso.Picasso;

/**
 * Created by Jonathan on 11/16/15.
 * CursorAdapter for displaying movies in MainActivity
 */
public class MovieAdapter extends CursorAdapter {
    public MovieAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.list_item_movies, parent, false);

        ViewHolder holder = new ViewHolder(view);
        view.setTag(holder);

        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ViewHolder holder = (ViewHolder) view.getTag();

        Picasso.with(context)
                .load(cursor.getString(MovieFragment.COL_MOVIE_POSTER_PATH))
                .placeholder(R.drawable.placeholder)
                .error(R.drawable.placeholder)
                .fit()
                .tag(context)
                .into(holder.imageView);
    }

    @Override
    public int getCount() {
        return getCursor() == null ? 0 : super.getCount();
    }

    private static class ViewHolder {
        ImageView imageView;

        private ViewHolder(View view) {
            imageView = (ImageView) view.findViewById(R.id.list_item_imageView);
        }
    }
}