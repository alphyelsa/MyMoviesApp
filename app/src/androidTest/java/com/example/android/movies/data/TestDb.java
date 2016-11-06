package com.example.android.movies.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.test.AndroidTestCase;

import com.example.android.movies.data.persistence.MovieContract;
import com.example.android.movies.data.persistence.MovieDBHelper;

import java.util.HashSet;

/**
 * Created by aesebast on 10/30/2016.
 */

public class TestDb extends AndroidTestCase {
    public static final String LOG_TAG = TestDb.class.getSimpleName();

    // Since we want each test to start with a clean slate
    void deleteTheDatabase() {
        mContext.deleteDatabase(MovieDBHelper.DATABASE_NAME);
    }

    /*
        This function gets called before each test is executed to delete the database.  This makes
        sure that we always have a clean test.
     */
    public void setUp() {
        deleteTheDatabase();
    }


    public void testCreateDb() throws Throwable {
        // build a HashSet of all of the table names we wish to look for
        // Note that there will be another table in the DB that stores the
        // Android metadata (db version information)
        final HashSet<String> tableNameHashSet = new HashSet<String>();
        tableNameHashSet.add(MovieContract.MovieEntry.TABLE_NAME);
        tableNameHashSet.add(MovieContract.TrailerEntry.TABLE_NAME);

        mContext.deleteDatabase(MovieDBHelper.DATABASE_NAME);
        SQLiteDatabase db = new MovieDBHelper(
                this.mContext).getWritableDatabase();
        assertEquals(true, db.isOpen());

        // have we created the tables we want?
        Cursor c = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null);

        assertTrue("Error: This means that the database has not been created correctly",
                c.moveToFirst());

        // verify that the tables have been created
        do {
            tableNameHashSet.remove(c.getString(0));
        } while (c.moveToNext());

        // if this fails, it means that your database doesn't contain both the location entry
        // and weather entry tables
        assertTrue("Error: Your database was created without both the location entry and weather entry tables",
                tableNameHashSet.isEmpty());

        // now, do our tables contain the correct columns?
        c = db.rawQuery("PRAGMA table_info(" + MovieContract.MovieEntry.TABLE_NAME + ")",
                null);

        assertTrue("Error: This means that we were unable to query the database for table information.",
                c.moveToFirst());

        // Build a HashSet of all of the column names we want to look for
        final HashSet<String> movieColumnHashSet = new HashSet<String>();
        movieColumnHashSet.add(MovieContract.MovieEntry._ID);
        movieColumnHashSet.add(MovieContract.MovieEntry.COLUMN_MOVIE_ID);
        movieColumnHashSet.add(MovieContract.MovieEntry.COLUMN_MOVIE_TITLE);
        movieColumnHashSet.add(MovieContract.MovieEntry.COLUMN_MOVIE_POSTER);
        movieColumnHashSet.add(MovieContract.MovieEntry.COLUMN_PLOT_SYNOPSIS);
        movieColumnHashSet.add(MovieContract.MovieEntry.COLUMN_RELEASE_YEAR);
        movieColumnHashSet.add(MovieContract.MovieEntry.COLUMN_USER_RATING);
        movieColumnHashSet.add(MovieContract.MovieEntry.COLUMN_LENGTH);

        int columnNameIndex = c.getColumnIndex("name");
        do {
            String columnName = c.getString(columnNameIndex);
            movieColumnHashSet.remove(columnName);
        } while (c.moveToNext());

        // if this fails, it means that your database doesn't contain all of the required location
        // entry columns
        assertTrue("Error: The database doesn't contain all of the required location entry columns",
                movieColumnHashSet.isEmpty());
        db.close();
    }

    /*
        Testing the database setup
     */
    public void testModel(Context context) {
        // insert our test records into the database
        MovieDBHelper dbHelper = new MovieDBHelper(context);
        SQLiteDatabase db = dbHelper.getWritableDatabase();


        ContentValues movieValues = TestUtilities.createMovieValues();
        long movieRowId = db.insert(MovieContract.MovieEntry.TABLE_NAME, null, movieValues);
        // Verify we got a row back.
        assertTrue("Error: Failure to insert records into movie table", movieRowId != -1);

        ContentValues trailerValues = TestUtilities.createTrailerValues();
        long trailerRowId = db.insert(MovieContract.TrailerEntry.TABLE_NAME, null, trailerValues);
        // Verify we got a row back.
        assertTrue("Error: Failure to insert trailer records", trailerRowId != -1);

        //[uery the database and receive a Cursor back
        Cursor cursor = db.query(MovieContract.MovieEntry.TABLE_NAME,  // Table to Query
                null, // all columns
                null, // Columns for the "where" clause
                null, // Values for the "where" clause
                null, // columns to group by
                null, // columns to filter by row groups
                null // sort order
        );
        // Move the cursor to a valid database row
        assertTrue("Error: No Records returned from location query", cursor.moveToFirst());

        // Validate data in resulting Cursor with the original ContentValues
        TestUtilities.validateCurrentRecord("Error: Movie Query Validation Failed", cursor, movieValues);


        // Move the cursor to demonstrate that there is only one record in the database
        assertFalse("Error: More than one record returned from location query",
                cursor.moveToNext());

        // Finally, close the cursor and database
        cursor.close();
        db.close();


    }
}
