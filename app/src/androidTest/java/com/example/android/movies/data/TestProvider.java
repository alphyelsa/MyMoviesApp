package com.example.android.movies.data;

import android.content.ComponentName;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.test.AndroidTestCase;

import com.example.android.movies.data.persistence.MovieContract;
import com.example.android.movies.data.persistence.MovieDBHelper;
import com.example.android.movies.data.persistence.MovieProvider;

/**
 * Created by aesebast on 10/30/2016.
 */

public class TestProvider extends AndroidTestCase {

    public static final String LOG_TAG = TestProvider.class.getSimpleName();
    private static final String TEST_MOVIE_ID = "188927";

    /*
       This helper function deletes all records from both database tables using the ContentProvider.
       It also queries the ContentProvider to make sure that the database has been successfully
       deleted, so it cannot be used until the Query and Delete functions have been written
       in the ContentProvider.
     */
    public void deleteAllRecordsFromProvider() {
        mContext.getContentResolver().delete(
                MovieContract.MovieEntry.CONTENT_URI,
                null,
                null
        );

        Cursor cursor = mContext.getContentResolver().query(
                MovieContract.MovieEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );
        assertEquals("Error: Records not deleted from Movie table during delete", 0, cursor.getCount());
        cursor.close();

        mContext.getContentResolver().delete(
                MovieContract.TrailerEntry.CONTENT_URI,
                null,
                null
        );

        cursor = mContext.getContentResolver().query(
                MovieContract.TrailerEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );
        assertEquals("Error: Records not deleted from Trailer table during delete", 0, cursor.getCount());
        cursor.close();
    }

    /*
       This helper function deletes all records from both database tables using the database
       functions only.  This is designed to be used to reset the state of the database until the
       delete functionality is available in the ContentProvider.
     */
    public void deleteAllRecordsFromDB() {
        MovieDBHelper dbHelper = new MovieDBHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        db.delete(MovieContract.MovieEntry.TABLE_NAME, null, null);
        db.delete(MovieContract.TrailerEntry.TABLE_NAME, null, null);
        db.close();
    }

    public void deleteAllRecords() {

        //deleteAllRecordsFromDB();
        deleteAllRecordsFromProvider();
    }

