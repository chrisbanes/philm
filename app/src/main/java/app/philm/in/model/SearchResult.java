package app.philm.in.model;

import com.google.common.base.Objects;

import java.util.List;

public class SearchResult {

    private final String mQuery;
    private List<PhilmMovie> mMovies;

    public SearchResult(String query) {
        mQuery = query;
    }

    public String getQuery() {
        return mQuery;
    }

    public void setMovies(List<PhilmMovie> movies) {
        mMovies = movies;
    }

    public List<PhilmMovie> getMovies() {
        return mMovies;
    }

    public boolean hasMovies() {
        return mMovies != null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        return Objects.equal(getQuery(), ((SearchResult) o).getQuery());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getQuery());
    }
}
