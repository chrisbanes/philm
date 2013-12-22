package app.philm.in.fragments;

import com.jakewharton.trakt.entities.Movie;

import android.animation.LayoutTransition;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.GridView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import app.philm.in.PhilmApplication;
import app.philm.in.R;
import app.philm.in.adapters.MovieGridAdapter;
import app.philm.in.controllers.MovieController;
import app.philm.in.fragments.base.GridFragment;

public class MovieGridFragment extends GridFragment implements MovieController.MovieUi {

    private static final String KEY_QUERY_TYPE = "query_type";

    private MovieController.MovieUiCallbacks mCallbacks;
    private Set<MovieController.Filter> mFilters;

    private MovieGridAdapter mMovieGridAdapter;

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

        setGridShown(false);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.movies, menu);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        updateItemCheckedState(menu, R.id.menu_filter_watched, MovieController.Filter.WATCHED);
        updateItemCheckedState(menu, R.id.menu_filter_unwatched, MovieController.Filter.UNWATCHED);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
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
    public void setItems(List<Movie> items) {
        mMovieGridAdapter.setItems(items);
        setGridShown(true);
    }

    @Override
    public MovieController.MovieQueryType getMovieQueryType() {
        final int queryType = getArguments().getInt(KEY_QUERY_TYPE);
        return MovieController.MovieQueryType.values()[queryType];
    }

    @Override
    public void showError(MovieController.Error error) {
        // TODO: Fix
        Toast.makeText(getActivity(), "Error: " + error.name(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void setActiveFilters(Set<MovieController.Filter> filters) {
        mFilters = filters;
        getActivity().invalidateOptionsMenu();
    }

    private void updateItemCheckedState(Menu menu, int itemId, MovieController.Filter filter) {
        MenuItem item = menu.findItem(itemId);
        if (item != null) {
            item.setChecked(mFilters.contains(filter));
        }
    }

    private MovieController getController() {
        return PhilmApplication.from(getActivity()).getMainController().getMovieController();
    }
}
