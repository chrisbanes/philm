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

package app.philm.in.fragments.base;

import com.google.common.base.Preconditions;

import android.os.Bundle;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.AbsListView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import app.philm.in.R;
import app.philm.in.controllers.MovieController;
import app.philm.in.model.ListItem;
import app.philm.in.model.PhilmMovie;
import app.philm.in.util.PhilmCollections;


public abstract class BasePhilmMovieListFragment<E extends AbsListView>
        extends BaseMovieControllerListFragment<E, PhilmMovie>
        implements MovieController.MovieListUi, AbsListView.OnScrollListener {

    private static final String LOG_TAG = BasePhilmMovieListFragment.class.getSimpleName();

    private Set<MovieController.MovieFilter> mFilters;
    private MovieController.MovieOperation[] mBatchOperations;

    private boolean mFiltersItemVisible;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        mFilters = new HashSet<>();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.movies, menu);

        MenuItem item = menu.findItem(R.id.menu_filter);
        if (item != null && item.isVisible() != mFiltersItemVisible) {
            item.setVisible(mFiltersItemVisible);
        }
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        if (mFiltersItemVisible) {

            MenuItem item = menu.findItem(R.id.menu_filter);
            if (item != null) {
                if (!PhilmCollections.isEmpty(mFilters)) {
                    item.setIcon(R.drawable.ic_action_filter_enabled);
                } else {
                    item.setIcon(R.drawable.ic_action_filter);
                }

                updateItemCheckedState(menu, R.id.menu_filter_collection,
                        MovieController.MovieFilter.COLLECTION);
                updateItemCheckedState(menu, R.id.menu_filter_seen,
                        MovieController.MovieFilter.SEEN);
                updateItemCheckedState(menu, R.id.menu_filter_unseen,
                        MovieController.MovieFilter.UNSEEN);
                updateItemCheckedState(menu, R.id.menu_filter_highly_rated,
                        MovieController.MovieFilter.HIGHLY_RATED);

                // Update the clear button depending if there are active filters
                MenuItem clearItem = menu.findItem(R.id.menu_filter_clear);
                if (clearItem != null) {
                    clearItem.setVisible(!PhilmCollections.isEmpty(mFilters));
                }
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_filter_collection:
                updateFilterState(MovieController.MovieFilter.COLLECTION, !item.isChecked());
                return true;
            case R.id.menu_filter_seen:
                updateFilterState(MovieController.MovieFilter.SEEN, !item.isChecked());
                return true;
            case R.id.menu_filter_unseen:
                updateFilterState(MovieController.MovieFilter.UNSEEN, !item.isChecked());
                return true;
            case R.id.menu_filter_highly_rated:
                updateFilterState(MovieController.MovieFilter.HIGHLY_RATED, !item.isChecked());
                return true;
            case R.id.menu_filter_clear:
                if (hasCallbacks()) {
                    getCallbacks().clearFilters();
                }
                return true;
            case R.id.menu_refresh:
                if (hasCallbacks()) {
                    getCallbacks().refresh();
                    return true;
                }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void setFiltersVisibility(boolean visible) {
        if (mFiltersItemVisible != visible) {
            mFiltersItemVisible = visible;
            getActivity().invalidateOptionsMenu();
        }
    }

    @Override
    public void showActiveFilters(Set<MovieController.MovieFilter> filters) {
        mFilters = filters;
        getActivity().invalidateOptionsMenu();
    }

    private void updateFilterState(MovieController.MovieFilter filter, boolean checked) {
        if (hasCallbacks()) {
            if (checked) {
                getCallbacks().addFilter(filter);
            } else {
                getCallbacks().removeFilter(filter);
            }
        }
    }

    private void updateItemCheckedState(Menu menu, int itemId, MovieController.MovieFilter filter) {
        if (!PhilmCollections.isEmpty(mFilters)) {
            MenuItem item = menu.findItem(itemId);
            if (item != null) {
                item.setChecked(mFilters.contains(filter));
            }
        }
    }

    @Override
    public void allowedBatchOperations(MovieController.MovieOperation... operations) {
        Preconditions.checkNotNull(operations, "operations cannot be null");
        Preconditions.checkArgument(operations.length > 0, "operations cannot be empty");
        mBatchOperations = operations;

        getListView().setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE_MODAL);
        getListView().setMultiChoiceModeListener(new MovieMultiChoiceListener());
    }

    @Override
    public void disableBatchOperations() {
        mBatchOperations = null;

        getListView().setChoiceMode(AbsListView.CHOICE_MODE_NONE);
        getListView().setMultiChoiceModeListener(null);
    }

    private class MovieMultiChoiceListener implements AbsListView.MultiChoiceModeListener {

        @Override
        public void onItemCheckedStateChanged(ActionMode mode, int position, long id,
                boolean checked) {
            // NO-OP
        }

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            mode.getMenuInflater().inflate(R.menu.cab_movies, menu);

            for (int i = 0, z = mBatchOperations.length; i < z; i++) {
                switch (mBatchOperations[i]) {
                    case ADD_TO_COLLECTION:
                        menu.findItem(R.id.menu_add_collection).setVisible(true);
                        break;
                    case ADD_TO_WATCHLIST:
                        menu.findItem(R.id.menu_add_watchlist).setVisible(true);
                        break;
                    case MARK_SEEN:
                        menu.findItem(R.id.menu_mark_seen).setVisible(true);
                        break;
                }
            }

            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            if (hasCallbacks()) {
                final E listView = getListView();
                final SparseBooleanArray checkedItems = listView.getCheckedItemPositions();

                final ArrayList<PhilmMovie> movies = new ArrayList<>(checkedItems.size());
                for (int i = 0, z = checkedItems.size(); i < z; i++) {
                    if (checkedItems.valueAt(i)) {
                        final int index = checkedItems.keyAt(i);
                        ListItem<PhilmMovie> listItem =
                                (ListItem<PhilmMovie>) listView.getItemAtPosition(index);
                        if (listItem.getType() == ListItem.TYPE_ITEM) {
                            movies.add(listItem.getItem());
                        }
                    }
                }

                switch (item.getItemId()) {
                    case R.id.menu_mark_seen:
                        getCallbacks().setMoviesSeen(movies, true);
                        return true;
                    case R.id.menu_add_collection:
                        getCallbacks().setMoviesInCollection(movies, true);
                        return true;
                    case R.id.menu_add_watchlist:
                        getCallbacks().setMoviesInWatchlist(movies, true);
                        return true;
                }

            }
            return false;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            // NO-OP
        }
    }

}
