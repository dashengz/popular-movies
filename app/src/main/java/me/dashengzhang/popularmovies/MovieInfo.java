package me.dashengzhang.popularmovies;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

public class MovieInfo implements Parcelable {

    /**
     * The Parcelable Creator
     */
    public static final Parcelable.Creator<MovieInfo> CREATOR
            = new Parcelable.Creator<MovieInfo>() {
        public MovieInfo createFromParcel(Parcel in) {
            return new MovieInfo(in);
        }

        public MovieInfo[] newArray(int size) {
            return new MovieInfo[size];
        }
    };
    String id;
    String title;
    String overview;
    String date;
    String poster;
    String vote;

    public MovieInfo(String id, String title, String overview, String date, String poster, String vote) {
        this.id = id;
        this.title = title;
        this.overview = overview;
        this.date = date;
        this.poster = poster;
        this.vote = vote;
    }

    /**
     * This constructor is invoked by the method createFromParcel
     * It's used to retrieve the Movie data from the Parcel object
     *
     * @param in the Parcel object to read
     */
    private MovieInfo(Parcel in) {
        this.id = in.readString();
        this.title = in.readString();
        this.overview = in.readString();
        this.date = in.readString();
        this.poster = in.readString();
        this.vote = in.readString();
    }

    public int describeContents() {
        return 0;
    }

    /**
     * writeToParcel
     * This method stores the MovieInfo data in a Parcel object
     *
     * @param out   the parcel name
     * @param flags the parcel options
     */
    public void writeToParcel(Parcel out, int flags) {
        out.writeString(id);
        out.writeString(title);
        out.writeString(overview);
        out.writeString(date);
        out.writeString(poster);
        out.writeString(vote);
    }

    public ArrayList<String> toArrayList() {
        ArrayList<String> movieInfoArrayList = new ArrayList<>();
        movieInfoArrayList.add(id);
        movieInfoArrayList.add(title);
        movieInfoArrayList.add(overview);
        movieInfoArrayList.add(date);
        movieInfoArrayList.add(poster);
        movieInfoArrayList.add(vote);
        return movieInfoArrayList;
    }
}
