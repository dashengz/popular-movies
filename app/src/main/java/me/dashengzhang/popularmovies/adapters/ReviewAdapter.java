package me.dashengzhang.popularmovies.adapters;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import me.dashengzhang.popularmovies.R;
import me.dashengzhang.popularmovies.fragments.DetailFragment;

/**
 * Created by Jonathan on 1/24/16.
 * CursorAdapter for displaying reviews in DetailActivity
 */
public class ReviewAdapter extends CursorAdapter {
    public ReviewAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.list_item_reviews, parent, false);

        ViewHolder holder = new ViewHolder(view);
        view.setTag(holder);

        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ViewHolder holder = (ViewHolder) view.getTag();

        holder.author.setText(cursor.getString(DetailFragment.COL_AUTHOR));
        holder.content.setText(cursor.getString(DetailFragment.COL_CONTENT));
    }

    @Override
    public int getCount() {
        return getCursor() == null ? 0 : super.getCount();
    }

    private static class ViewHolder {
        TextView author;
        TextView content;

        private ViewHolder(View view) {
            author = (TextView) view.findViewById(R.id.reviewAuthorField);
            content = (TextView) view.findViewById(R.id.reviewContentField);
        }
    }
}
