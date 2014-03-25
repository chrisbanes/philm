package app.philm.in.model;

import com.uwetrottmann.tmdb.entities.PersonCredits;

public class PhilmPersonCredit {

    final String title;
    final int id;
    final String job;
    final String posterPath;
    final long releaseDate;

    public PhilmPersonCredit(PersonCredits.CastCredit credit) {
        this.id = credit.id;
        this.title = credit.title;
        this.posterPath = credit.poster_path;
        this.job = credit.character;
        this.releaseDate = credit.release_date != null ? credit.release_date.getTime() : 0;
    }

    public PhilmPersonCredit(PersonCredits.CrewCredit credit) {
        this.id = credit.id;
        this.title = credit.title;
        this.posterPath = credit.poster_path;
        this.job = credit.job;
        this.releaseDate = credit.release_date != null ? credit.release_date.getTime() : 0;
    }

    public String getTitle() {
        return title;
    }

    public int getId() {
        return id;
    }

    public String getJob() {
        return job;
    }

    public String getPosterPath() {
        return posterPath;
    }

    public long getReleaseDate() {
        return releaseDate;
    }
}
