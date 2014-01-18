package app.philm.in.tasks;

import com.uwetrottmann.tmdb.entities.ResultsPage;

import java.util.ArrayList;

import app.philm.in.model.PhilmMovie;
import app.philm.in.state.MoviesState;

abstract class BaseTmdbPaginatedRunnable extends BaseMovieRunnable<ResultsPage> {
    private final int mPage;

    BaseTmdbPaginatedRunnable(int callingId, int page) {
        super(callingId);
        mPage = page;
    }

    @Override
    public final void onSuccess(ResultsPage result) {
        if (result != null) {
            MoviesState.MoviePaginatedResult paginatedResult = getResultFromState();
            if (paginatedResult == null) {
                paginatedResult = createPaginatedResult();
                paginatedResult.items = new ArrayList<PhilmMovie>();
            }

            paginatedResult.items.addAll(getTmdbEntityMapper().map(result.results));
            paginatedResult.page = result.page;

            if (result.total_pages != null) {
                paginatedResult.totalPages = result.total_pages;
            }

            updateState(paginatedResult);
        }
    }

    protected int getPage() {
        return mPage;
    }

    protected abstract MoviesState.MoviePaginatedResult getResultFromState();

    protected abstract void updateState(MoviesState.MoviePaginatedResult result);

    protected MoviesState.MoviePaginatedResult createPaginatedResult() {
        return new MoviesState.MoviePaginatedResult();
    }
}