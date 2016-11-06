package com.example.android.movies.network;

import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.example.android.movies.BuildConfig;
import com.example.android.movies.data.adapter.FetchMoviesAdapter;
import com.example.android.movies.data.model.Movie;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by aesebast on 9/27/2016.
 * This class extends AsyncTask and performs the calls to TheMoviedb.
 */
public class FetchMovieTask extends AsyncTask<String, Void, String[]> {
    private final String LOG_TAG = FetchMovieTask.class.getSimpleName();

    FetchMoviesAdapter mMovieAdapter;
    RecyclerView mGridView;
    String mPageNum;
    ArrayList<Movie> movies;

    public FetchMovieTask(RecyclerView gridView, FetchMoviesAdapter movieAdapter, int pagenum) {
        mGridView = gridView;
        mMovieAdapter = movieAdapter;
        mPageNum = Integer.toString(pagenum);
    }

    /**
     * Take the String representing the list of movies in JSON Format and
     * pull out the data.
     * Creates a movie object and invokes the toString() method of the object.
     */
    private void getMovieFromJson(String moviesJsonStr)
            throws JSONException {

        movies = new ArrayList<>();

        // These are the names of the JSON objects that need to be extracted.
        final String MOVIE_LIST = "results";
        final String MOVIE_ID = "id";
        final String MOVIE_TITLE = "title";
        final String MOVIE_POSTER_LOC = "poster_path";
        final String MOVIE_SYNOPSIS = "overview";
        final String MOVIE_DATE = "release_date";
        final String MOVIE_RATING = "vote_average";

        JSONObject moviesJson = new JSONObject(moviesJsonStr);
        JSONArray movieArray = moviesJson.getJSONArray(MOVIE_LIST);

        for (int i = 0; i < movieArray.length(); i++) {
            // Get the JSON object representing the movie
            JSONObject eachMovie = movieArray.getJSONObject(i);
            String id = eachMovie.getString(MOVIE_ID);
            String title = eachMovie.getString(MOVIE_TITLE);
            String movie_poster_loc = "http://image.tmdb.org/t/p/w185/" + eachMovie.getString(MOVIE_POSTER_LOC);
            String movie_plot = eachMovie.getString(MOVIE_SYNOPSIS);
            String release_date = eachMovie.getString(MOVIE_DATE);
            String movie_rating = eachMovie.getString(MOVIE_RATING);
            movies.add(new Movie(id, title, movie_poster_loc, movie_plot, movie_rating, release_date));
        }

    }

    @Override
    protected String[] doInBackground(String... params) {

        Log.v(LOG_TAG, "AsyncTask -> doInBackground() -> Start");

        if (params.length == 0) {
            return null;
        }

        // These two need to be declared outside the try/catch
        // so that they can be closed in the finally block.
        HttpsURLConnection urlConnection = null;
        BufferedReader reader = null;
        StringBuffer buffer = new StringBuffer();

        // Will contain the raw JSON response as a string.
        String moviesJsonStr = null;

        try {
            // Construct the URL for the MovieDB query query
            final String MOVIEDB_BASE_URL = "https://api.themoviedb.org/3/movie/" + params[0] + "?";
            final String APIKEY_PARAM = "api_key";
            final String PAGE_NUM = "page";

            Log.v(LOG_TAG, "Building the URL");
            Uri builtUri = Uri.parse(MOVIEDB_BASE_URL).buildUpon().appendQueryParameter(APIKEY_PARAM, BuildConfig.MOVIEDB_API_KEY).appendQueryParameter(PAGE_NUM, mPageNum).build();
            URL url = new URL(builtUri.toString());

            // Create the request to MovieDB, and open the connection
            Log.v(LOG_TAG, "Connecting to the URL: " + url.toString());
            if (url != null) {
                urlConnection = (HttpsURLConnection) url.openConnection();
            }
            if (urlConnection != null) {
                urlConnection.setRequestMethod("GET");
                //urlConnection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:45.0) Gecko/20100101 Firefox/45.0");
                //urlConnection.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
                urlConnection.connect();
                Log.v(LOG_TAG, "Connection established");
                int status = urlConnection.getResponseCode();
                if (status != 200) {
                    Log.e(LOG_TAG, "Error while establishing connection to " + url.toString());
                    Log.e(LOG_TAG, "Status: " + status);
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
                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }
            }

            Log.v(LOG_TAG, "Reading the JSON");
            String line;
            while ((line = reader.readLine()) != null) {
                // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                // But it does make debugging a *lot* easier if you print out the completed
                // buffer for debugging.
                buffer.append(line + "\n");
            }

            if (buffer.length() == 0) {
                Log.e(LOG_TAG, "Empty JSON String");
                return null;
            }
            moviesJsonStr = buffer.toString();

            Log.d(LOG_TAG, "The JSON Fetched: " + moviesJsonStr);

            try {

                getMovieFromJson(moviesJsonStr);

            } catch (JSONException jse) {
                Log.e(LOG_TAG, jse.getMessage(), jse);
                jse.printStackTrace();
            }

        } catch (IOException e) {
            Log.e(LOG_TAG, "Error ", e);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    Log.e(LOG_TAG, "Error closing stream", e);
                }
            }
        }
        return null;
    }

    @Override
    protected void onPostExecute(String[] strings) {
        Log.d(LOG_TAG, "Setting the movie details to Adapter");
        if (mPageNum.equals("1")) {
            mMovieAdapter.bindData(movies);
        } else {
            mMovieAdapter.appendItems(movies);
            mMovieAdapter.notifyDataSetChanged();
        }
    }
}
