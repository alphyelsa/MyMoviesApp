package com.example.android.movies.data.model;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;

import com.example.android.movies.data.persistence.MovieContract;

import java.util.ArrayList;

/**
 * Created by aesebast on 9/15/2016.
 *
 * This class stores the details of each movie.
 *
 */
public class Movie implements Parcelable{

    private String id;
    private String movieTitle;
    private String moviePoster;
    private String plotSynopsis;
    private String userRating;
    private String releaseYear;
    private String length;
    private ArrayList<Trailer> trailers;

    public Movie(String id, String movieTitle, String moviePoster, String plotSynopsis, String userRating, String releaseDate) {
        this.id = id;
        this.movieTitle = movieTitle;
        this.moviePoster = moviePoster;
        this.plotSynopsis = plotSynopsis;
        this.userRating = userRating;
        this.releaseYear = releaseDate.split("-")[0];
        this.length = "0";
    }

    public Movie(Cursor movieCursor){
        this.id = movieCursor.getString(movieCursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_MOVIE_ID));
        this.movieTitle = movieCursor.getString(movieCursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_MOVIE_TITLE));
        this.moviePoster = movieCursor.getString(movieCursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_MOVIE_POSTER));
        this.plotSynopsis = movieCursor.getString(movieCursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_PLOT_SYNOPSIS));
        this.userRating = movieCursor.getString(movieCursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_USER_RATING));
        this.releaseYear = movieCursor.getString(movieCursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_RELEASE_YEAR));
        this.length = movieCursor.getString(movieCursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_LENGTH));
    }

    public String getId() {
        return id;
    }

    public String getMovieTitle() {
        return movieTitle;
    }

    public String getMoviePoster() {
        return moviePoster;
    }

    public String getPlotSynopsis() {
        return plotSynopsis;
    }

    public String getUserRating() {
        return userRating;
    }

    public String getReleaseYear() {
        return releaseYear;
    }

    public String getLength() {
        return length;
    }

    public void setLength(String length) {
        this.length = length;
    }

    public ArrayList<Trailer> getTrailers() {
        return trailers;
    }

    public void setTrailers(ArrayList<Trailer> trailers) {
        this.trailers = trailers;
    }

    @Override
    public String toString() {
        String movieString =  getId() + ":##:" + getMovieTitle() + ":##:" + getMoviePoster() + ":##:" + getPlotSynopsis() + ":##:" + getUserRating() + ":##:" + getReleaseYear() + ":##:" + getLength();
        if(trailers != null) {
            for (Trailer trailer : trailers) {
                movieString = movieString + trailer.toString();
            }
        }
        return movieString;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.id);
        dest.writeString(this.movieTitle);
        dest.writeString(this.moviePoster);
        dest.writeString(this.plotSynopsis);
        dest.writeString(this.userRating);
        dest.writeString(this.releaseYear);
        dest.writeString(this.length);
    }

    // "De-parcel object
    public Movie(Parcel in) {
        this.id = in.readString();
        this.movieTitle = in.readString();
        this.moviePoster = in.readString();
        this.plotSynopsis = in.readString();
        this.userRating = in.readString();
        this.releaseYear = in.readString();
        this.length = in.readString();
    }

    // Creator
    public static final Parcelable.Creator CREATOR
            = new Parcelable.Creator() {
        public Movie createFromParcel(Parcel in) {
            return new Movie(in);
        }

        public Movie[] newArray(int size) {
            return new Movie[size];
        }
    };

    public ContentValues getMovieAsContentValues(){
        ContentValues movieValues = new ContentValues();
        movieValues.put(MovieContract.MovieEntry.COLUMN_MOVIE_ID, this.getId());
        movieValues.put(MovieContract.MovieEntry.COLUMN_MOVIE_TITLE,this.getMovieTitle());
        movieValues.put(MovieContract.MovieEntry.COLUMN_MOVIE_POSTER, this.getMoviePoster());
        movieValues.put(MovieContract.MovieEntry.COLUMN_PLOT_SYNOPSIS, this.getPlotSynopsis());
        movieValues.put(MovieContract.MovieEntry.COLUMN_RELEASE_YEAR, this.getReleaseYear());
        movieValues.put(MovieContract.MovieEntry.COLUMN_USER_RATING, this.getUserRating());
        movieValues.put(MovieContract.MovieEntry.COLUMN_LENGTH, this.getLength());

        return movieValues;
    }
}
