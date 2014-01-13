package app.philm.in.fragments.base;


import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

import java.util.List;

import app.philm.in.adapters.MovieSectionedListAdapter;
import app.philm.in.model.ListItem;
import app.philm.in.model.PhilmMovie;
import app.philm.in.view.PinnedSectionListView;

public abstract class MovieListFragment extends BasePhilmMovieListFragment<ListView> {

    private MovieSectionedListAdapter mMovieListAdapter;

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
    public String getRequestParameter() {
        return null;
    }

    @Override
    public boolean isModal() {
        return false;
    }

    @Override
    protected ListView createListView(Context context) {
        return new PinnedSectionListView(context);
    }

}
