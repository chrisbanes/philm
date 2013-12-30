package app.philm.in.fragments;

import com.hb.views.PinnedSectionListView;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

import java.util.List;

import app.philm.in.adapters.MovieSectionedListAdapter;
import app.philm.in.controllers.MovieController;
import app.philm.in.fragments.base.PhilmMovieListFragment;
import app.philm.in.model.ListItem;
import app.philm.in.model.PhilmMovie;

public class MovieListFragment extends PhilmMovieListFragment<ListView> {

    private static final String KEY_QUERY_TYPE = "query_type";

    private MovieSectionedListAdapter mMovieListAdapter;

    public static MovieListFragment create(MovieController.MovieQueryType type) {
        Bundle bundle = new Bundle();
        bundle.putInt(KEY_QUERY_TYPE, type.ordinal());

        MovieListFragment fragment = new MovieListFragment();
        fragment.setArguments(bundle);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mMovieListAdapter = new MovieSectionedListAdapter(getActivity());
        setListAdapter(mMovieListAdapter);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        if (hasCallbacks()) {
            ListItem<PhilmMovie> item = (ListItem<PhilmMovie>) l.getItemAtPosition(position);
            if (item.getType() == ListItem.TYPE_ITEM) {
                getCallbacks().showMovieDetail(item.getItem());
            }
        }
    }

    @Override
    public void setItems(List<ListItem<PhilmMovie>> items) {
        mMovieListAdapter.setItems(items);
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
    protected ListView createListView(Context context) {
        return new PinnedSectionListView(context, null);
    }

}
