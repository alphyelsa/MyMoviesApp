package com.example.android.movies.data.model;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;

import com.example.android.movies.data.persistence.MovieContract;

/**
 * Created by aesebast on 10/29/2016.
 */

public class Trailer implements Parcelable {
    private String id;
    private String thumbnail;
    private String videoUrl;

    private final String YOUTUBE = "YouTube";

    public Trailer(String id, String key, String site) {
        this.id = id;
        if(YOUTUBE.equals(site)) {
            this.videoUrl = "https://www.youtube.com/watch?v="+key;
        }
        if(YOUTUBE.equals(site)) {
            this.thumbnail = "http://img.youtube.com/vi/"+key+"/default.jpg";
        }
    }

    public Trailer(Cursor trailerCursor){
        this.id = trailerCursor.getString(trailerCursor.getColumnIndex(MovieContract.TrailerEntry.COLUMN_MOVIE_ID));
        this.thumbnail = trailerCursor.getString(trailerCursor.getColumnIndex(MovieContract.TrailerEntry.COLUMN_THUMBNAIL_URL));
        this.videoUrl = trailerCursor.getString(trailerCursor.getColumnIndex(MovieContract.TrailerEntry.COLUMN_VIDEO_URL));
    }

    public String getVideoUrl(){
        return videoUrl;
    }

    public String getThumbNail(){
        return thumbnail;
    }

    public String toString(){
        return getVideoUrl();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.id);
        dest.writeString(this.getThumbNail());
        dest.writeString(this.getVideoUrl());
    }

    // "De-parcel object
    public Trailer(Parcel in) {
        this.id = in.readString();
        this.thumbnail = in.readString();
        this.videoUrl = in.readString();
    }

    // Creator
    public static final Parcelable.Creator CREATOR
            = new Parcelable.Creator() {
        public Trailer createFromParcel(Parcel in) {
            return new Trailer(in);
        }

        public Trailer[] newArray(int size) {
            return new Trailer[size];
        }
    };

    public ContentValues getTrailerAsContentValues(){
        ContentValues trailerValues = new ContentValues();
        trailerValues.put(MovieContract.TrailerEntry.COLUMN_MOVIE_ID, this.id);
        trailerValues.put(MovieContract.TrailerEntry.COLUMN_THUMBNAIL_URL,this.getThumbNail());
        trailerValues.put(MovieContract.TrailerEntry.COLUMN_VIDEO_URL,this.getVideoUrl());
        return trailerValues;
    }
}
