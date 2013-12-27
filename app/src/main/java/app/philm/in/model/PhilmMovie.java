package app.philm.in.model;

import com.google.common.base.Preconditions;

import com.jakewharton.trakt.entities.Movie;

public class PhilmMovie {

    private Movie traktEntity;

    public PhilmMovie(Movie traktEntity) {
        setMovie(traktEntity);
    }

    public Movie getMovie() {
        return traktEntity;
    }

    public void setMovie(Movie movie) {
        traktEntity = Preconditions.checkNotNull(movie, "movie cannot be null");
    }

    public boolean isWatched() {
        if (traktEntity.watched != null) {
            return traktEntity.watched;
        } else if (traktEntity.plays != null) {
            return traktEntity.plays > 0;
        }
        return false;
    }

    public String getImdbId() {
        return traktEntity.imdb_id;
    }

    public boolean inCollection() {
        if (traktEntity.inCollection != null) {
            return traktEntity.inCollection;
        }
        return false;
    }

    public boolean inWatchlist() {
        if (traktEntity.inWatchlist != null) {
            return traktEntity.inWatchlist;
        }
        return false;
    }

    public String getTitle() {
        return traktEntity.title;
    }

    public int getYear() {
        return traktEntity.year;
    }

}
