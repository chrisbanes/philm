package app.philm.in.fragments.base;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.AbsListView;
import android.widget.AdapterView;

import java.util.HashSet;
import java.util.Set;

import app.philm.in.PhilmApplication;
import app.philm.in.R;
import app.philm.in.controllers.MovieController;
import app.philm.in.network.NetworkError;
import app.philm.in.util.PhilmCollections;
import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;


public abstract class PhilmMovieListFragment<E extends AbsListView> extends ListFragment<E>
        implements MovieController.MovieListUi {

    private Set<MovieController.Filter> mFilters;

    private MovieController.MovieUiCallbacks mCallbacks;

    private boolean mFiltersItemVisible;

    private Crouton mCurrentCrouton;

    private int mFirstVisiblePosition;
    private int mFirstVisiblePositionTop;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        mFilters = new HashSet<MovieController.Filter>();
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
                        MovieController.Filter.COLLECTION);
                updateItemCheckedState(menu, R.id.menu_filter_watched,
                        MovieController.Filter.WATCHED);
                updateItemCheckedState(menu, R.id.menu_filter_unwatched,
                        MovieController.Filter.UNWATCHED);
                updateItemCheckedState(menu, R.id.menu_filter_highly_rated,
                        MovieController.Filter.HIGHLY_RATED);

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
                updateFilterState(MovieController.Filter.COLLECTION, !item.isChecked());
                return true;
            case R.id.menu_filter_watched:
                updateFilterState(MovieController.Filter.WATCHED, !item.isChecked());
                return true;
            case R.id.menu_filter_unwatched:
                updateFilterState(MovieController.Filter.UNWATCHED, !item.isChecked());
                return true;
            case R.id.menu_filter_highly_rated:
                updateFilterState(MovieController.Filter.HIGHLY_RATED, !item.isChecked());
                return true;
            case R.id.menu_filter_clear:
                if (mCallbacks != null) {
                    mCallbacks.clearFilters();
                }
                return true;
            case R.id.menu_refresh:
                if (mCallbacks != null) {
                    mCallbacks.refresh();
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }



    @Override
    public void onResume() {
        super.onResume();
        getController().attachUi(this);
    }

    @Override
    public void onPause() {
        saveListViewPosition();
        cancelCrouton();
        getController().detachUi(this);
        super.onPause();
    }

    private void saveListViewPosition() {
        E listView = getListView();

        mFirstVisiblePosition = listView.getFirstVisiblePosition();

        if (mFirstVisiblePosition != AdapterView.INVALID_POSITION && listView.getChildCount() > 0) {
            mFirstVisiblePositionTop = listView.getChildAt(0).getTop();
        }
    }

    protected void moveListViewToSavedPositions() {
        if (mFirstVisiblePosition != AdapterView.INVALID_POSITION) {
            getListView().setSelection(mFirstVisiblePosition);
        }
    }

    @Override
    public String getRequestParameter() {
        return null;
    }

    @Override
    public void showLoadingProgress(boolean visible) {
        setListShown(!visible);
    }

    @Override
    public void setFiltersVisibility(boolean visible) {
        if (mFiltersItemVisible != visible) {
            mFiltersItemVisible = visible;
            getActivity().invalidateOptionsMenu();
        }
    }

    @Override
    public void showActiveFilters(Set<MovieController.Filter> filters) {
        mFilters = filters;
        getActivity().invalidateOptionsMenu();
    }

    @Override
    public void showError(NetworkError error) {
        setListShown(true);
        switch (error) {
            case UNAUTHORIZED:
                setEmptyText(getString(R.string.empty_missing_account, getTitle()));
                break;
            case NETWORK_ERROR:
                setEmptyText(getString(R.string.empty_network_error, getTitle()));
                break;
            case UNKNOWN:
                setEmptyText(getString(R.string.empty_unknown_error, getTitle()));
                break;
        }
    }

    private void cancelCrouton() {
        if (mCurrentCrouton != null) {
            mCurrentCrouton.cancel();
        }
    }

    protected void showCrouton(int text, Style style) {
        cancelCrouton();
        mCurrentCrouton = Crouton.makeText(getActivity(), text, style);
        mCurrentCrouton.show();
    }

    protected final boolean hasCallbacks() {
        return mCallbacks != null;
    }

    protected final MovieController.MovieUiCallbacks getCallbacks() {
        return mCallbacks;
    }

    @Override
    public void setCallbacks(MovieController.MovieUiCallbacks callbacks) {
        mCallbacks = callbacks;
    }

    private MovieController getController() {
        return PhilmApplication.from(getActivity()).getMainController().getMovieController();
    }

    private String getTitle() {
        switch (getMovieQueryType()) {
            case LIBRARY:
                return getString(R.string.library_title);
            case TRENDING:
                return getString(R.string.trending_title);
            case WATCHLIST:
                return getString(R.string.watchlist_title);
        }
        return null;
    }

    private void updateFilterState(MovieController.Filter filter, boolean checked) {
        if (mCallbacks != null) {
            if (checked) {
                mCallbacks.addFilter(filter);
            } else {
                mCallbacks.removeFilter(filter);
            }
        }
    }

    private void updateItemCheckedState(Menu menu, int itemId, MovieController.Filter filter) {
        if (!PhilmCollections.isEmpty(mFilters)) {
            MenuItem item = menu.findItem(itemId);
            if (item != null) {
                item.setChecked(mFilters.contains(filter));
            }
        }
    }

}
