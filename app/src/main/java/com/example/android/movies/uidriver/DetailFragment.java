package com.example.android.movies.uidriver;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.example.android.movies.R;
import com.example.android.movies.data.adapter.TrailerAdapter;
import com.example.android.movies.data.model.Movie;
import com.example.android.movies.data.model.Trailer;
import com.example.android.movies.data.persistence.MovieContract;
import com.example.android.movies.network.FetchDetailTask;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Detail Fragment
 */
public class DetailFragment extends Fragment {

    boolean favourite;
    Movie movie;
    TrailerAdapter mTrailerAdapter;
    private final String LOG_TAG = DetailFragment.class.getSimpleName();

    public DetailFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);

        mTrailerAdapter = new TrailerAdapter(getActivity(), R.layout.trailerlistitem);
        ListView listView = (ListView) rootView.findViewById(R.id.listoftrailers);
        listView.setAdapter(mTrailerAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Trailer trailer = mTrailerAdapter.getItem(position);
                if (trailer != null && trailer.getVideoUrl() != null) {
                    Log.i(LOG_TAG, "Opening the video: " + trailer.getVideoUrl());
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(trailer.getVideoUrl()));
                    startActivity(intent);
                } else {
                    Log.e(LOG_TAG, "Error in loading the video url for this movie " + movie.getMovieTitle());
                }
            }
        });

        Bundle arguments = getArguments();
        if (arguments != null) {
            movie = arguments.getParcelable(Movie.MOVIE);
        } else {

            Intent intent = getActivity().getIntent();
            Bundle data = intent.getExtras();
            if (data != null) {
                movie = data.getParcelable(Movie.MOVIE);
            }
        }
        if (movie != null) {
            favourite = isFavourite();
            if (favourite) {
                Log.v(LOG_TAG, "The Movie: " + movie.getMovieTitle() + " is already marked favourite. Fetching trailer info from DB");

                //Get trailer for specific movie
                Cursor trailerCursor = getActivity().getContentResolver().query(
                        MovieContract.TrailerEntry.buildTrailerUri(movie.getId()),
                        null, // leaving "columns" null just returns all the columns.
                        null, // cols for "where" clause
                        null, // values for "where" clause
                        null  // sort order
                );

                ArrayList<Trailer> trailers = new ArrayList<>();

                while (trailerCursor.moveToNext()) {
                    Log.v(LOG_TAG, "Loading Trailer " + trailerCursor.getString(trailerCursor.getColumnIndex(MovieContract.TrailerEntry.COLUMN_VIDEO_URL)));
                    trailers.add(new Trailer(trailerCursor));
                }
                movie.setTrailers(trailers);
                trailerCursor.close();

                mTrailerAdapter.setTrailers(movie.getTrailers());
                mTrailerAdapter.notifyDataSetChanged();
            } else {
                Log.v(LOG_TAG, "Fetching the data from MovieDB Api");
                //Fetch Trailer Info by querying the api
                FetchDetailTask detailTask = new FetchDetailTask(getActivity(), this.movie, this.mTrailerAdapter);
                detailTask.execute(movie.getId());
            }
        }

        Button button = (Button) rootView.findViewById(R.id.setFavourite);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buttonOperation(v);
            }
        });

        return rootView;
    }

    public boolean isFavourite() {
        boolean isFavourite = false;
        //Query the DB for the movie record
        Log.v(LOG_TAG, "Searching DB for Movie " + movie.getMovieTitle() + " with Id: " + movie.getId());
        Cursor movieCursor = getActivity().getContentResolver().query(
                MovieContract.MovieEntry.buildMovieUri(movie.getId()),
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null  // sort order
        );
        if (movieCursor.getCount() == 1)
            isFavourite = true;
        movieCursor.close();
        return isFavourite;
    }

    @Override
    public void onResume() {
        super.onResume();

        setButtonText();

        if (movie != null) {
            ((TextView) getActivity().findViewById(R.id.title)).setText(movie.getMovieTitle());
            ((TextView) getActivity().findViewById(R.id.user_rating)).setText(movie.getUserRating());
            ImageView imageView = (ImageView) getActivity().findViewById(R.id.poster);
            Picasso.with(getContext()).load(movie.getMoviePoster()).into(imageView);
            ((TextView) getActivity().findViewById(R.id.synopsis)).setText(movie.getPlotSynopsis());
            ((TextView) getActivity().findViewById(R.id.length)).setText(movie.getLength());
            ((TextView) getActivity().findViewById(R.id.year)).setText(movie.getReleaseYear());
        }

    }

    public void buttonOperation(View view) {
        if (favourite) {
            //Delete the Movie Record from DB
            getActivity().getContentResolver().delete(MovieContract.MovieEntry.buildMovieUri(movie.getId()), null, null);
            //Delete the corresponding trailer records
            getActivity().getContentResolver().delete(MovieContract.TrailerEntry.buildTrailerUri(movie.getId()), null, null);
            favourite = false;
            setButtonText();
        } else {
            //Insert the record to db
            ContentValues movieValues = movie.getMovieAsContentValues();
            getActivity().getContentResolver().insert(MovieContract.MovieEntry.CONTENT_URI, movieValues);

            //Insert the trailer records]
            for (Trailer trailer : movie.getTrailers()) {
                Log.v(LOG_TAG, "Inserting trailer " + trailer.getVideoUrl() + " into DB");
                ContentValues trailerValues = trailer.getTrailerAsContentValues();
                getActivity().getContentResolver().insert(MovieContract.TrailerEntry.CONTENT_URI, trailerValues);
            }
            favourite = true;
            setButtonText();

        }
    }

    public void setButtonText() {
        Button favouriteButton = (Button) getActivity().findViewById(R.id.setFavourite);
        if (favouriteButton != null) {
            if (favourite) {
                favouriteButton.setText(R.string.unmarkfavourite);
            } else {
                favouriteButton.setText(R.string.markasfavourite);
            }
        }
    }
}
