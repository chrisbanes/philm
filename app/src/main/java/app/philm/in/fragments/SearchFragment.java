package app.philm.in.fragments;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;
import android.widget.SearchView;
import android.widget.TextView;

import java.lang.reflect.Method;
import java.util.ArrayList;

import app.philm.in.Constants;
import app.philm.in.R;
import app.philm.in.controllers.MovieController;
import app.philm.in.fragments.base.BaseDetailFragment;
import app.philm.in.model.PhilmMovie;
import app.philm.in.model.PhilmPerson;
import app.philm.in.state.MoviesState;
import app.philm.in.util.PhilmCollections;
import app.philm.in.view.MovieDetailCardLayout;
import app.philm.in.view.PhilmImageView;

public class SearchFragment extends BaseDetailFragment implements MovieController.MainSearchUi {

    private static final String LOG_TAG = SearchFragment.class.getSimpleName();

    private static final AutoCompleteTextViewReflector HIDDEN_METHOD_INVOKER
            = new AutoCompleteTextViewReflector();

    private MoviesState.SearchResult mSearchResult;

    private SearchView mSearchView;
    private String mQueryToDisplay;

    private final Handler mHandler = new Handler();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void setSearchResult(MoviesState.SearchResult result) {
        mSearchResult = result;
        setQuery(mSearchResult != null ? mSearchResult.query : null);
        populateUi();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.movies_search, menu);

        MenuItem item = menu.findItem(R.id.menu_search);
        if (item != null) {
            mSearchView = (SearchView) item.getActionView();
            mSearchView.setIconifiedByDefault(false);
            mSearchView.setQueryRefinementEnabled(true);
            mSearchView.setQueryHint(getString(R.string.search_hint));

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

            mHandler.post(mShowImeRunnable);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        mSearchView = null;
    }

    private void setQuery(String query) {
        if (mSearchView != null) {
            mSearchView.setQuery(query, false);
            mQueryToDisplay = null;
        } else {
            mQueryToDisplay = query;
        }
    }

    private void populateUi() {
        if (mSearchResult == null) {
            getListAdapter().setItems(null);
            setEmptyText(R.string.search_empty);
            return;
        }

        final ArrayList<SearchCategoryItems> items = new ArrayList<>();

        if (mSearchResult.movies != null && !PhilmCollections.isEmpty(mSearchResult.movies.items)) {
            items.add(SearchCategoryItems.MOVIES);
        }

        if (mSearchResult.people != null && !PhilmCollections.isEmpty(mSearchResult.people.items)) {
            items.add(SearchCategoryItems.PEOPLE);
        }

        if (PhilmCollections.isEmpty(items)) {
            setEmptyText(R.string.search_empty_no_results);
        }

        getListAdapter().setItems(items);
    }

    @Override
    protected ListAdapter createListAdapter() {
        return new SearchCategoryAdapter();
    }

    @Override
    public MovieController.MovieQueryType getMovieQueryType() {
        return MovieController.MovieQueryType.SEARCH;
    }

    @Override
    public String getRequestParameter() {
        return null;
    }

    @Override
    public String getUiTitle() {
        return mSearchResult != null ? mSearchResult.query : null;
    }

    @Override
    public boolean isModal() {
        return false;
    }

    @Override
    protected SearchCategoryAdapter getListAdapter() {
        return (SearchCategoryAdapter) super.getListAdapter();
    }

    private enum SearchCategoryItems implements DetailType {
        MOVIES(R.layout.item_movie_detail_generic_card),
        PEOPLE(R.layout.item_movie_detail_generic_card);

        private final int mLayoutId;

        private SearchCategoryItems(int layoutId) {
            mLayoutId = layoutId;
        }

        @Override
        public int getLayoutId() {
            return mLayoutId;
        }
    }

    protected class SearchCategoryAdapter extends BaseDetailAdapter<SearchCategoryItems> {

        @Override
        public int getViewTypeCount() {
            return SearchCategoryItems.values().length;
        }

        @Override
        protected void bindView(SearchCategoryItems item, View view) {
            if (Constants.DEBUG) {
                Log.d(LOG_TAG, "bindView. Item: " + item.name());
            }

            switch (item) {
                case MOVIES:
                    bindMovies(view);
                    break;
                case PEOPLE:
                    bindPeople(view);
                    break;
            }

            view.setTag(item);
        }

        private void bindMovies(View view) {
            if (Constants.DEBUG) {
                Log.d(LOG_TAG, "bindMovies");
            }

            final View.OnClickListener seeMoreClickListener = new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (hasCallbacks()) {
                        getCallbacks().showMovieSearchResults();
                    }
                }
            };

            MovieAdapter adapter = new MovieAdapter(LayoutInflater.from(getActivity()));

            MovieDetailCardLayout cardLayout = (MovieDetailCardLayout) view;
            cardLayout.setTitle(R.string.category_movies);

