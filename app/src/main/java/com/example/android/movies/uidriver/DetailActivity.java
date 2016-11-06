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
        setContentView(R.layout.moviedetail);
    }

}
