package com.example.android.movies.network;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.TextView;

import com.example.android.movies.BuildConfig;
import com.example.android.movies.R;
import com.example.android.movies.data.model.Movie;
import com.example.android.movies.data.model.Trailer;
import com.example.android.movies.data.adapter.TrailerAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by aesebast on 10/29/2016.
 */
public class FetchDetailTask extends AsyncTask<String, Void, String[]> {

    private final String LOG_TAG = FetchDetailTask.class.getSimpleName();
    Movie mMovie;
    Context mContext;
    TrailerAdapter mTrailerAdapter;

    public FetchDetailTask(Context context, Movie movie, TrailerAdapter trailerAdapter) {
        mMovie = movie;
        mContext = context;
        mTrailerAdapter = trailerAdapter;
    }

    /**
     * Take the String representing the list of movies in JSON Format and
     * pull out the data.
     * Creates a movie object and invokes the toString() method of the object.
     */
    private void getMovieFromJson(String movieJsonStr)
            throws JSONException {

        if (mMovie == null) {
            Log.e(LOG_TAG, "The movie record is null. Hence not parsing the JSON");
            return;
        }

        // These are the names of the JSON objects that need to be extracted.
        final String MOVIE_LENGTH = "runtime";
        final String MOVIE_VIDEOS = "videos";
        final String MOVIE_VIDEO_RESULTS = "results";
        final String VIDEO_TYPE = "type";
        final String VIDEO_TYPE_TRAILER = "Trailer";
        final String VIDEO_ID = "id";
        final String VIDEO_KEY = "key";
        final String VIDEO_SITE = "site";

        JSONObject movieJson = new JSONObject(movieJsonStr);
        mMovie.setLength(movieJson.getString(MOVIE_LENGTH));
        String id = movieJson.getString(VIDEO_ID);
        JSONArray videoArray = movieJson.getJSONObject(MOVIE_VIDEOS).getJSONArray(MOVIE_VIDEO_RESULTS);
        ArrayList<Trailer> trailers = new ArrayList<Trailer>();
        for (int i = 0; i < videoArray.length(); i++) {
            // Get the JSON object representing the movie
            JSONObject eachVideo = videoArray.getJSONObject(i);
            String type = eachVideo.getString(VIDEO_TYPE);
            if (VIDEO_TYPE_TRAILER.equals(type)) {
                String key = eachVideo.getString(VIDEO_KEY);
                String site = eachVideo.getString(VIDEO_SITE);
                trailers.add(new Trailer(id, key, site));
            }
        }
        if (trailers != null & trailers.size() != 0) {
            mMovie.setTrailers(trailers);
        }
    }

    @Override
    protected String[] doInBackground(String... params) {

        Log.v(LOG_TAG, "doInBackground() -> Start");

        if (params.length == 0) {
            return null;
        }

        // These two need to be declared outside the try/catch
        // so that they can be closed in the finally block.
        HttpsURLConnection urlConnection = null;
        BufferedReader reader = null;
        StringBuffer buffer = new StringBuffer();

        // Will contain the raw JSON response as a string.
        String movieJsonStr = null;

        // Construct the URL for the MovieDB query query
        final String MOVIEDB_BASE_URL = "https://api.themoviedb.org/3/movie/" + params[0] + "?";
        final String APIKEY_PARAM = "api_key";
        final String VIDEOKEY_PARAM = "append_to_response";

        Log.v(LOG_TAG, "Building the URL");
        Uri builtUri = Uri.parse(MOVIEDB_BASE_URL).buildUpon().appendQueryParameter(APIKEY_PARAM, BuildConfig.MOVIEDB_API_KEY).appendQueryParameter(VIDEOKEY_PARAM, "videos").build();

        URL url = null;

        try {
            url = new URL(builtUri.toString());
        } catch (MalformedURLException me) {
            Log.e(LOG_TAG, "Caught Malformed Exception with url " + builtUri.toString());
            return null;
        }
        if (url != null) {
            // Create the request to MovieDB, and open the connection
            Log.v(LOG_TAG, "Connecting to the URL: " + url.toString());
            if (url != null) {
                try {
                    urlConnection = (HttpsURLConnection) url.openConnection();
                } catch (IOException ioe) {
                    Log.e(LOG_TAG, "Encountered IOException while trying to open connection to " + url.toString());
                    return null;
                }
            }
            if (urlConnection != null) {

                try {
                    urlConnection.setRequestMethod("GET");
                    urlConnection.connect();
                    Log.v(LOG_TAG, "Connection established");
                    int status = urlConnection.getResponseCode();
                    if (status != 200) {
                        Log.e(LOG_TAG, "Error while establishing connection to " + url.toString());
                        Log.e(LOG_TAG, "Status: " + status);
                        return null;
                    }
                } catch (ProtocolException pe) {
                    Log.e(LOG_TAG, "Encountered ProtocolException while trying to set GET as Request Method");
                    return null;
                } catch (IOException ioe) {
                    Log.e(LOG_TAG, "Encountered IOException while trying to connect to URL: " + url.toString());
                    return null;
                }


                // Read the input stream into a String
                try {
                    InputStream inputStream = urlConnection.getInputStream();
                    if (inputStream == null) {
                        Log.v(LOG_TAG, "Input Stream returned null");
                        return null;
                    }
                    reader = new BufferedReader(new InputStreamReader(inputStream));
                    Log.v(LOG_TAG, "Reading the JSON");
                    String line;
                    while ((line = reader.readLine()) != null) {
                        buffer.append(line + "\n");
                    }

                    if (buffer.length() == 0) {
                        Log.e(LOG_TAG, "Empty JSON String");
                        return null;
                    }
                    String moviesJsonStr = buffer.toString();

                    Log.d(LOG_TAG, "The JSON Fetched: " + moviesJsonStr);

                    try {
                        getMovieFromJson(moviesJsonStr);
                    } catch (JSONException jse) {
                        Log.e(LOG_TAG, jse.getMessage(), jse);
                        jse.printStackTrace();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }
            }
        }
        return null;
    }

    @Override
    protected void onPostExecute(String[] strings) {
        if(mMovie != null && mMovie.getLength() != null)
        ((TextView)((Activity)mContext).findViewById(R.id.length)).setText(mMovie.getLength() + " min");
        mTrailerAdapter.setTrailers(mMovie.getTrailers());
        mTrailerAdapter.notifyDataSetChanged();
    }
}
