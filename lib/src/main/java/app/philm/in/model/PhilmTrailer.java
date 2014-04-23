package app.philm.in.model;

import com.uwetrottmann.tmdb.entities.Trailer;

public class PhilmTrailer {

    public static enum Source {
        QUICKTIME, YOUTUBE
    }

    private Source mSource;
    private String mId;
    private String mName;

    public void setFromTmdb(Source source, Trailer tmdbTrailer) {
        mSource = source;
        mName = tmdbTrailer.name;
        mId = tmdbTrailer.source;
    }

    public Source getSource() {
        return mSource;
    }

    public String getId() {
        return mId;
    }

    public String getName() {
        return mName;
    }
}
