package app.philm.in.lib.tasks;

import com.uwetrottmann.tmdb.entities.PersonResultsPage;

import app.philm.in.lib.model.PhilmPerson;
import app.philm.in.lib.state.MoviesState;

abstract class BaseTmdbPaginatedPersonRunnable extends BaseTmdbPaginatedRunnable<
        MoviesState.PersonPaginatedResult, PhilmPerson, PersonResultsPage> {

    BaseTmdbPaginatedPersonRunnable(int callingId, int page) {
        super(callingId, page);
    }

    @Override
    protected void updatePaginatedResult(
            MoviesState.PersonPaginatedResult result,
            PersonResultsPage tmdbResult) {
        result.items.addAll(getTmdbPersonEntityMapper().mapAll(tmdbResult.results));

        result.page = tmdbResult.page;
        if (tmdbResult.total_pages != null) {
            result.totalPages = tmdbResult.total_pages;
        }
    }

    @Override
    protected MoviesState.PersonPaginatedResult createPaginatedResult() {
        return new MoviesState.PersonPaginatedResult();
    }
}