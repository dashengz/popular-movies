package com.dashengz.popularmovies.adapters;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.dashengz.popularmovies.R;
import com.dashengz.popularmovies.fragments.DetailFragment;

/**
 * Created by Jonathan on 1/24/16.
 * CursorAdapter for displaying trailers in DetailActivity
 */
public class TrailerAdapter extends CursorAdapter {
    public TrailerAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.list_item_trailers, parent, false);

        ViewHolder holder = new ViewHolder(view);
        view.setTag(holder);

        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ViewHolder holder = (ViewHolder) view.getTag();

        holder.name.setText(cursor.getString(DetailFragment.COL_NAME));
    }

    @Override
    public int getCount() {
        return getCursor() == null ? 0 : super.getCount();
    }

    private static class ViewHolder {
        TextView name;

        private ViewHolder(View view) {
            name = (TextView) view.findViewById(R.id.trailerNameField);
        }
    }
}
