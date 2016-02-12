package com.dashengz.popularmovies.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.support.annotation.Nullable;

/**
 * The content provider of the app.
 * Created by Jonathan on 11/16/15.
 */
public class MovieProvider extends ContentProvider {

    static final int MOVIES = 100;
    static final int MOVIE_ITEM = 101;
    static final int REVIEWS_OF_MOVIE = 200;
    static final int REVIEW_ITEM = 201;
    static final int TRAILERS_OF_MOVIE = 300;
    static final int TRAILER_ITEM = 301;
    // The URI Matcher used by this content provider
    private static final UriMatcher sUriMatcher = buildUriMatcher();
    // table names, movie_ids
    private static final String MOVIES_TABLE = MovieContract.MovieEntry.TABLE_NAME;
    private static final String REVIEWS_TABLE = MovieContract.ReviewEntry.TABLE_NAME;
    private static final String TRAILERS_TABLE = MovieContract.TrailerEntry.TABLE_NAME;
    private static final String MOVIE_ID_IN_MOVIES = MovieContract.MovieEntry._ID;
    private static final String MOVIE_ID_IN_REVIEWS = MovieContract.ReviewEntry.COLUMN_MOVIE_ID;
    private static final String MOVIE_ID_IN_TRAILERS = MovieContract.TrailerEntry.COLUMN_MOVIE_ID;
    // DbHelper instance
    private MovieDbHelper movieDbHelper;

    static UriMatcher buildUriMatcher() {
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);

        final String authority = MovieContract.CONTENT_AUTHORITY;
        final String moviePath = MovieContract.PATH_MOVIES;
        final String reviewPath = MovieContract.PATH_REVIEWS;
        final String trailerPath = MovieContract.PATH_TRAILERS;

        // since all appended values are numbers, use # instead of *
        matcher.addURI(authority, moviePath, MOVIES);
        matcher.addURI(authority, moviePath + "/#", MOVIE_ITEM);
        matcher.addURI(authority, reviewPath + "/" + moviePath + "/#", REVIEWS_OF_MOVIE);
        matcher.addURI(authority, reviewPath + "/#", REVIEW_ITEM);
        matcher.addURI(authority, trailerPath + "/" + moviePath + "/#", TRAILERS_OF_MOVIE);
        matcher.addURI(authority, trailerPath + "/#", TRAILER_ITEM);

