package app.philm.in.fragments.base;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

import java.util.List;

import app.philm.in.adapters.MovieCreditSectionedListAdapter;
import app.philm.in.controllers.MovieController;
import app.philm.in.model.ListItem;
import app.philm.in.model.PhilmMovieCredit;
import app.philm.in.view.PinnedSectionListView;


public abstract class BaseMovieCreditListFragment
        extends BaseMovieControllerListFragment<ListView, PhilmMovieCredit>
        implements MovieController.MovieCreditListUi {

    private MovieCreditSectionedListAdapter mMovieListAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mMovieListAdapter = new MovieCreditSectionedListAdapter(getActivity());
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
            ListItem<PhilmMovieCredit> item = (ListItem<PhilmMovieCredit>) l.getItemAtPosition(position);
            if (item.getType() == ListItem.TYPE_ITEM) {
                PhilmMovieCredit cast = item.getItem();
                if (cast != null && cast.getPerson() != null) {
                    getCallbacks().showPersonDetail(cast.getPerson());
                }
            }
        }
    }

    @Override
    public void setItems(List<ListItem<PhilmMovieCredit>> items) {
        mMovieListAdapter.setItems(items);
    }

    @Override
    public boolean isModal() {
        return false;
    }

    @Override
    protected ListView createListView(Context context) {
        ListView lv = new PinnedSectionListView(context);
        lv.setDivider(null);
        lv.setDividerHeight(0);
        return lv;
    }
}
