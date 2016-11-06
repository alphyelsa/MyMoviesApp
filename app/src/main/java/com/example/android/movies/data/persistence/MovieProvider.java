package com.example.android.movies.data.persistence;

import android.annotation.TargetApi;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

/**
 * Created by aesebast on 10/30/2016.
 */

public class MovieProvider extends ContentProvider {

    public static final String LOG_TAG = MovieProvider.class.getName();
    public static final int MOVIE = 100;
    public static final int MOVIE_WITH_ID = 101;
    public static final int TRAILER = 200;
    public static final int TRAILER_WITH_ID = 201;

    // The URI Matcher used by this content provider.
    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private MovieDBHelper mOpenHelper;

    private static final String MOVIE_WITH_ID_SELECTION =  MovieContract.MovieEntry.COLUMN_MOVIE_ID + "=?";
    private static final String TRAILER_WITH_ID_SELECTION =  MovieContract.TrailerEntry.COLUMN_MOVIE_ID + "=?";

    /*
        This UriMatcher will match each URI to the MOVIE and MOVIE_WITH_ID integer constants
        defined above.  This can be tested by uncommenting the testUriMatcher test within TestUriMatcher.
     */
    public static UriMatcher buildUriMatcher() {
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = MovieContract.CONTENT_AUTHORITY;

        // For each type of URI you want to add, create a corresponding code.
        matcher.addURI(authority, MovieContract.PATH_MOVIE, MOVIE);
        matcher.addURI(authority, MovieContract.PATH_MOVIE + "/*", MOVIE_WITH_ID);
        matcher.addURI(authority,MovieContract.PATH_TRAILER, TRAILER);
        matcher.addURI(authority,MovieContract.PATH_TRAILER + "/*",TRAILER_WITH_ID);
        return matcher;
    }

    /*
        create a new MovieDbHelper
     */
    @Override
    public boolean onCreate() {
        mOpenHelper = new MovieDBHelper(getContext());
        return true;
    }

    @Override
    public String getType(Uri uri) {

        // Use the Uri Matcher to determine what kind of URI this is.
        final int match = sUriMatcher.match(uri);

        switch (match) {
            case MOVIE:
            case MOVIE_WITH_ID:
                return MovieContract.MovieEntry.CONTENT_TYPE;
            case TRAILER:
            case TRAILER_WITH_ID:
                return MovieContract.TrailerEntry.CONTENT_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        // Here's the switch statement that, given a URI, will determine what kind of request it is,
        // and query the database accordingly.
        Cursor retCursor = null;
        switch (sUriMatcher.match(uri)) {
            case MOVIE:{
                retCursor = mOpenHelper.getReadableDatabase().query(MovieContract.MovieEntry.TABLE_NAME,projection,selection,selectionArgs,null,null,null);
                break;
            }
            case MOVIE_WITH_ID: {
                retCursor = mOpenHelper.getReadableDatabase().query(MovieContract.MovieEntry.TABLE_NAME,projection,MOVIE_WITH_ID_SELECTION, new String[]{MovieContract.MovieEntry.getMovieIdFromUri(uri)},null,null,null);
                break;
            }
            case TRAILER:{
                retCursor = mOpenHelper.getReadableDatabase().query(MovieContract.TrailerEntry.TABLE_NAME,projection,selection,selectionArgs,null,null,null);
                break;
            }
            case TRAILER_WITH_ID: {
                retCursor = mOpenHelper.getReadableDatabase().query(MovieContract.TrailerEntry.TABLE_NAME,projection,TRAILER_WITH_ID_SELECTION,new String[]{MovieContract.TrailerEntry.getMovieIdFromUri(uri)},null,null,null);
                break;
            }

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return retCursor;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        Uri returnUri;

        switch (match) {
            case MOVIE: {
                long _id = db.insert(MovieContract.MovieEntry.TABLE_NAME, null, values);
                if ( _id > 0 )
                    returnUri = MovieContract.MovieEntry.buildMovieUri(values.getAsString(MovieContract.MovieEntry.COLUMN_MOVIE_ID));
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            case TRAILER:{
                long _id = db.insert(MovieContract.TrailerEntry.TABLE_NAME, null, values);
                if ( _id > 0 )
                    returnUri = MovieContract.TrailerEntry.buildTrailerUri(values.getAsString(MovieContract.TrailerEntry.COLUMN_MOVIE_ID));
                else
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
        //Get a writable database
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();

        final int match = sUriMatcher.match(uri);
        int rowsDeleted = 0;

        switch (match) {
            case MOVIE: {
                rowsDeleted = db.delete(MovieContract.MovieEntry.TABLE_NAME, selection, selectionArgs);
                break;
            }
            case MOVIE_WITH_ID: {
                rowsDeleted = db.delete(MovieContract.MovieEntry.TABLE_NAME, MOVIE_WITH_ID_SELECTION, new String[]{MovieContract.MovieEntry.getMovieIdFromUri(uri)});
                break;
            }
            case TRAILER:{
                rowsDeleted = db.delete(MovieContract.TrailerEntry.TABLE_NAME, selection, selectionArgs);
                break;
            }
            case TRAILER_WITH_ID: {
                rowsDeleted = db.delete(MovieContract.TrailerEntry.TABLE_NAME, TRAILER_WITH_ID_SELECTION, new String[]{MovieContract.TrailerEntry.getMovieIdFromUri(uri)});
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        if(rowsDeleted >0)
            getContext().getContentResolver().notifyChange(uri, null);
        return rowsDeleted;
    }

    @Override
    public int update(
            Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();

        final int match = sUriMatcher.match(uri);
        int rowsUpdated = 0;

        switch (match) {
            case MOVIE:{
                rowsUpdated = db.update(MovieContract.MovieEntry.TABLE_NAME, values, selection, selectionArgs);
                break;
            }
            case MOVIE_WITH_ID: {
                rowsUpdated = db.update(MovieContract.MovieEntry.TABLE_NAME, values, MOVIE_WITH_ID_SELECTION, new String[]{MovieContract.MovieEntry.getMovieIdFromUri(uri)});
                break;
            }
            case TRAILER:{
                rowsUpdated = db.update(MovieContract.TrailerEntry.TABLE_NAME, values, selection, selectionArgs);
                break;
            }
            case TRAILER_WITH_ID: {
                rowsUpdated = db.update(MovieContract.TrailerEntry.TABLE_NAME, values, TRAILER_WITH_ID_SELECTION, new String[]{MovieContract.TrailerEntry.getMovieIdFromUri(uri)});
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        if(rowsUpdated >0)
            getContext().getContentResolver().notifyChange(uri, null);
        return rowsUpdated;
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case MOVIE:
                db.beginTransaction();
                int returnCount = 0;
                try {
                    for (ContentValues value : values) {
                        long _id = db.insert(MovieContract.MovieEntry.TABLE_NAME, null, value);
                        if (_id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;
            case TRAILER:
                db.beginTransaction();
                returnCount = 0;
                try {
                    for (ContentValues value : values) {
                        long _id = db.insert(MovieContract.TrailerEntry.TABLE_NAME, null, value);
                        if (_id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;
            default:
                return super.bulkInsert(uri, values);
        }
    }

    // You do not need to call this method. This is a method specifically to assist the testing
    // framework in running smoothly. You can read more at:
    // http://developer.android.com/reference/android/content/ContentProvider.html#shutdown()
    @Override
    @TargetApi(11)
    public void shutdown() {
        mOpenHelper.close();
        super.shutdown();
    }
}
