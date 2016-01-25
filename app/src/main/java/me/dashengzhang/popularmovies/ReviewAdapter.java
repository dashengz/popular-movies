package me.dashengzhang.popularmovies;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Created by Jonathan on 1/24/16.
 */
public class ReviewAdapter extends CursorAdapter {
    public ReviewAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.list_item_reviews, parent, false);

        ViewHolder holder = new ViewHolder();
        holder.author = (TextView) view.findViewById(R.id.reviewAuthorField);
        holder.content = (TextView) view.findViewById(R.id.reviewContentField);
        holder.readMore = (TextView) view.findViewById(R.id.reviewContentReadMore);

        view.setTag(holder);

        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ViewHolder holder = (ViewHolder) view.getTag();

        holder.author.setText(cursor.getString(DetailFragment.COL_AUTHOR));
        String content = cursor.getString(DetailFragment.COL_CONTENT);
        if (content.length() > 300) {
            holder.readMore.setVisibility(View.VISIBLE);
            String contentShort = content.substring(0, 300);
            String contentDisplay = contentShort + " ...";
            holder.content.setText(contentDisplay);
        } else {
            holder.content.setText(content);
            holder.readMore.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public int getCount() {
        return getCursor() == null ? 0 : super.getCount();
    }

    private static class ViewHolder {
        TextView author;
        TextView content;
        TextView readMore;
    }
}
