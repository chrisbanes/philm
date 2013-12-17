package app.philm.in.fragments;

import com.google.common.base.Preconditions;

import android.app.ListFragment;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import app.philm.in.R;
import app.philm.in.controllers.MainController.MainControllerProvider;
import app.philm.in.controllers.MainController.MainControllerUi;
import app.philm.in.controllers.MainController.MainControllerUiCallbacks;
import app.philm.in.controllers.MainController.SideMenuItem;

public class SideMenuFragment extends ListFragment implements MainControllerUi {

    private MainControllerUiCallbacks mCallbacks;

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getView().setBackgroundColor(getResources().getColor(R.color.side_menu_background));
    }

    @Override
    public void onResume() {
        super.onResume();
        ((MainControllerProvider) getActivity()).getMainController().attachUi(this);
    }

    @Override
    public void onPause() {
        ((MainControllerProvider) getActivity()).getMainController().detachUi(this);
        super.onPause();
    }

    @Override
    public void setSideMenuItems(SideMenuItem[] items) {
        setListAdapter(new SideMenuItemAdapter(items));
        setListShown(true);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        if (mCallbacks != null) {
            mCallbacks.onSideMenuItemSelected((SideMenuItem) l.getItemAtPosition(position));
        }
    }

    @Override
    public void setCallbacks(MainControllerUiCallbacks callbacks) {
        mCallbacks = callbacks;
    }

    private class SideMenuItemAdapter extends BaseAdapter {
        private SideMenuItem[] mItems;

        SideMenuItemAdapter(SideMenuItem[] items) {
            mItems = Preconditions.checkNotNull(items, "items cannot be null");
        }

        @Override
        public int getCount() {
            return mItems.length;
        }

        @Override
        public SideMenuItem getItem(int position) {
            return mItems[position];
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View view, ViewGroup viewGroup) {
            if (view == null) {
                view = getActivity().getLayoutInflater()
                        .inflate(android.R.layout.simple_list_item_1, viewGroup, false);
            }

            final SideMenuItem item = getItem(position);
            final TextView textView = (TextView) view.findViewById(android.R.id.text1);
            textView.setText(item.getTitle());

            return view;
        }
    }
}
