package app.philm.in.model;

public class WatchingMovie {

    public final PhilmMovie movie;
    public final long startTime;
    public final long endTime;
    public final long duration;

    public WatchingMovie(PhilmMovie movie, long startTime, long duration) {
        this.movie = movie;
        this.startTime = startTime;
        this.duration = duration;
        endTime = startTime + duration;
    }
}
