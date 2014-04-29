package app.philm.in.model;

import com.uwetrottmann.tmdb.entities.Video;

public class PhilmMovieVideo {

    private static final String SOURCE_YOUTUBE = "youtube";

    private static final String TYPE_TRAILER = "trailer";

    public static enum Source {
        QUICKTIME, YOUTUBE
    }

    public static enum Type {
        TRAILER
    }

    private Source mSource;
    private String mId;
    private String mName;
    private Type mType;

    public void setFromTmdb(Video video) {
        if (SOURCE_YOUTUBE.equalsIgnoreCase(video.site)) {
            mSource = Source.YOUTUBE;
        } else {
            mSource = Source.QUICKTIME;
        }
        mName = video.name;
        mId = video.key;

        if (TYPE_TRAILER.equalsIgnoreCase(video.type)) {
            mType = Type.TRAILER;
        }
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

    public Type getType() {
        return mType;
    }

    public static boolean isValid(Video video) {
        return SOURCE_YOUTUBE.equalsIgnoreCase(video.site) && video.key != null;
    }
}
