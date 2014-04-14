package app.philm.in.lib.tasks;

import java.util.ArrayList;

import app.philm.in.lib.network.NetworkError;
import app.philm.in.lib.state.BaseState;

abstract class BaseTmdbPaginatedRunnable<R extends BaseState.PaginatedResult<PE>, PE, TR>
        extends BaseMovieRunnable<TR> {

    private final int mPage;

    BaseTmdbPaginatedRunnable(int callingId, int page) {
        super(callingId);
        mPage = page;
    }

    @Override
    public final void onSuccess(TR result) {
        if (result != null) {
            R paginatedResult = getResultFromState();

            if (paginatedResult == null) {
                paginatedResult = createPaginatedResult();
                paginatedResult.items = new ArrayList<>();
            }

            updatePaginatedResult(paginatedResult, result);
            updateState(paginatedResult);
        }
    }

    @Override
    protected int getSource() {
        return NetworkError.SOURCE_TMDB;
    }

    protected int getPage() {
        return mPage;
    }

    protected abstract void updatePaginatedResult(R result, TR tmdbResult);

    protected abstract R getResultFromState();

    protected abstract R createPaginatedResult();

    protected abstract void updateState(R result);

    @Override
    protected Object createLoadingProgressEvent(boolean show) {
        if (mPage > 1) {
            return new BaseState.ShowLoadingProgressEvent(getCallingId(), show, true);
        } else {
            return super.createLoadingProgressEvent(show);
        }
    }
}