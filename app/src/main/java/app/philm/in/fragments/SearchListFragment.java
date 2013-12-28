package app.philm.in.fragments;

import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.SearchView;

import app.philm.in.R;
import app.philm.in.controllers.MovieController;

public class SearchListFragment extends MovieListFragment implements MovieController.SearchMovieUi {

    private SearchView mSearchView;

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.movies_search, menu);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        MenuItem item = menu.findItem(R.id.menu_search);
        if (item != null) {
            mSearchView = (SearchView) item.getActionView();
            mSearchView.setIconifiedByDefault(false);
            mSearchView.setQueryRefinementEnabled(true);

            mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    if (hasCallbacks()) {
                        getCallbacks().search(query);
                    }
                    mSearchView.clearFocus();
                    return true;
                }

                @Override
                public boolean onQueryTextChange(String query) {
                    return false;
                }
            });
        }
    }

    @Override
    public MovieController.MovieQueryType getMovieQueryType() {
        return MovieController.MovieQueryType.SEARCH;
    }

    @Override
    public void setQuery(String query) {
        if (mSearchView != null) {
            mSearchView.setQuery(query, false);
        }
    }
}
