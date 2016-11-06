package com.example.android.movies.data;

import android.net.Uri;
import android.test.AndroidTestCase;

import com.example.android.movies.data.persistence.MovieContract;

/**
 * Created by aesebast on 10/30/2016.
 */

public class TestMovieContract extends AndroidTestCase {

    // intentionally includes a slash to make sure Uri is getting quoted correctly
    private static final String TEST_MOVIE_ID = "/188927";

    /*
        Test the movie id function
     */
    public void testBuildWeatherLocation() {
        Uri movieUri = MovieContract.MovieEntry.buildMovieUri(TEST_MOVIE_ID);
        assertNotNull("Error: Null Uri returned.  You must fill-in buildMovieUri in " +
                        "MovieContract.",movieUri);
        assertEquals("Error: Movie Id not properly appended to the end of the Uri",
                TEST_MOVIE_ID, movieUri.getLastPathSegment());
        assertEquals("Error: Movie Id uri doesn't match our expected result",
                movieUri.toString(),
                "content://com.example.android.movies/movie/%2F188927");
    }
}
