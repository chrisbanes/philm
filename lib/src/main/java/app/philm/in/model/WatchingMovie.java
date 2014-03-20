package app.philm.in.model;

import com.google.common.base.Preconditions;

import com.jakewharton.trakt.enumerations.ActivityAction;

public class WatchingMovie {

    public enum Type {
        CHECKIN, SCROBBLE
    }

    public final PhilmMovie movie;
    public final Type type;
    public final long startTime;
    public final long endTime;
    public final long duration;

    public WatchingMovie(PhilmMovie movie, Type type, long startTime, long duration) {
        this.movie = Preconditions.checkNotNull(movie, "movie cannot be null");
        this.type = Preconditions.checkNotNull(type, "type cannot be null");
        this.startTime = startTime;
        this.duration = duration;
        this.endTime = startTime + duration;
    }

    public static boolean validAction(ActivityAction action) {
        return from(action) != null;
    }

    public static Type from(ActivityAction action) {
        switch (action) {
            case Checkin:
                return Type.CHECKIN;
            case Scrobble:
                return Type.SCROBBLE;
        }
        return null;
    }
}
