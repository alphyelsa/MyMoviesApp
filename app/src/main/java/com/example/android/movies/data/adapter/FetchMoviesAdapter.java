package com.example.android.movies.data.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.example.android.movies.R;
import com.example.android.movies.data.model.Movie;
import com.example.android.movies.uidriver.DetailActivity;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by aesebast on 10/20/2016.
 */
public class FetchMoviesAdapter extends RecyclerView.Adapter<FetchMoviesAdapter.MovieResultViewHolder> {

    private Context mContext;
    private ArrayList<Movie> movies;

    public FetchMoviesAdapter(final Context context) {
        super();
        this.mContext = context;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public MovieResultViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View m_grid_view = LayoutInflater.from(parent.getContext()).inflate(R.layout.grid_item, parent, false);
        MovieResultViewHolder vh = new MovieResultViewHolder(m_grid_view,mContext);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(MovieResultViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        final Movie movie = movies.get(position);
        holder.setmMovie(movie);
        Picasso.with(mContext).load(movie.getMoviePoster()).into(holder.gridViewIcon);
    }

    @Override
    public int getItemCount() {
        if(movies !=null)
            return movies.size();
        else
            return 0;
    }

    public void appendItems(ArrayList<Movie> newMovies){
        movies.addAll(newMovies);
        bindData(movies);
    }

    public void bindData(ArrayList<Movie> movies)
    {
        this.movies = movies;
        this.notifyDataSetChanged();
    }

    public static class MovieResultViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        ImageView gridViewIcon;
        Context mContext;
        Movie mMovie;

        public MovieResultViewHolder(final View itemView, Context mContext) {
            super(itemView);
            gridViewIcon = (ImageView)itemView.findViewById(R.id.grid_item_icon);
            this.mContext = mContext;
            itemView.setOnClickListener(this);
        }

        public void setmMovie(Movie movie){
            this.mMovie = movie;
        }

        @Override
        public void onClick(View v) {
            Intent gotoDetailIntent = new Intent(mContext, DetailActivity.class);
            gotoDetailIntent.putExtra("movie", mMovie);
            mContext.startActivity(gotoDetailIntent);
        }
    }
}