            populateDetailGrid(
                    (ViewGroup) view.findViewById(R.id.card_content),
                    cardLayout,
                    seeMoreClickListener,
                    adapter);
        }

        private void bindPeople(View view) {
            if (Constants.DEBUG) {
                Log.d(LOG_TAG, "bindPeople");
            }

            final View.OnClickListener seeMoreClickListener = new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (hasCallbacks()) {
                        getCallbacks().showPeopleSearchResults();
                    }
                }
            };

            PeopleAdapter adapter = new PeopleAdapter(LayoutInflater.from(getActivity()));

            MovieDetailCardLayout cardLayout = (MovieDetailCardLayout) view;
            cardLayout.setTitle(R.string.category_people);

            populateDetailGrid(
                    (ViewGroup) view.findViewById(R.id.card_content),
                    cardLayout,
                    seeMoreClickListener,
                    adapter);
        }
    }

    private class PeopleAdapter extends BaseAdapter {

        private final View.OnClickListener mItemOnClickListener;
        private final LayoutInflater mInflater;

        PeopleAdapter(LayoutInflater inflater) {
            mInflater = inflater;
            mItemOnClickListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (hasCallbacks()) {
                        getCallbacks().showPersonDetail((PhilmPerson) v.getTag());
                    }
                }
            };
        }

        @Override
        public int getCount() {
            if (mSearchResult != null) {
                if (mSearchResult.people != null) {
                    return PhilmCollections.size(mSearchResult.people.items);
                }
            }
            return 0;
        }

        @Override
        public PhilmPerson getItem(int position) {
            return mSearchResult.people.items.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View view, ViewGroup viewGroup) {
            if (view == null) {
                view = mInflater.inflate(getLayoutId(), viewGroup, false);
            }

            final PhilmPerson item = getItem(position);

            final TextView title = (TextView) view.findViewById(R.id.textview_title);
            title.setText(item.getName());

            final PhilmImageView imageView =
                    (PhilmImageView) view.findViewById(R.id.imageview_poster);
            imageView.loadProfileUrl(item);

            view.setOnClickListener(mItemOnClickListener);
            view.setTag(item);

            return view;
        }

        protected int getLayoutId() {
            return R.layout.item_movie_detail_grid_item_1line;
        }
    }

    private class MovieAdapter extends BaseAdapter {

        private final View.OnClickListener mItemOnClickListener;
        private final LayoutInflater mInflater;

        MovieAdapter(LayoutInflater inflater) {
            mInflater = inflater;
            mItemOnClickListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (hasCallbacks()) {
                        getCallbacks().showMovieDetail((PhilmMovie) v.getTag());
                    }
                }
            };
        }

        @Override
        public int getCount() {
            if (mSearchResult != null) {
                if (mSearchResult.movies != null) {
                    return PhilmCollections.size(mSearchResult.movies.items);
                }
            }
            return 0;
        }

        @Override
        public PhilmMovie getItem(int position) {
            return mSearchResult.movies.items.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View view, ViewGroup viewGroup) {
            if (view == null) {
                view = mInflater.inflate(getLayoutId(), viewGroup, false);
            }

            final PhilmMovie item = getItem(position);

            final TextView title = (TextView) view.findViewById(R.id.textview_title);
            title.setText(item.getName());

            final PhilmImageView imageView =
                    (PhilmImageView) view.findViewById(R.id.imageview_poster);
            imageView.loadPosterUrl(item);

            view.setOnClickListener(mItemOnClickListener);
            view.setTag(item);

            return view;
        }

        protected int getLayoutId() {
            return R.layout.item_movie_detail_grid_item_1line;
        }
    }

    private static class AutoCompleteTextViewReflector {
        private Method showSoftInputUnchecked;

        AutoCompleteTextViewReflector() {
            try {
                showSoftInputUnchecked = InputMethodManager.class.getMethod(
                        "showSoftInputUnchecked", int.class, ResultReceiver.class);
                showSoftInputUnchecked.setAccessible(true);
            } catch (NoSuchMethodException e) {
                // Ah well.
            }
        }

        void showSoftInputUnchecked(InputMethodManager imm, View view, int flags) {
            if (showSoftInputUnchecked != null) {
                try {
                    showSoftInputUnchecked.invoke(imm, flags, null);
                    return;
                } catch (Exception e) {
                }
            }

            // Hidden method failed, call public version instead
            imm.showSoftInput(view, flags);
        }
    }

    private final Runnable mShowImeRunnable = new Runnable() {
        public void run() {
            InputMethodManager imm = (InputMethodManager)
                    getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);

            if (imm != null) {
                mSearchView.requestFocus();
                HIDDEN_METHOD_INVOKER.showSoftInputUnchecked(imm, mSearchView, 0);
            }
        }
    };

}
