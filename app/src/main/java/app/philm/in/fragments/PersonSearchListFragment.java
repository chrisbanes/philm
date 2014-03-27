package app.philm.in.fragments;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.SearchView;

import app.philm.in.R;
import app.philm.in.controllers.MovieController;
import app.philm.in.fragments.base.PersonListFragment;

public class PersonSearchListFragment extends PersonListFragment
        implements MovieController.SearchPersonUi {

    private SearchView mSearchView;
    private String mQueryToDisplay;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setEmptyText(getString(R.string.search_empty));
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.movies_search, menu);

        MenuItem item = menu.findItem(R.id.menu_search);
        if (item != null) {
            mSearchView = (SearchView) item.getActionView();
            mSearchView.setIconifiedByDefault(false);
            mSearchView.setQueryRefinementEnabled(true);
            mSearchView.setQueryHint(getString(R.string.search_hint_movies));

            if (mQueryToDisplay != null) {
                setQuery(mQueryToDisplay);
            }

            mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    if (hasCallbacks()) {
                        getCallbacks().search(query);
                    }
                    if (mSearchView != null) {
                        mSearchView.clearFocus();
                    }
                    return true;
                }

                @Override
                public boolean onQueryTextChange(String query) {
                    if (TextUtils.isEmpty(query) && hasCallbacks()) {
                        getCallbacks().clearSearch();
                    }
                    return false;
                }
            });
        }
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
    }

    @Override
    public void onPause() {
        super.onPause();
        mSearchView = null;
    }

    @Override
    public MovieController.MovieQueryType getMovieQueryType() {
        return MovieController.MovieQueryType.SEARCH_PEOPLE;
    }

    @Override
    public void setQuery(String query) {
        if (mSearchView != null) {
            mSearchView.setQuery(query, false);
            mQueryToDisplay = null;
        } else {
            mQueryToDisplay = query;
        }
    }
}
