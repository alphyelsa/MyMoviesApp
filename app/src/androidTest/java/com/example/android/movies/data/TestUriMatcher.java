package com.example.android.movies.data;

import android.content.UriMatcher;
import android.net.Uri;
import android.test.AndroidTestCase;

import com.example.android.movies.data.persistence.MovieContract;
import com.example.android.movies.data.persistence.MovieProvider;

/**
 * Created by aesebast on 10/30/2016.
 */

public class TestUriMatcher extends AndroidTestCase {
    private static final String TEST_MOVIE_ID = "188927";

    // content://com.example.android.movies/movie"
    private static final Uri TEST_MOVIE_DIR = MovieContract.MovieEntry.CONTENT_URI;
    private static final Uri TEST_MOVIE_WITH_ID = MovieContract.MovieEntry.buildMovieUri(TEST_MOVIE_ID);

    /*
        This function tests that the UriMatcher returns the correct integer value
        for each of the Uri types that our ContentProvider can handle.
     */
    public void testUriMatcher() {
        UriMatcher testMatcher = MovieProvider.buildUriMatcher();

        assertEquals("Error: The All MovieS URI was matched incorrectly.",
                testMatcher.match(TEST_MOVIE_DIR), MovieProvider.MOVIE);
        assertEquals("Error: The Movied Id URI was matched incorrectly.",
                testMatcher.match(TEST_MOVIE_WITH_ID), MovieProvider.MOVIE_WITH_ID);
    }
}
