package com.example.android.movies.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.test.AndroidTestCase;

import com.example.android.movies.data.persistence.MovieContract;
import com.example.android.movies.data.persistence.MovieDBHelper;
import com.example.android.movies.utils.PollingCheck;

import java.util.Map;
import java.util.Set;

/**
 * Created by aesebast on 10/30/2016.
 */

public class TestUtilities extends AndroidTestCase {

    static void validateCursor(String error, Cursor valueCursor, ContentValues expectedValues) {
        assertTrue("Empty cursor returned. " + error, valueCursor.moveToFirst());
        validateCurrentRecord(error, valueCursor, expectedValues);
    }

    static void validateCurrentRecord(String error, Cursor valueCursor, ContentValues expectedValues) {
        Set<Map.Entry<String, Object>> valueSet = expectedValues.valueSet();
        for (Map.Entry<String, Object> entry : valueSet) {
            String columnName = entry.getKey();
            int idx = valueCursor.getColumnIndex(columnName);
            assertFalse("Column '" + columnName + "' not found. " + error, idx == -1);
            String expectedValue = entry.getValue().toString();
            assertEquals("Value '" + entry.getValue().toString() +
                    "' did not match the expected value '" +
                    expectedValue + "'. " + error, expectedValue, valueCursor.getString(idx));
        }
    }

    /*
        Create some default movie values for your database tests.
     */
    static ContentValues createMovieValues() {
        ContentValues movieValues = new ContentValues();
        movieValues.put(MovieContract.MovieEntry.COLUMN_MOVIE_ID, "188927");
        movieValues.put(MovieContract.MovieEntry.COLUMN_MOVIE_TITLE,"Star Trek Beyond");
        movieValues.put(MovieContract.MovieEntry.COLUMN_MOVIE_POSTER, "http://image.tmdb.org/t/p/w185/mLrQMqyZgLeP8FrT5LCobKAiqmK.jpg");
        movieValues.put(MovieContract.MovieEntry.COLUMN_PLOT_SYNOPSIS, "The USS Enterprise crew explores the furthest reaches of uncharted space, where they encounter a mysterious new enemy who puts them and everything the Federation stands for to the test.");
        movieValues.put(MovieContract.MovieEntry.COLUMN_RELEASE_YEAR, "2016");
        movieValues.put(MovieContract.MovieEntry.COLUMN_USER_RATING, "6.33");
        movieValues.put(MovieContract.MovieEntry.COLUMN_LENGTH, "122");

        return movieValues;
    }

      /*
          Create Trailer Entries
       */
    static ContentValues createTrailerValues() {
        // Create a new map of values, where column names are the keys
        ContentValues testValues = new ContentValues();
        testValues.put(MovieContract.TrailerEntry.COLUMN_MOVIE_ID, "188927");
        testValues.put(MovieContract.TrailerEntry.COLUMN_VIDEO_URL, "https://www.youtube.com/watch?v=dCyv5xKIqlw");
        testValues.put(MovieContract.TrailerEntry.COLUMN_THUMBNAIL_URL, "http://img.youtube.com/vi/dCyv5xKIqlw/default.jpg");

        return testValues;
    }

    /*
        Creating movie and trailer records
     */
    static void insertMovieAndTrailerRecords(Context context) {
        // insert our test records into the database
        MovieDBHelper dbHelper = new MovieDBHelper(context);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues movieValues = TestUtilities.createMovieValues();
        ContentValues trailerValues = TestUtilities.createTrailerValues();

        long locationRowId;
        locationRowId = db.insert(MovieContract.MovieEntry.TABLE_NAME, null, movieValues);
        // Verify we got a row back.
        assertTrue("Error: Failure to insert records into movie table", locationRowId != -1);

        locationRowId = db.insert(MovieContract.TrailerEntry.TABLE_NAME, null, trailerValues);
        // Verify we got a row back.
        assertTrue("Error: Failure to insert trailer records", locationRowId != -1);

    }

    /*
        The functions we provide inside of TestProvider use this utility class to test
        the ContentObserver callbacks using the PollingCheck class that we grabbed from the Android
        CTS tests.
        Note that this only tests that the onChange function is called; it does not test that the
        correct Uri is returned.
     */
    static class TestContentObserver extends ContentObserver {
        final HandlerThread mHT;
        boolean mContentChanged;

        static TestContentObserver getTestContentObserver() {
            HandlerThread ht = new HandlerThread("ContentObserverThread");
            ht.start();
            return new TestContentObserver(ht);
        }

        private TestContentObserver(HandlerThread ht) {
            super(new Handler(ht.getLooper()));
            mHT = ht;
        }

        // On earlier versions of Android, this onChange method is called
        @Override
        public void onChange(boolean selfChange) {
            onChange(selfChange, null);
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            mContentChanged = true;
        }

        public void waitForNotificationOrFail() {
            // Note: The PollingCheck class is taken from the Android CTS (Compatibility Test Suite).
            // It's useful to look at the Android CTS source for ideas on how to test your Android
            // applications.  The reason that PollingCheck works is that, by default, the JUnit
            // testing framework is not running on the main Android application thread.
            new PollingCheck(5000) {
                @Override
                protected boolean check() {
                    return mContentChanged;
                }
            }.run();
            mHT.quit();
        }
    }

    static TestContentObserver getTestContentObserver() {
        return TestContentObserver.getTestContentObserver();
    }
}
