package app.philm.in.fragments;

import android.content.res.Resources;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.GridView;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import app.philm.in.PhilmApplication;
import app.philm.in.R;
import app.philm.in.adapters.MovieGridAdapter;
import app.philm.in.controllers.MovieController;
import app.philm.in.fragments.base.GridFragment;
import app.philm.in.model.PhilmMovie;
import app.philm.in.network.NetworkError;
import app.philm.in.util.PhilmCollections;

public class MovieGridFragment extends GridFragment implements MovieController.MovieListUi {

    private static final String KEY_QUERY_TYPE = "query_type";

    private MovieController.MovieUiCallbacks mCallbacks;
    private Set<MovieController.Filter> mFilters;

    private MovieGridAdapter mMovieGridAdapter;

    private boolean mFiltersItemVisible;

    public static MovieGridFragment create(MovieController.MovieQueryType type) {
        Bundle bundle = new Bundle();
        bundle.putInt(KEY_QUERY_TYPE, type.ordinal());

        MovieGridFragment fragment = new MovieGridFragment();
        fragment.setArguments(bundle);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        mMovieGridAdapter = new MovieGridAdapter(getActivity());
        setListAdapter(mMovieGridAdapter);

        mFilters = new HashSet<MovieController.Filter>();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Resources res = getResources();
        GridView gridView = getGridView();

        gridView.setNumColumns(GridView.AUTO_FIT);
        gridView.setColumnWidth(res.getDimensionPixelSize(R.dimen.movie_grid_item_width));
        gridView.setHorizontalSpacing(res.getDimensionPixelSize(R.dimen.movie_grid_spacing));
        gridView.setVerticalSpacing(res.getDimensionPixelSize(R.dimen.movie_grid_spacing));
        gridView.setFastScrollEnabled(true);

        setGridShown(false);
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
            updateItemCheckedState(menu, R.id.menu_filter_collection,
                    MovieController.Filter.COLLECTION);
            updateItemCheckedState(menu, R.id.menu_filter_watched,
                    MovieController.Filter.WATCHED);
            updateItemCheckedState(menu, R.id.menu_filter_unwatched,
                    MovieController.Filter.UNWATCHED);

            // Update the clear button depending if there are active filters
            menu.findItem(R.id.menu_filter_clear).setVisible(!PhilmCollections.isEmpty(mFilters));
        }
    }

    @Override
    public void onListItemClick(GridView l, View v, int position, long id) {
        if (mCallbacks != null) {
            PhilmMovie movie = (PhilmMovie) l.getItemAtPosition(position);
            mCallbacks.showMovieDetail(movie);
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

    private void updateFilterState(MovieController.Filter filter, boolean checked) {
        if (mCallbacks != null) {
            if (checked) {
                mCallbacks.addFilter(filter);
            } else {
                mCallbacks.removeFilter(filter);
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        getController().attachUi(this);
    }

    @Override
    public void onPause() {
        getController().detachUi(this);
        super.onPause();
    }

    @Override
    public void setCallbacks(MovieController.MovieUiCallbacks callbacks) {
        mCallbacks = callbacks;
    }

    @Override
    public void setItems(List<PhilmMovie> items) {
        mMovieGridAdapter.setItems(items);
    }

    @Override
    public void setItemsWithSections(List<PhilmMovie> items,
            List<MovieController.Filter> sections) {
        mMovieGridAdapter.setItems(items);
    }

    @Override
    public MovieController.MovieQueryType getMovieQueryType() {
        final int queryType = getArguments().getInt(KEY_QUERY_TYPE);
        return MovieController.MovieQueryType.values()[queryType];
    }

    @Override
    public String getRequestParameter() {
        return null;
    }

    @Override
    public void showError(NetworkError error) {
        setGridShown(true);
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

    @Override
    public void showLoadingProgress(boolean visible) {
        setGridShown(!visible);
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

    private void updateItemCheckedState(Menu menu, int itemId, MovieController.Filter filter) {
        if (!PhilmCollections.isEmpty(mFilters)) {
            MenuItem item = menu.findItem(itemId);
            if (item != null) {
                item.setChecked(mFilters.contains(filter));
            }
        }
    }

    private MovieController getController() {
        return PhilmApplication.from(getActivity()).getMainController().getMovieController();
    }
}
