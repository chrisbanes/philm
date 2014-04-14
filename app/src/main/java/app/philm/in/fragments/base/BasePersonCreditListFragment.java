package app.philm.in.fragments.base;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

import java.util.List;

import app.philm.in.adapters.PersonCreditSectionedListAdapter;
import app.philm.in.lib.controllers.MovieController;
import app.philm.in.lib.model.ListItem;
import app.philm.in.lib.model.PhilmPersonCredit;
import app.philm.in.view.PinnedSectionListView;


public abstract class BasePersonCreditListFragment
        extends BaseMovieControllerListFragment<ListView, PhilmPersonCredit>
        implements MovieController.PersonCreditListUi {

    private PersonCreditSectionedListAdapter mMovieListAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mMovieListAdapter = new PersonCreditSectionedListAdapter(getActivity());
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
            ListItem<PhilmPersonCredit> item = (ListItem<PhilmPersonCredit>) l.getItemAtPosition(position);
            if (item.getType() == ListItem.TYPE_ITEM) {
                PhilmPersonCredit credit = item.getItem();
                if (credit != null) {
                    getCallbacks().showMovieDetail(credit);
                }
            }
        }
    }

    @Override
    public void setItems(List<ListItem<PhilmPersonCredit>> items) {
        mMovieListAdapter.setItems(items);
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