    // Since we want each test to start with a clean slate, run deleteAllRecords
    // in setUp (called by the test runner before each test).
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        deleteAllRecords();
    }

    /*
        This test checks to make sure that the content provider is registered correctly.
     */
    public void testProviderRegistry() {
        PackageManager pm = mContext.getPackageManager();

        // We define the component name based on the package name from the context and the
        // MovieProvider class.
        ComponentName componentName = new ComponentName(mContext.getPackageName(),
                MovieProvider.class.getName());
        try {
            // Fetch the provider info using the component name from the PackageManager
            // This throws an exception if the provider isn't registered.
            ProviderInfo providerInfo = pm.getProviderInfo(componentName, 0);

            // Make sure that the registered authority matches the authority from the Contract.
            assertEquals("Error: MovieProvider registered with authority: " + providerInfo.authority +
                            " instead of authority: " + MovieContract.CONTENT_AUTHORITY,
                    providerInfo.authority, MovieContract.CONTENT_AUTHORITY);
        } catch (PackageManager.NameNotFoundException e) {
            // I guess the provider isn't registered correctly.
            assertTrue("Error: MovieProvider not registered at " + mContext.getPackageName(),
                    false);
        }
    }

    /*
            This test doesn't touch the database.  It verifies that the ContentProvider returns
            the correct type for each type of URI that it can handle.
         */
    public void testGetType() {
        // content://com.example.android.movies/movie/
        String type = mContext.getContentResolver().getType(MovieContract.MovieEntry.CONTENT_URI);
        // vnd.android.cursor.dir/com.example.android.movies/movie
        assertEquals("Error: the MovieEntry CONTENT_URI should return MovieEntry.CONTENT_TYPE",
                MovieContract.MovieEntry.CONTENT_TYPE, type);

        // content://com.example.android.movies/movie/188927
        type = mContext.getContentResolver().getType(
                MovieContract.MovieEntry.buildMovieUri(TEST_MOVIE_ID));
        // vnd.android.cursor.dir/com.example.android.movies/movie/188927
        assertEquals("Error: the MovieEntry CONTENT_URI with location should return MovieEntry.CONTENT_TYPE",
                MovieContract.MovieEntry.CONTENT_TYPE, type);
    }


    /*
        This test uses the database directly to insert and then uses the ContentProvider to
        read out the data.
     */
    public void testBasicQuery() {
        // insert our test records into the database
        MovieDBHelper dbHelper = new MovieDBHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        //Add movie to DB
        ContentValues movieValues = TestUtilities.createMovieValues();
        long movieRowId = db.insert(MovieContract.MovieEntry.TABLE_NAME, null, movieValues);
        assertTrue("Unable to Insert MovieEntry into the Database", movieRowId != -1);

        //Add trailer to DB
        ContentValues trailerValues = TestUtilities.createTrailerValues();
        long trailerRowId = db.insert(MovieContract.TrailerEntry.TABLE_NAME,null,trailerValues);
        assertTrue("Unable to Insert TrailerEntry into Database", trailerRowId != -1);
        db.close();

        // Test the basic content provider query
        Cursor movieCursor = mContext.getContentResolver().query(
                MovieContract.MovieEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );

        // Make sure we get the correct cursor out of the database
        TestUtilities.validateCursor("testBasicMovieQuery", movieCursor, movieValues);

        Cursor trailerCursor = mContext.getContentResolver().query(
                MovieContract.TrailerEntry.CONTENT_URI,  // Table to Query
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null // columns to group by
        );
        TestUtilities.validateCursor("testBasicQueryForTrailer",
                trailerCursor, trailerValues);
        trailerCursor.close();

        // Get the specific movie
        movieCursor = mContext.getContentResolver().query(
                MovieContract.MovieEntry.buildMovieUri(movieValues.getAsString(MovieContract.MovieEntry.COLUMN_MOVIE_ID)),
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null  // sort order
        );
        TestUtilities.validateCursor("testBasicQueryForMovieWithId",
                movieCursor, movieValues);
        movieCursor.close();

        //Get trailer for specific movie
        trailerCursor = mContext.getContentResolver().query(
                MovieContract.TrailerEntry.buildTrailerUri(trailerValues.getAsString(MovieContract.TrailerEntry.COLUMN_MOVIE_ID)),
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null  // sort order
        );
        TestUtilities.validateCursor("testBasicQueryForTrailerWithId",
                trailerCursor, trailerValues);

        trailerCursor.close();

        movieCursor.close();
    }

    // Make sure we can still delete after adding/updating stuff
    public void testInsertReadProvider() {
        ContentValues movieValues = TestUtilities.createMovieValues();
        ContentValues trailerValues = TestUtilities.createTrailerValues();

        // Register a content observer for our insert.  This time, directly with the content resolver
        TestUtilities.TestContentObserver tco = TestUtilities.getTestContentObserver();
        mContext.getContentResolver().registerContentObserver(MovieContract.MovieEntry.CONTENT_URI, true, tco);
        Uri movieUri = mContext.getContentResolver().insert(MovieContract.MovieEntry.CONTENT_URI, movieValues);

        // Did our content observer get called? If this fails, your insert movie
        // isn't calling getContext().getContentResolver().notifyChange(uri, null);
        tco.waitForNotificationOrFail();
        mContext.getContentResolver().unregisterContentObserver(tco);

        long movieRowId = ContentUris.parseId(movieUri);

        // Verify we got a row back.
        assertTrue(movieRowId != -1);

        // Data's inserted.  IN THEORY.  Now pull some out to stare at it and verify it made
        // the round trip.

        // A cursor is your primary interface to the query results.
        Cursor cursor = mContext.getContentResolver().query(
                MovieContract.MovieEntry.CONTENT_URI,
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null  // sort order
        );

        TestUtilities.validateCursor("testInsertReadProvider. Error validating MovieEntry.",
                cursor, movieValues);

        //Fantastic.  Add trailers
        // The TestContentObserver is a one-shot class
        tco = TestUtilities.getTestContentObserver();

        mContext.getContentResolver().registerContentObserver(MovieContract.TrailerEntry.CONTENT_URI, true, tco);

        Uri trailerInsertUri = mContext.getContentResolver()
                .insert(MovieContract.TrailerEntry.CONTENT_URI, trailerValues);
        assertTrue(trailerInsertUri != null);

        // Did our content observer get called?  If this fails, your insert trailer
        // in your ContentProvider isn't calling getContext().getContentResolver().notifyChange(uri, null);
        tco.waitForNotificationOrFail();
        mContext.getContentResolver().unregisterContentObserver(tco);

        // A cursor is your primary interface to the query results.
        Cursor movieCursor = mContext.getContentResolver().query(
                MovieContract.MovieEntry.CONTENT_URI,  // Table to Query
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null // columns to group by
        );

        TestUtilities.validateCursor("testInsertReadProvider. Error validating MovieEntry insert.",
                movieCursor, movieValues);
        movieCursor.close();

        Cursor trailerCursor = mContext.getContentResolver().query(
                MovieContract.TrailerEntry.CONTENT_URI,  // Table to Query
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null // columns to group by
        );
        TestUtilities.validateCursor("testInsertReadProvider. Error validating TrailerEntry insert.",
                trailerCursor, trailerValues);
        trailerCursor.close();

        // Get the specific movie
        movieCursor = mContext.getContentResolver().query(
                MovieContract.MovieEntry.buildMovieUri(movieValues.getAsString(MovieContract.MovieEntry.COLUMN_MOVIE_ID)),
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null  // sort order
        );
        TestUtilities.validateCursor("testInsertReadProvider.  Error validating Movie Entry for a particular movie",
                movieCursor, movieValues);
        movieCursor.close();

        //Get trailer for specific movie
        trailerCursor = mContext.getContentResolver().query(
                MovieContract.TrailerEntry.buildTrailerUri(trailerValues.getAsString(MovieContract.TrailerEntry.COLUMN_MOVIE_ID)),
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null  // sort order
        );
        TestUtilities.validateCursor("testInsertReadProvider. Error validating TrailerEntry insert.",
                trailerCursor, trailerValues);

        trailerCursor.close();
    }

    // Make sure we can still delete after adding/updating stuff
    public void testDeleteRecords() {
        testInsertReadProvider();

        // Register a content observer for our movie delete.
        TestUtilities.TestContentObserver movieObserver = TestUtilities.getTestContentObserver();
        mContext.getContentResolver().registerContentObserver(MovieContract.MovieEntry.CONTENT_URI, true, movieObserver);

        // Register a content observer for our trailer delete.
        TestUtilities.TestContentObserver trailerObserver = TestUtilities.getTestContentObserver();
        mContext.getContentResolver().registerContentObserver(MovieContract.TrailerEntry.CONTENT_URI, true, trailerObserver);

        deleteAllRecordsFromProvider();

        // If either of these fail, you most-likely are not calling the
        // getContext().getContentResolver().notifyChange(uri, null); in the ContentProvider
        // delete.  (only if the insertReadProvider is succeeding)
        movieObserver.waitForNotificationOrFail();
        trailerObserver.waitForNotificationOrFail();

        mContext.getContentResolver().unregisterContentObserver(movieObserver);
        mContext.getContentResolver().unregisterContentObserver(trailerObserver);
    }


    static private final int BULK_INSERT_RECORDS_TO_INSERT = 10;

    static ContentValues[] createBulkInsertMovieValues() {
        ContentValues[] returnContentValues = new ContentValues[BULK_INSERT_RECORDS_TO_INSERT];

        ContentValues movieValues = new ContentValues();
        movieValues.put(MovieContract.MovieEntry.COLUMN_MOVIE_ID, "188927");
        movieValues.put(MovieContract.MovieEntry.COLUMN_MOVIE_TITLE, "Star Trek Beyond");
        movieValues.put(MovieContract.MovieEntry.COLUMN_MOVIE_POSTER, "http://image.tmdb.org/t/p/w185/mLrQMqyZgLeP8FrT5LCobKAiqmK.jpg");
        movieValues.put(MovieContract.MovieEntry.COLUMN_PLOT_SYNOPSIS, "The USS Enterprise crew explores the furthest reaches of uncharted space, where they encounter a mysterious new enemy who puts them and everything the Federation stands for to the test.");
        movieValues.put(MovieContract.MovieEntry.COLUMN_RELEASE_YEAR, "2016");
        movieValues.put(MovieContract.MovieEntry.COLUMN_USER_RATING, "6.33");
        movieValues.put(MovieContract.MovieEntry.COLUMN_LENGTH, "122");
        returnContentValues[0] = movieValues;

        movieValues = new ContentValues();
        movieValues.put(MovieContract.MovieEntry.COLUMN_MOVIE_ID, "188927");
        movieValues.put(MovieContract.MovieEntry.COLUMN_MOVIE_TITLE, "Star Trek Beyond");
        movieValues.put(MovieContract.MovieEntry.COLUMN_MOVIE_POSTER, "http://image.tmdb.org/t/p/w185/mLrQMqyZgLeP8FrT5LCobKAiqmK.jpg");
        movieValues.put(MovieContract.MovieEntry.COLUMN_PLOT_SYNOPSIS, "The USS Enterprise crew explores the furthest reaches of uncharted space, where they encounter a mysterious new enemy who puts them and everything the Federation stands for to the test.");
        movieValues.put(MovieContract.MovieEntry.COLUMN_RELEASE_YEAR, "2016");
        movieValues.put(MovieContract.MovieEntry.COLUMN_USER_RATING, "6.33");
        movieValues.put(MovieContract.MovieEntry.COLUMN_LENGTH, "122");
        returnContentValues[1] = movieValues;

        movieValues = new ContentValues();
        movieValues.put(MovieContract.MovieEntry.COLUMN_MOVIE_ID, "188927");
        movieValues.put(MovieContract.MovieEntry.COLUMN_MOVIE_TITLE, "Star Trek Beyond");
        movieValues.put(MovieContract.MovieEntry.COLUMN_MOVIE_POSTER, "http://image.tmdb.org/t/p/w185/mLrQMqyZgLeP8FrT5LCobKAiqmK.jpg");
        movieValues.put(MovieContract.MovieEntry.COLUMN_PLOT_SYNOPSIS, "The USS Enterprise crew explores the furthest reaches of uncharted space, where they encounter a mysterious new enemy who puts them and everything the Federation stands for to the test.");
        movieValues.put(MovieContract.MovieEntry.COLUMN_RELEASE_YEAR, "2016");
        movieValues.put(MovieContract.MovieEntry.COLUMN_USER_RATING, "6.33");
        movieValues.put(MovieContract.MovieEntry.COLUMN_LENGTH, "122");
        returnContentValues[2] = movieValues;

        movieValues = new ContentValues();
        movieValues.put(MovieContract.MovieEntry.COLUMN_MOVIE_ID, "188927");
        movieValues.put(MovieContract.MovieEntry.COLUMN_MOVIE_TITLE, "Star Trek Beyond");
        movieValues.put(MovieContract.MovieEntry.COLUMN_MOVIE_POSTER, "http://image.tmdb.org/t/p/w185/mLrQMqyZgLeP8FrT5LCobKAiqmK.jpg");
        movieValues.put(MovieContract.MovieEntry.COLUMN_PLOT_SYNOPSIS, "The USS Enterprise crew explores the furthest reaches of uncharted space, where they encounter a mysterious new enemy who puts them and everything the Federation stands for to the test.");
        movieValues.put(MovieContract.MovieEntry.COLUMN_RELEASE_YEAR, "2016");
        movieValues.put(MovieContract.MovieEntry.COLUMN_USER_RATING, "6.33");
        movieValues.put(MovieContract.MovieEntry.COLUMN_LENGTH, "122");
        returnContentValues[3] = movieValues;

        movieValues = new ContentValues();
        movieValues.put(MovieContract.MovieEntry.COLUMN_MOVIE_ID, "188927");
        movieValues.put(MovieContract.MovieEntry.COLUMN_MOVIE_TITLE, "Star Trek Beyond");
        movieValues.put(MovieContract.MovieEntry.COLUMN_MOVIE_POSTER, "http://image.tmdb.org/t/p/w185/mLrQMqyZgLeP8FrT5LCobKAiqmK.jpg");
        movieValues.put(MovieContract.MovieEntry.COLUMN_PLOT_SYNOPSIS, "The USS Enterprise crew explores the furthest reaches of uncharted space, where they encounter a mysterious new enemy who puts them and everything the Federation stands for to the test.");
        movieValues.put(MovieContract.MovieEntry.COLUMN_RELEASE_YEAR, "2016");
        movieValues.put(MovieContract.MovieEntry.COLUMN_USER_RATING, "6.33");
        movieValues.put(MovieContract.MovieEntry.COLUMN_LENGTH, "122");
        returnContentValues[4] = movieValues;

        movieValues = new ContentValues();
        movieValues.put(MovieContract.MovieEntry.COLUMN_MOVIE_ID, "188927");
        movieValues.put(MovieContract.MovieEntry.COLUMN_MOVIE_TITLE, "Star Trek Beyond");
        movieValues.put(MovieContract.MovieEntry.COLUMN_MOVIE_POSTER, "http://image.tmdb.org/t/p/w185/mLrQMqyZgLeP8FrT5LCobKAiqmK.jpg");
        movieValues.put(MovieContract.MovieEntry.COLUMN_PLOT_SYNOPSIS, "The USS Enterprise crew explores the furthest reaches of uncharted space, where they encounter a mysterious new enemy who puts them and everything the Federation stands for to the test.");
        movieValues.put(MovieContract.MovieEntry.COLUMN_RELEASE_YEAR, "2016");
        movieValues.put(MovieContract.MovieEntry.COLUMN_USER_RATING, "6.33");
        movieValues.put(MovieContract.MovieEntry.COLUMN_LENGTH, "122");
        returnContentValues[5] = movieValues;

        movieValues = new ContentValues();
        movieValues.put(MovieContract.MovieEntry.COLUMN_MOVIE_ID, "188927");
        movieValues.put(MovieContract.MovieEntry.COLUMN_MOVIE_TITLE, "Star Trek Beyond");
        movieValues.put(MovieContract.MovieEntry.COLUMN_MOVIE_POSTER, "http://image.tmdb.org/t/p/w185/mLrQMqyZgLeP8FrT5LCobKAiqmK.jpg");
        movieValues.put(MovieContract.MovieEntry.COLUMN_PLOT_SYNOPSIS, "The USS Enterprise crew explores the furthest reaches of uncharted space, where they encounter a mysterious new enemy who puts them and everything the Federation stands for to the test.");
        movieValues.put(MovieContract.MovieEntry.COLUMN_RELEASE_YEAR, "2016");
        movieValues.put(MovieContract.MovieEntry.COLUMN_USER_RATING, "6.33");
        movieValues.put(MovieContract.MovieEntry.COLUMN_LENGTH, "122");
        returnContentValues[6] = movieValues;

        movieValues = new ContentValues();
        movieValues.put(MovieContract.MovieEntry.COLUMN_MOVIE_ID, "188927");
        movieValues.put(MovieContract.MovieEntry.COLUMN_MOVIE_TITLE, "Star Trek Beyond");
        movieValues.put(MovieContract.MovieEntry.COLUMN_MOVIE_POSTER, "http://image.tmdb.org/t/p/w185/mLrQMqyZgLeP8FrT5LCobKAiqmK.jpg");
        movieValues.put(MovieContract.MovieEntry.COLUMN_PLOT_SYNOPSIS, "The USS Enterprise crew explores the furthest reaches of uncharted space, where they encounter a mysterious new enemy who puts them and everything the Federation stands for to the test.");
        movieValues.put(MovieContract.MovieEntry.COLUMN_RELEASE_YEAR, "2016");
        movieValues.put(MovieContract.MovieEntry.COLUMN_USER_RATING, "6.33");
        movieValues.put(MovieContract.MovieEntry.COLUMN_LENGTH, "122");
        returnContentValues[7] = movieValues;

        movieValues = new ContentValues();
        movieValues.put(MovieContract.MovieEntry.COLUMN_MOVIE_ID, "188927");
        movieValues.put(MovieContract.MovieEntry.COLUMN_MOVIE_TITLE, "Star Trek Beyond");
        movieValues.put(MovieContract.MovieEntry.COLUMN_MOVIE_POSTER, "http://image.tmdb.org/t/p/w185/mLrQMqyZgLeP8FrT5LCobKAiqmK.jpg");
        movieValues.put(MovieContract.MovieEntry.COLUMN_PLOT_SYNOPSIS, "The USS Enterprise crew explores the furthest reaches of uncharted space, where they encounter a mysterious new enemy who puts them and everything the Federation stands for to the test.");
        movieValues.put(MovieContract.MovieEntry.COLUMN_RELEASE_YEAR, "2016");
        movieValues.put(MovieContract.MovieEntry.COLUMN_USER_RATING, "6.33");
        movieValues.put(MovieContract.MovieEntry.COLUMN_LENGTH, "122");
        returnContentValues[8] = movieValues;

        movieValues = new ContentValues();
        movieValues.put(MovieContract.MovieEntry.COLUMN_MOVIE_ID, "188927");
        movieValues.put(MovieContract.MovieEntry.COLUMN_MOVIE_TITLE, "Star Trek Beyond");
        movieValues.put(MovieContract.MovieEntry.COLUMN_MOVIE_POSTER, "http://image.tmdb.org/t/p/w185/mLrQMqyZgLeP8FrT5LCobKAiqmK.jpg");
        movieValues.put(MovieContract.MovieEntry.COLUMN_PLOT_SYNOPSIS, "The USS Enterprise crew explores the furthest reaches of uncharted space, where they encounter a mysterious new enemy who puts them and everything the Federation stands for to the test.");
        movieValues.put(MovieContract.MovieEntry.COLUMN_RELEASE_YEAR, "2016");
        movieValues.put(MovieContract.MovieEntry.COLUMN_USER_RATING, "6.33");
        movieValues.put(MovieContract.MovieEntry.COLUMN_LENGTH, "122");
        returnContentValues[9] = movieValues;

        return returnContentValues;
    }

    public void testBulkInsert() {
        deleteAllRecordsFromProvider();

        // Now we can bulkInsert some movies.
        // With ContentProviders, you really only have to implement the features you
        // use, after all.
        ContentValues[] bulkInsertContentValues = createBulkInsertMovieValues();

        // Register a content observer for our bulk insert.
        TestUtilities.TestContentObserver movieObserver = TestUtilities.getTestContentObserver();
        mContext.getContentResolver().registerContentObserver(MovieContract.MovieEntry.CONTENT_URI, true, movieObserver);

        int insertCount = mContext.getContentResolver().bulkInsert(MovieContract.MovieEntry.CONTENT_URI, bulkInsertContentValues);

        // If this fails, it means that you most-likely are not calling the
        // getContext().getContentResolver().notifyChange(uri, null); in your BulkInsert
        // ContentProvider method.
        movieObserver.waitForNotificationOrFail();
        mContext.getContentResolver().unregisterContentObserver(movieObserver);

        assertEquals(insertCount, BULK_INSERT_RECORDS_TO_INSERT);

        // A cursor is your primary interface to the query results.
        Cursor cursor = mContext.getContentResolver().query(
                MovieContract.MovieEntry.CONTENT_URI,
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null  // sort order
        );

        // we should have as many records in the database as we've inserted
        assertEquals(cursor.getCount(), BULK_INSERT_RECORDS_TO_INSERT);

        // and let's make sure they match the ones we created
        cursor.moveToFirst();
        for ( int i = 0; i < BULK_INSERT_RECORDS_TO_INSERT; i++, cursor.moveToNext() ) {
            TestUtilities.validateCurrentRecord("testBulkInsert.  Error validating MovieEntry " + i,
                    cursor, bulkInsertContentValues[i]);
        }
        cursor.close();
    }
}
