package com.example.android.movies.network;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Created by aesebast on 9/27/2016.
 */
public class Network {

    /* This method checks if there is a valid network connection.
        Returns true if a network is present
     */
    public static boolean isNetworkAvaiable(Context context) {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        if (activeNetworkInfo != null && activeNetworkInfo.isConnected())
            return true;
        else
            return false;
    }

}
