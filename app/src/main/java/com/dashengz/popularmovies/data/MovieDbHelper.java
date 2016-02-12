package com.dashengz.popularmovies.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.dashengz.popularmovies.data.MovieContract.MovieEntry;
import com.dashengz.popularmovies.data.MovieContract.ReviewEntry;
import com.dashengz.popularmovies.data.MovieContract.TrailerEntry;

/**
 * The SQLiteOpenHelper of the app.
 * Created by Jonathan on 11/16/15.
 */
public class MovieDbHelper extends SQLiteOpenHelper {
    static final String DATABASE_NAME = "movie_info.db";
    private static final int DATABASE_VERSION = 1;

    public MovieDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        final String SQL_CREATE_MOVIE_TABLE =
                "CREATE TABLE " + MovieEntry.TABLE_NAME + " (" +
                        MovieEntry._ID + " INTEGER PRIMARY KEY, " +
                        MovieEntry.COLUMN_TITLE + " TEXT NOT NULL, " +
                        MovieEntry.COLUMN_OVERVIEW + " TEXT NOT NULL, " +
                        MovieEntry.COLUMN_DATE + " TEXT NOT NULL, " +
                        MovieEntry.COLUMN_POSTER_PATH + " TEXT NOT NULL, " +
                        MovieEntry.COLUMN_VOTE + " REAL NOT NULL, " +
                        MovieEntry.COLUMN_POPULARITY + " REAL, " +
                        MovieEntry.COLUMN_RATING + " INTEGER, " +
                        MovieEntry.COLUMN_FAVORITE + " INTEGER " +
                        " )";

        final String SQL_CREATE_REVIEW_TABLE =
                "CREATE TABLE " + ReviewEntry.TABLE_NAME + " (" +
                        ReviewEntry._ID + " INTEGER PRIMARY KEY, " +
                        ReviewEntry.COLUMN_MOVIE_ID + " INTEGER NOT NULL, " +
                        ReviewEntry.COLUMN_REVIEW_ID + " TEXT UNIQUE NOT NULL, " +
                        ReviewEntry.COLUMN_AUTHOR + " TEXT NOT NULL, " +
                        ReviewEntry.COLUMN_CONTENT + " TEXT NOT NULL, " +
                        ReviewEntry.COLUMN_URL + " TEXT NOT NULL, " +
                        // use on delete cascade to make sure that when the movie entry is deleted,
                        // the reviews of this movie will also be deleted
                        "FOREIGN KEY (" + ReviewEntry.COLUMN_MOVIE_ID + ") REFERENCES " +
                        MovieEntry.TABLE_NAME + " (" + MovieEntry._ID + " ) ON DELETE CASCADE)";

        final String SQL_CREATE_TRAILER_TABLE =
                "CREATE TABLE " + TrailerEntry.TABLE_NAME + " (" +
                        TrailerEntry._ID + " INTEGER PRIMARY KEY, " +
                        TrailerEntry.COLUMN_MOVIE_ID + " INTEGER NOT NULL, " +
                        TrailerEntry.COLUMN_TRAILER_ID + " TEXT UNIQUE NOT NULL, " +
                        TrailerEntry.COLUMN_KEY + " TEXT NOT NULL, " +
                        TrailerEntry.COLUMN_NAME + " TEXT NOT NULL, " +
                        TrailerEntry.COLUMN_SITE + " TEXT NOT NULL, " +
                        TrailerEntry.COLUMN_TYPE + " TEXT NOT NULL, " +
                        // use on delete cascade to make sure that when the movie entry is deleted,
                        // the trailers of this movie will also be deleted
                        "FOREIGN KEY (" + TrailerEntry.COLUMN_MOVIE_ID + ") REFERENCES " +
                        MovieEntry.TABLE_NAME + " (" + MovieEntry._ID + " ) ON DELETE CASCADE)";

        db.execSQL(SQL_CREATE_MOVIE_TABLE);
        db.execSQL(SQL_CREATE_REVIEW_TABLE);
        db.execSQL(SQL_CREATE_TRAILER_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Will update in the future if database setup changes.
    }
}
