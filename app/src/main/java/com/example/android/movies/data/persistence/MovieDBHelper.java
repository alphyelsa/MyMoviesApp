package com.example.android.movies.data.persistence;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.android.movies.data.persistence.MovieContract.MovieEntry;
import com.example.android.movies.data.persistence.MovieContract.TrailerEntry;

/**
 * Created by aesebast on 10/30/2016.
 */

public class MovieDBHelper extends SQLiteOpenHelper {

    // If you change the database schema, you must increment the database version.
    private static final int DATABASE_VERSION = 2;

    public static final String DATABASE_NAME = "movie.db";

    public MovieDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }


    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

        // Create a table to hold trailers.  A trailer consists of the videourl and thumbnail url
        final String SQL_CREATE_TRAILER_TABLE = "CREATE TABLE " + TrailerEntry.TABLE_NAME + " (" +
                TrailerEntry._ID + " INTEGER PRIMARY KEY," +
                TrailerEntry.COLUMN_MOVIE_ID + " TEXT NOT NULL, " +
                TrailerEntry.COLUMN_THUMBNAIL_URL + " TEXT NOT NULL, " +
                TrailerEntry.COLUMN_VIDEO_URL + " TEXT NOT NULL, " +
                // Set up the movie id column as a foreign key to movie table.
                " FOREIGN KEY (" + TrailerEntry.COLUMN_MOVIE_ID + ") REFERENCES " +
                MovieEntry.TABLE_NAME + " (" + MovieEntry.COLUMN_MOVIE_ID + ") " + " );";

        final String SQL_CREATE_MOVIE_TABLE = "CREATE TABLE " + MovieEntry.TABLE_NAME + " (" +
                MovieEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +

                // the ID of the movie entry associated with this movie data
                MovieEntry.COLUMN_MOVIE_ID + " TEXT NOT NULL, " +
                MovieEntry.COLUMN_MOVIE_TITLE + " TEXT NOT NULL, " +
                MovieEntry.COLUMN_MOVIE_POSTER + " TEXT NOT NULL, " +
                MovieEntry.COLUMN_PLOT_SYNOPSIS + " CLOB NOT NULL, " +
                MovieEntry.COLUMN_RELEASE_YEAR + " TEXT NOT NULL, " +
                MovieEntry.COLUMN_USER_RATING + " TEXT NOT NULL, " +
                MovieEntry.COLUMN_LENGTH + " TEXT "+
                // To assure the application does not multiple movies with same id,
                // added a UNIQUE constraint with REPLACE strategy
                //" UNIQUE (" + MovieEntry.COLUMN_MOVIE_ID + ") ON CONFLICT REPLACE) +
                ");";

        sqLiteDatabase.execSQL(SQL_CREATE_MOVIE_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_TRAILER_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        // Note that this only fires if you change the version number for your database.
        // It does NOT depend on the version number for your application.
        // If you want to update the schema without wiping data, commenting out the next 2 lines
        // should be your top priority before modifying this method.
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TrailerEntry.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + MovieEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }
}
