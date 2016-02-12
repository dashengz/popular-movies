package com.dashengz.popularmovies.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * The SQLite database contract of the app.
 * Created by Jonathan on 11/16/15.
 */
public class MovieContract {
    public static final String CONTENT_AUTHORITY = "me.dashengzhang.popularmovies";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final String PATH_MOVIES = "movies";
    public static final String PATH_REVIEWS = "reviews";
    public static final String PATH_TRAILERS = "trailers";

    // final ints to help find movie_id segment in urls
    public static final int MOVIE_ID_POSITION_IN_TRAILERS = 2;
    public static final int MOVIE_ID_POSITION_IN_REVIEWS = 2;

    /* Inner class that defines the table contents of the movies table */
    public static final class MovieEntry implements BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_MOVIES).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_MOVIES;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_MOVIES;

        // Table name
        public static final String TABLE_NAME = "movies";

        // public static final String COLUMN_MOVIE_ID = "movie_id";
        public static final String COLUMN_TITLE = "title";
        public static final String COLUMN_OVERVIEW = "overview";
        public static final String COLUMN_DATE = "date";
        public static final String COLUMN_POSTER_PATH = "poster_path";
        public static final String COLUMN_VOTE = "vote";
        public static final String COLUMN_POPULARITY = "popularity";
        public static final String COLUMN_RATING = "rating";
        public static final String COLUMN_FAVORITE = "favorite";

        // instead of saving movie_id directly in the movies table, build the url with appended movie_id
        // movie_id is stored as _ID
        // eg. content://me.dashengzhang.popularmovies/movies/id (actually is movie_id)
        public static Uri buildMovieUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        // get the movie_id appended to the content url
        // in order to let others tables access it
        public static long getMovieIdFromUri(Uri uriWithMovieId) {
            return Long.parseLong(uriWithMovieId.getLastPathSegment());
        }
    }

    /* Inner class that defines the table contents of the reviews table */
    public static final class ReviewEntry implements BaseColumns {

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
        public static final String COLUMN_URL = "url";

        // eg. content://me.dashengzhang.popularmovies/reviews/id
        public static Uri buildReviewUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        // eg. content://me.dashengzhang.popularmovies/reviews/movies/movie_id
        public static Uri buildUriByMovieId(long movieId) {
            return CONTENT_URI.buildUpon().appendPath(PATH_MOVIES)
                    .appendPath(Long.toString(movieId)).build();
        }
    }

    /* Inner class that defines the table contents of the trailers table */
    public static final class TrailerEntry implements BaseColumns {

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
        public static final String COLUMN_KEY = "key";
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_SITE = "site";
        public static final String COLUMN_TYPE = "type";

        // eg. content://me.dashengzhang.popularmovies/trailers/id
        public static Uri buildTrailerUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        // eg. content://me.dashengzhang.popularmovies/trailers/movies/movie_id
        public static Uri buildUriByMovieId(long movieId) {
            return CONTENT_URI.buildUpon().appendPath(PATH_MOVIES)
                    .appendPath(Long.toString(movieId)).build();
        }
    }
}
