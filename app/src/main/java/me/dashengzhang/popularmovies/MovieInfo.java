package me.dashengzhang.popularmovies;

import android.os.Parcel;
import android.os.Parcelable;

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

    private long id;
    private String title;
    private String overview;
    private String date;
    private String poster;
    private double vote;

    public MovieInfo(long id, String title, String overview, String date, String poster, double vote) {
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
        this.id = in.readLong();
        this.title = in.readString();
        this.overview = in.readString();
        this.date = in.readString();
        this.poster = in.readString();
        this.vote = in.readDouble();
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
        out.writeLong(id);
        out.writeString(title);
        out.writeString(overview);
        out.writeString(date);
        out.writeString(poster);
        out.writeDouble(vote);
    }

    public long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getOverview() {
        return overview;
    }

    public String getDate() {
        return date;
    }

    public String getPoster() {
        return poster;
    }

    public double getVote() {
        return vote;
    }
}
