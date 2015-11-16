package me.dashengzhang.popularmovies.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by Jonathan on 11/16/15.
 */
public class MovieContract {
    public static final String CONTENT_AUTHORITY = "me.dashengzhang.popularmovies";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final String PATH_MOVIE_INFO = "movie_info";
    public static final String PATH_RATING = "rating";
    public static final String PATH_POPULARITY = "popularity";
    public static final String PATH_FAVORITE = "favorite";
    public static final String PATH_REVIEWS = "reviews";
    public static final String PATH_TRAILERS = "trailers";

    /* Inner class that defines the table contents of the movie_info table */
    public static final class MovieInfoEntry implements BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_MOVIE_INFO).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_MOVIE_INFO;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_MOVIE_INFO;

        // Table name
        public static final String TABLE_NAME = "movie_info";

        public static final String COLUMN_MOVIE_ID = "movie_id";
        public static final String COLUMN_TITLE = "title";
        public static final String COLUMN_OVERVIEW = "overview";
        public static final String COLUMN_DATE = "date";
        public static final String COLUMN_POSTER_PATH = "poster_path";
        public static final String COLUMN_VOTE = "vote";

        public static Uri buildMovieInfoUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
    }

    /* Inner class that defines the table contents of the movie_info table */
    public static final class PopularityEntry implements BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_POPULARITY).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_POPULARITY;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_POPULARITY;

        // Table name
        public static final String TABLE_NAME = "popularity";

        public static final String COLUMN_MOVIE_ID = "movie_id";

        public static Uri buildPopularityUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
    }

    /* Inner class that defines the table contents of the movie_info table */
    public static final class RatingEntry implements BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_RATING).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_RATING;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_RATING;

        // Table name
        public static final String TABLE_NAME = "rating";

        public static final String COLUMN_MOVIE_ID = "movie_id";

        public static Uri buildRatingUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
    }

    /* Inner class that defines the table contents of the movie_info table */
    public static final class FavoriteEntry implements BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_FAVORITE).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_FAVORITE;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_FAVORITE;

        // Table name
        public static final String TABLE_NAME = "favorite";

        public static final String COLUMN_MOVIE_ID = "movie_id";

        public static Uri buildFavoriteUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
    }

    /* Inner class that defines the table contents of the movie_info table */
    public static final class ReviewsEntry implements BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_REVIEWS).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_REVIEWS;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_REVIEWS;

        // Table name
        public static final String TABLE_NAME = "reviews";

        public static final String COLUMN_MOVIE_ID = "movie_id";
        public static final String COLUMN_REVIEW_ID = "review_id";
        public static final String COLUMN_AUTHOR = "author";
        public static final String COLUMN_CONTENT = "content";

        public static Uri buildReviewsUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
    }

    /* Inner class that defines the table contents of the movie_info table */
    public static final class TrailersEntry implements BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_TRAILERS).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_TRAILERS;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_TRAILERS;

        // Table name
        public static final String TABLE_NAME = "trailers";

        public static final String COLUMN_MOVIE_ID = "movie_id";
        public static final String COLUMN_TRAILER_ID = "trailer_id";
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_KEY = "key";
        public static final String COLUMN_TYPE = "type";

        public static Uri buildTrailersUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
    }
}
