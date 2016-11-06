package com.example.android.movies.data.persistence;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by aesebast on 10/30/2016.
 */

public class MovieContract {

    // The "Content authority" is a name for the entire content provider, similar to the
    // relationship between a domain name and its website.  A convenient string to use for the
    // content authority is the package name for the app, which is guaranteed to be unique on the
    // device.
    public static final String CONTENT_AUTHORITY = "com.example.android.movies";

    // Use CONTENT_AUTHORITY to create the base of all URI's which apps will use to contact
    // the content provider.
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    // Possible paths (appended to base content URI for possible URI's)
    public static final String PATH_MOVIE = "movie";
    public static final String PATH_TRAILER = "trailer";

    /* Inner class that defines the table contents of the Movie table */
    public static final class MovieEntry implements BaseColumns {

        public static final String TABLE_NAME = "movie";

        // Column storing the id of the movie
        public static final String COLUMN_MOVIE_ID = "movie_id";
        // Title of the movie
        public static final String COLUMN_MOVIE_TITLE = "movie_title";
        // poster as returned by API, to identify the icon to be used
        public static final String COLUMN_MOVIE_POSTER = "movie_poster";
        // Plot Synopsis
        public static final String COLUMN_PLOT_SYNOPSIS = "plot";
        // User rating
        public static final String COLUMN_USER_RATING = "user_rating";
        // Release Year
        public static final String COLUMN_RELEASE_YEAR = "release_year";
        //Length
        public static final String COLUMN_LENGTH = "length";

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_MOVIE).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_MOVIE;

        public static Uri buildMovieUri(String id) {
            return CONTENT_URI.buildUpon().appendPath(id).build();
        }

        public static String getMovieIdFromUri(Uri uri) {
            return uri.getPathSegments().get(1);
        }
    }

    /*
        Inner class that defines the table contents of the Trailer table
     */
    public static final class TrailerEntry implements BaseColumns {

        public static final String TABLE_NAME = "trailer";
        // Column storing the id of the movie
        public static final String COLUMN_MOVIE_ID = "movie_id";
        //Video URL
        public static final String COLUMN_VIDEO_URL = "video_url";
        //ThumbNail URL
        public static final String COLUMN_THUMBNAIL_URL = "thumbnail_url";

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_TRAILER).build();
        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_TRAILER;

        public static Uri buildTrailerUri(String id) {
            return CONTENT_URI.buildUpon().appendPath(id).build();
        }

        public static String getMovieIdFromUri(Uri uri) {
            return uri.getPathSegments().get(1);
        }

    }
}
