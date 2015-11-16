package me.dashengzhang.popularmovies;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Jonathan on 11/10/15.
 */
public class Trailer implements Parcelable {
    /**
     * The Parcelable Creator
     */
    public static final Parcelable.Creator<Trailer> CREATOR
            = new Parcelable.Creator<Trailer>() {
        public Trailer createFromParcel(Parcel in) {
            return new Trailer(in);
        }

        public Trailer[] newArray(int size) {
            return new Trailer[size];
        }
    };

    private long movieId;
    private String trailerId;
    private String name;
    private String key;
    private String type;

    public Trailer(long movieId, String trailerId, String name, String key, String type) {
        this.movieId = movieId;
        this.trailerId = trailerId;
        this.name = name;
        this.key = key;
        this.type = type;
    }

    /**
     * This constructor is invoked by the method createFromParcel
     * It's used to retrieve the Trailer data from the Parcel object
     *
     * @param in the Parcel object to read
     */
    private Trailer(Parcel in) {
        this.movieId = in.readLong();
        this.trailerId = in.readString();
        this.name = in.readString();
        this.key = in.readString();
        this.type = in.readString();
    }

    public int describeContents() {
        return 0;
    }

    /**
     * writeToParcel
     * This method stores the Trailer data in a Parcel object
     *
     * @param out   the parcel name
     * @param flags the parcel options
     */
    public void writeToParcel(Parcel out, int flags) {
        out.writeLong(movieId);
        out.writeString(trailerId);
        out.writeString(name);
        out.writeString(key);
        out.writeString(type);
    }

    public long getMovieId() {
        return movieId;
    }

    public String getTrailerId() {
        return trailerId;
    }

    public String getName() {
        return name;
    }

    public String getKey() {
        return key;
    }

    public String getType() {
        return type;
    }
}
