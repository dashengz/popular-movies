package me.dashengzhang.popularmovies;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Jonathan on 11/10/15.
 */
public class Review implements Parcelable {
    /**
     * The Parcelable Creator
     */
    public static final Parcelable.Creator<Review> CREATOR
            = new Parcelable.Creator<Review>() {
        public Review createFromParcel(Parcel in) {
            return new Review(in);
        }

        public Review[] newArray(int size) {
            return new Review[size];
        }
    };

    private long movieId;
    private String reviewId;
    private String author;
    private String content;

    public Review(long movieId, String reviewId, String author, String content) {
        this.movieId = movieId;
        this.reviewId = reviewId;
        this.author = author;
        this.content = content;
    }

    /**
     * This constructor is invoked by the method createFromParcel
     * It's used to retrieve the Review data from the Parcel object
     *
     * @param in the Parcel object to read
     */
    private Review(Parcel in) {
        this.movieId = in.readLong();
        this.reviewId = in.readString();
        this.author = in.readString();
        this.content = in.readString();
    }

    public int describeContents() {
        return 0;
    }

    /**
     * writeToParcel
     * This method stores the Review data in a Parcel object
     *
     * @param out   the parcel name
     * @param flags the parcel options
     */
    public void writeToParcel(Parcel out, int flags) {
        out.writeLong(movieId);
        out.writeString(reviewId);
        out.writeString(author);
        out.writeString(content);
    }

    public long getMovieId() {
        return movieId;
    }

    public String getReviewId() {
        return reviewId;
    }

    public String getAuthor() {
        return author;
    }

    public String getContent() {
        return content;
    }
}
