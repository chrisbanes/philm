/*
 * Copyright 2014 Chris Banes
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package app.philm.in.tasks;

import java.util.ArrayList;

import app.philm.in.network.NetworkError;
import app.philm.in.state.BaseState;

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