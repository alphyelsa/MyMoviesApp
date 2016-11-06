package com.example.android.movies.data.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.android.movies.R;
import com.example.android.movies.data.model.Trailer;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by aesebast on 10/29/2016.
 */

public class TrailerAdapter extends ArrayAdapter<Trailer> {

    private final String LOG_TAG = TrailerAdapter.class.getSimpleName();
    Context mContext;
    ArrayList<Trailer> trailers;

    public TrailerAdapter(Context context, int resource) {
        super(context, resource);
        this.mContext = context;
    }

    public void setTrailers(ArrayList<Trailer> trailers) {
        this.trailers = trailers;
    }

    @Override
    public int getCount() {
        if (trailers != null) {
            Log.i(LOG_TAG,"No of Trailers:" + trailers.size());
            return trailers.size();
        }
        else {
            Log.i(LOG_TAG,"No Tralers for this movie");
            return 0;
        }
    }

    @Override
    public Trailer getItem(int position){
        Log.i(LOG_TAG,"Getting Trailer " + (position + 1));
        if (position <= trailers.size()){
            return trailers.get(position);
        }
        else
            return null;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        Log.i(LOG_TAG,"Loading trailer at position" + (position+1));

        View listItemView = convertView;
        if (listItemView == null) {
            listItemView = LayoutInflater.from(getContext()).inflate(R.layout.trailerlistitem, parent, false);
        }

        Trailer trailer = getItem(position);
        if(trailer!=null) {

            TextView trailerTitle = (TextView) listItemView.findViewById(R.id.FilePath);
            trailerTitle.setText("Trailer " + (position+1));

            ImageView imageView = (ImageView) listItemView.findViewById(R.id.Thumbnail);
            Picasso.with(mContext).load(trailer.getThumbNail()).into(imageView);
        }

        return listItemView;
    }
}
