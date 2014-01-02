package app.philm.in.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import app.philm.in.PhilmApplication;
import app.philm.in.R;
import app.philm.in.controllers.MainController;
import app.philm.in.controllers.MainController.MainControllerUi;
import app.philm.in.controllers.MainController.MainControllerUiCallbacks;
import app.philm.in.controllers.MainController.SideMenuItem;

public class SideMenuFragment extends Fragment implements MainControllerUi, View.OnClickListener {

    private SideMenuItem[] mSideMenuItems;

    private MainControllerUiCallbacks mCallbacks;

    private LinearLayout mSideItemsLayout;
    private Button mAddAccountButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_drawer, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mSideItemsLayout = (LinearLayout) view.findViewById(R.id.side_items_layout);

        mAddAccountButton = (Button) view.findViewById(R.id.btn_account);
        mAddAccountButton.setOnClickListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        getController().attachUi(this);
    }

    @Override
    public void onPause() {
        getController().detachUi(this);
        super.onPause();
    }

    @Override
    public void setSideMenuItems(SideMenuItem[] items) {
        mSideMenuItems = items;

        if (mSideItemsLayout != null) {
            populateSideItems();
        }
    }

    @Override
    public void onClick(View view) {
        if (mCallbacks != null) {
            if (view == mAddAccountButton) {
                mCallbacks.addAccountRequested();
            } else if (view.getTag() instanceof SideMenuItem) {
                mCallbacks.onSideMenuItemSelected((SideMenuItem) view.getTag());
            }
        }
    }

    private void populateSideItems() {
        mSideItemsLayout.removeAllViews();
        final LayoutInflater inflater = getActivity().getLayoutInflater();

        for (SideMenuItem item : mSideMenuItems) {
            Button button = (Button) inflater.inflate(R.layout.item_drawer, mSideItemsLayout, false);
            button.setText(item.getTitle());
            button.setTag(item);
            button.setOnClickListener(this);
            mSideItemsLayout.addView(button);
        }
    }

    @Override
    public void setCallbacks(MainControllerUiCallbacks callbacks) {
        mCallbacks = callbacks;
    }

    private MainController getController() {
        return PhilmApplication.from(getActivity()).getMainController();
    }

}
