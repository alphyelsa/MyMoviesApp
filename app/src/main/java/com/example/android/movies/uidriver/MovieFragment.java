package com.example.android.movies.uidriver;

import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.android.movies.R;
import com.example.android.movies.data.adapter.FetchMoviesAdapter;
import com.example.android.movies.data.model.Movie;
import com.example.android.movies.data.persistence.MovieContract;
import com.example.android.movies.network.EndlessScrollListener;
import com.example.android.movies.network.FetchMovieTask;
import com.example.android.movies.network.Network;

import java.util.ArrayList;

/* The class driving the fragment content of list page */
public class MovieFragment extends Fragment {

    FetchMoviesAdapter mMovieAdapter;
    RecyclerView mRecyclerView;
    private final String LOG_TAG = MovieFragment.class.getSimpleName();
    private static final int PAGE_COUNT = 20;
    private final String KEY_RECYCLER_STATE = "recycler_state";
    private static Bundle mBundleRecyclerViewState;

    public MovieFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Log.v(LOG_TAG, "OnCreateView()");

        super.onCreate(savedInstanceState);
        View rootView = inflater.inflate(R.layout.fragment_movie, container, false);
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.my_recycler_view);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);

        // use a grid layout manager
        GridLayoutManager mLayoutManager = new GridLayoutManager(getActivity(), getNumColumns());
        mLayoutManager.onSaveInstanceState();
        mRecyclerView.setLayoutManager(mLayoutManager);

        mMovieAdapter = new FetchMoviesAdapter(getActivity());
        mRecyclerView.setAdapter(mMovieAdapter);

        mRecyclerView.addOnScrollListener(new EndlessScrollListener(mLayoutManager) {
            @Override
            public boolean onLoadMore(int page, int totalItemsCount) {
                // Triggered only when new data needs to be appended to the list
                updateMovies(totalItemsCount / PAGE_COUNT + 1);
                return true; // ONLY if more data is actually being loaded; false otherwise.
            }
        });

        // Inflate the layout for this fragment
        return rootView;
    }

    @Override
    public void onPause() {
        super.onPause();

        // save RecyclerView state
        mBundleRecyclerViewState = new Bundle();
        Parcelable listState = mRecyclerView.getLayoutManager().onSaveInstanceState();
        mBundleRecyclerViewState.putParcelable(KEY_RECYCLER_STATE, listState);
    }

    @Override
    public void onResume() {
        Log.v(LOG_TAG, "onResume()");
        super.onResume();

        // restore RecyclerView state
        if (mBundleRecyclerViewState != null) {
            Parcelable listState = mBundleRecyclerViewState.getParcelable(KEY_RECYCLER_STATE);
            mRecyclerView.getLayoutManager().onRestoreInstanceState(listState);
        }
    }

    @Override
    public void onStart() {
        Log.v(LOG_TAG, "onStart()");
        super.onStart();
        updateMovies();
    }

    /* This method counts the number of columns that must be shown in grid based on device size and orientation*/

    private int getNumColumns() {

        Log.v(LOG_TAG, "Setting the number of columns dynamically");

        int numColumns = (getActivity().getChangingConfigurations() == Configuration.ORIENTATION_LANDSCAPE ? 3 : 2);

        /*

        int gridViewEntrySize = getResources().getDimensionPixelSize(R.dimen.grip_view_entry_size);
        int gridViewSpacing = getResources().getDimensionPixelSize(R.dimen.grip_view_spacing);

        WindowManager wm = (WindowManager) getActivity().getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();

        int numColumns = (display.getWidth() - gridViewSpacing) / (gridViewEntrySize + gridViewSpacing);
        */
        Log.v(LOG_TAG, "Number of Columns: " + numColumns);

        return numColumns;
    }

    private void updateMovies() {
        updateMovies(1);
    }

    private void updateMovies(int pagenum) {
        Log.v(LOG_TAG, "updateMovies()");
        String preference = getPreference();
        if (preference.equals(getText(R.string.Favourite))) {
            //[TODO]: Query data from DB and populate the view.
            ArrayList<Movie> movies = new ArrayList();
            Cursor movieCursor = getActivity().getContentResolver().query(
                    MovieContract.MovieEntry.CONTENT_URI,  // Table to Query
                    null, // leaving "columns" null just returns all the columns.
                    null, // cols for "where" clause
                    null, // values for "where" clause
                    null // columns to group by
            );

            while (movieCursor.moveToNext()) {
                movies.add(new Movie(movieCursor));
            }
            mMovieAdapter.bindData(movies);
            mMovieAdapter.notifyDataSetChanged();

            //If length is 0, set a toast message, No Favourites Marked
            if (movies.size() == 0) {
                Log.e(LOG_TAG, "Zero Favourites!");
                Toast.makeText(getActivity(), "No Movies Marked As Favourite", Toast.LENGTH_LONG).show();
            }
        } else {
            if (Network.isNetworkAvaiable(getActivity())) {
                FetchMovieTask movieTask = new FetchMovieTask(mRecyclerView, mMovieAdapter, pagenum);
                movieTask.execute(preference);
            } else {
                Log.e(LOG_TAG, "Network connection is not available!");
                Toast.makeText(getActivity(), "No Internet Connection", Toast.LENGTH_LONG).show();
            }
        }
    }

    private String getPreference() {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String preference = sharedPrefs.getString(getText(R.string.sort_preference).toString(), "");
        Log.v(LOG_TAG, "The preference selected: " + preference);
        return preference;

    }

}
