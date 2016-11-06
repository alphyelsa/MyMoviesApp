package com.example.android.movies.uidriver;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.example.android.movies.R;

/**
 * Created by aesebast on 9/19/2016.
 */
public class DetailActivity extends AppCompatActivity {

    private final String LOG_TAG = DetailActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /*
        if (savedInstanceState == null) {
            // Create the detail fragment and add it to the activity
            // using a fragment transaction.

            DetailFragment fragment = new DetailFragment();

            getSupportFragmentManager().beginTransaction()
                    .add(R.id.detail_fragment, fragment)
                    .commit();
        }
        */
        setContentView(R.layout.moviedetail);
    }

}
