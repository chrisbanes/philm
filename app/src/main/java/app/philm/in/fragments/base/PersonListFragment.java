package app.philm.in.fragments.base;


import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

import java.util.List;

import app.philm.in.adapters.PersonSectionedListAdapter;
import app.philm.in.model.ListItem;
import app.philm.in.model.PhilmPerson;
import app.philm.in.view.PinnedSectionListView;

public abstract class PersonListFragment
        extends BaseMovieControllerListFragment<ListView, PhilmPerson> {

    private PersonSectionedListAdapter mAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAdapter = new PersonSectionedListAdapter(getActivity());
        setListAdapter(mAdapter);
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
            ListItem<PhilmPerson> item = (ListItem<PhilmPerson>) l.getItemAtPosition(position);
            if (item.getType() == ListItem.TYPE_ITEM) {
                getCallbacks().showPersonDetail(item.getItem());
            }
        }
    }

    @Override
    public void setItems(List<ListItem<PhilmPerson>> items) {
        mAdapter.setItems(items);
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