        return matcher;
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);

        switch (match) {
            case MOVIES:
                return MovieContract.MovieEntry.CONTENT_TYPE;
            case MOVIE_ITEM:
                return MovieContract.MovieEntry.CONTENT_ITEM_TYPE;
            case REVIEWS_OF_MOVIE:
                return MovieContract.ReviewEntry.CONTENT_TYPE;
            case REVIEW_ITEM:
                return MovieContract.ReviewEntry.CONTENT_ITEM_TYPE;
            case TRAILERS_OF_MOVIE:
                return MovieContract.TrailerEntry.CONTENT_TYPE;
            case TRAILER_ITEM:
                return MovieContract.TrailerEntry.CONTENT_ITEM_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Override
    public boolean onCreate() {
        movieDbHelper = new MovieDbHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        Cursor retCursor;
        final int match = sUriMatcher.match(uri);
        final SQLiteDatabase movieDatabase = movieDbHelper.getReadableDatabase();
        final SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();

        final String REVIEW_ID = MovieContract.ReviewEntry._ID;
        final String TRAILER_ID = MovieContract.TrailerEntry._ID;

        switch (match) {
            case MOVIES: {
                retCursor = movieDatabase.query(MOVIES_TABLE, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            }
            case MOVIE_ITEM: {
                Long movieId = ContentUris.parseId(uri);
                queryBuilder.setTables(MOVIES_TABLE);
                queryBuilder.appendWhere(MOVIE_ID_IN_MOVIES + "=" + movieId);
                retCursor = queryBuilder.query(movieDatabase, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            }
            case REVIEWS_OF_MOVIE: {
                String movieId = uri.getPathSegments().get(MovieContract.MOVIE_ID_POSITION_IN_REVIEWS);
                queryBuilder.setTables(REVIEWS_TABLE);
                queryBuilder.appendWhere(MOVIE_ID_IN_REVIEWS + "=" + movieId);
                retCursor = queryBuilder.query(movieDatabase, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            }
            case REVIEW_ITEM: {
                Long reviewId = ContentUris.parseId(uri);
                queryBuilder.setTables(REVIEWS_TABLE);
                queryBuilder.appendWhere(REVIEW_ID + "=" + reviewId);
                retCursor = queryBuilder.query(movieDatabase, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            }
            case TRAILERS_OF_MOVIE: {
                String movieId = uri.getPathSegments().get(MovieContract.MOVIE_ID_POSITION_IN_TRAILERS);
                queryBuilder.setTables(TRAILERS_TABLE);
                queryBuilder.appendWhere(MOVIE_ID_IN_TRAILERS + "=" + movieId);
                retCursor = queryBuilder.query(movieDatabase, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            }
            case TRAILER_ITEM: {
                Long trailerId = ContentUris.parseId(uri);
                queryBuilder.setTables(TRAILERS_TABLE);
                queryBuilder.appendWhere(TRAILER_ID + "=" + trailerId);
                retCursor = queryBuilder.query(movieDatabase, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return retCursor;
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final SQLiteDatabase movieDatabase = movieDbHelper.getReadableDatabase();
        final int match = sUriMatcher.match(uri);
        Uri returnUri;

        switch (match) {
            case MOVIES: {
                long _id = movieDatabase.insertOrThrow(MOVIES_TABLE, null, values);
                if (_id > 0)
                    returnUri = MovieContract.MovieEntry.buildMovieUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            case REVIEWS_OF_MOVIE: {
                long _id = movieDatabase.insert(REVIEWS_TABLE, null, values);
                if (_id > 0) {
                    long movieId = Long.parseLong(
                            uri.getPathSegments().get(MovieContract.MOVIE_ID_POSITION_IN_REVIEWS));
                    returnUri = MovieContract.ReviewEntry.buildUriByMovieId(movieId);
                } else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            case TRAILERS_OF_MOVIE: {
                long _id = movieDatabase.insert(TRAILERS_TABLE, null, values);
                if (_id > 0) {
                    long movieId = Long.parseLong(
                            uri.getPathSegments().get(MovieContract.MOVIE_ID_POSITION_IN_TRAILERS));
                    returnUri = MovieContract.TrailerEntry.buildUriByMovieId(movieId);
                } else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase movieDatabase = movieDbHelper.getReadableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsDeleted;

        // this makes delete all rows return the number of rows deleted
        if (null == selection) selection = "1";

        switch (match) {
            case MOVIES:
                rowsDeleted = movieDatabase.delete(MOVIES_TABLE, selection, selectionArgs);
                break;
            case REVIEWS_OF_MOVIE: {
                long movieId = ContentUris.parseId(uri);
                rowsDeleted = movieDatabase.delete(REVIEWS_TABLE,
                        MOVIE_ID_IN_REVIEWS + " = ? ", // select the movie_id that matches
                        new String[]{Long.toString(movieId)} // use the movieId as args
                );
                break;
            }
            case TRAILERS_OF_MOVIE: {
                long movieId = ContentUris.parseId(uri);
                rowsDeleted = movieDatabase.delete(TRAILERS_TABLE,
                        MOVIE_ID_IN_TRAILERS + " = ? ", // select the movieId that matches
                        new String[]{Long.toString(movieId)} // use the movieId as args
                );
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        // Because a null deletes all rows
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return rowsDeleted;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final SQLiteDatabase movieDatabase = movieDbHelper.getReadableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsUpdated;

        switch (match) {
            case MOVIES:
                rowsUpdated = movieDatabase.update(MOVIES_TABLE, values, selection, selectionArgs);
                break;
            case REVIEWS_OF_MOVIE:
                rowsUpdated = movieDatabase.update(REVIEWS_TABLE, values, selection, selectionArgs);
                break;
            case TRAILERS_OF_MOVIE:
                rowsUpdated = movieDatabase.update(TRAILERS_TABLE, values, selection, selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsUpdated;
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        final SQLiteDatabase movieDatabase = movieDbHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);

        switch (match) {
            case MOVIES:
                movieDatabase.beginTransaction();
                int returnCount = 0;
                try {
                    // overwrite if the movie_id(_id in Movies Table) already exists
                    // do update() on those targets instead of insert()
                    for (ContentValues value : values) {
                        try {
                            // use insertOrThrow to check if the movie entry already exists
                            movieDatabase.insertOrThrow(MOVIES_TABLE, null, value);
                            returnCount++;
                        } catch (SQLException e) {
                            // SQLException happens when there's existing movie entry that has the same id
                            // extracts movieId from the values fed in
                            String movieId = value.getAsString(MOVIE_ID_IN_MOVIES);
                            returnCount += movieDatabase.update(MOVIES_TABLE, value,
                                    MOVIE_ID_IN_MOVIES + "=?", // select the movieId that matches
                                    new String[]{movieId}); // use the movieId as args
                        }
                    }
                    movieDatabase.setTransactionSuccessful();
                } finally {
                    movieDatabase.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }
}
