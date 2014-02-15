package app.philm.in.fragments.base;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

import java.util.List;

import app.philm.in.adapters.MovieCastSectionedListAdapter;
import app.philm.in.controllers.MovieController;
import app.philm.in.model.ListItem;
import app.philm.in.model.PhilmCast;
import app.philm.in.view.PinnedSectionListView;


public abstract class MovieCastListFragment
        extends BaseMovieControllerListFragment<ListView, PhilmCast>
        implements MovieController.MovieCastListUi {

    private MovieCastSectionedListAdapter mMovieListAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mMovieListAdapter = new MovieCastSectionedListAdapter(getActivity());
        setListAdapter(mMovieListAdapter);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ListView lv = getListView();
        lv.setDrawSelectorOnTop(true);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        if (hasCallbacks()) {
            ListItem<PhilmCast> item = (ListItem<PhilmCast>) l.getItemAtPosition(position);
            if (item.getType() == ListItem.TYPE_ITEM) {
                // TODO
                //getCallbacks().showMovieDetail(item.getItem());
            }
        }
    }

    @Override
    public void setItems(List<ListItem<PhilmCast>> items) {
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
