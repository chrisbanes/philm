package app.philm.in.fragments;

import com.squareup.picasso.Picasso;

import android.graphics.Rect;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import app.philm.in.PhilmApplication;
import app.philm.in.R;
import app.philm.in.controllers.MainController;
import app.philm.in.controllers.MainController.MainControllerUi;
import app.philm.in.controllers.MainController.MainControllerUiCallbacks;
import app.philm.in.controllers.MainController.SideMenuItem;
import app.philm.in.fragments.base.InsetAwareFragment;
import app.philm.in.model.PhilmMovie;
import app.philm.in.model.PhilmUserProfile;
import app.philm.in.model.WatchingMovie;
import app.philm.in.view.PhilmImageView;
import app.philm.in.view.StringManager;

public class SideMenuFragment extends InsetAwareFragment
        implements MainControllerUi, View.OnClickListener, AdapterView.OnItemClickListener {

    private SideMenuItem[] mSideMenuItems;

    private MainControllerUiCallbacks mCallbacks;

    private ListView mListView;
    private SideMenuItemAdapter mAdapter;

    private View mAccountLayout;
    private TextView mAccountButton;
    private ImageView mAvatarImageView;

    private View mCheckinLayout;
    private PhilmImageView mCheckinImageView;
    private TextView mCheckinTitleTextView;

    private PhilmUserProfile mUserProfile;
    private WatchingMovie mMovieCheckin;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedState) {
        return inflater.inflate(R.layout.fragment_drawer, container, false);
    }

    @Override
    public String getUiTitle() {
        return null;
    }

    @Override
    public boolean isModal() {
        return false;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mListView = (ListView) view.findViewById(android.R.id.list);
        mListView.setOnItemClickListener(this);
        mListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

        mAdapter = new SideMenuItemAdapter();
        mListView.setAdapter(mAdapter);

        mAccountLayout = view.findViewById(R.id.layout_profile);
        mAccountLayout.setOnClickListener(this);
        mAccountButton = (TextView) view.findViewById(R.id.textview_account);
        mAvatarImageView = (ImageView) view.findViewById(R.id.imageview_account_avatar);

        mCheckinLayout = view.findViewById(R.id.layout_checkin);
        mCheckinLayout.setOnClickListener(this);

        //mCheckinImageView = (PhilmImageView) mCheckinLayout
        //        .findViewById(R.id.imageview_checkin_movie);
        mCheckinTitleTextView = (TextView) mCheckinLayout.findViewById(R.id.textview_title);
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
    public void setSideMenuItems(final SideMenuItem[] items, final SideMenuItem selected) {
        mSideMenuItems = items;

        mAdapter.notifyDataSetChanged();

        if (mSideMenuItems != null && selected != null) {
            for (int i = 0; i < mSideMenuItems.length ; i++) {
                if (mSideMenuItems[i] == selected) {
                    mListView.setItemChecked(i, true);
                    break;
                }
            }
        }
    }

    @Override
    public void showAddAccountButton() {
        mUserProfile = null;
        mAvatarImageView.setVisibility(View.GONE);
        mAccountButton.setText(R.string.button_add_account);
    }

    @Override
    public void showUserProfile(PhilmUserProfile profile) {
        mUserProfile = profile;

        mAvatarImageView.setVisibility(View.VISIBLE);

        Picasso.with(getActivity())
                .load(profile.getAvatarUrl())
                .resizeDimen(R.dimen.drawer_account_avatar_width,
                        R.dimen.drawer_account_avatar_height)
                .centerCrop()
                .into(mAvatarImageView);

        mAccountButton.setText(profile.getUsername());
    }

    @Override
    public void showMovieCheckin(WatchingMovie checkin) {
        mCheckinLayout.setVisibility(View.VISIBLE);

        final PhilmMovie movie = checkin.movie;

        //mCheckinImageView.loadPosterUrl(movie);
        mCheckinTitleTextView.setText(movie.getTitle());
    }

    @Override
    public void hideMovieCheckin() {
        mCheckinLayout.setVisibility(View.GONE);
    }

    @Override
    public void onClick(View view) {
        if (mCallbacks != null) {
            switch (view.getId()) {
                case R.id.layout_profile:
                    if (mUserProfile == null) {
                        mCallbacks.addAccountRequested();
                    }
                    break;
                case R.id.layout_checkin:
                    mCallbacks.showMovieCheckin();
                    break;
            }
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (mCallbacks != null) {
            mCallbacks.onSideMenuItemSelected((SideMenuItem) parent.getItemAtPosition(position));
        }
    }

    @Override
    public void onInsetsChanged(Rect insets) {
        getView().setPadding(insets.left, insets.top, 0, insets.bottom);
    }

    private class SideMenuItemAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return mSideMenuItems != null ? mSideMenuItems.length : 0;
        }

        @Override
        public SideMenuItem getItem(int position) {
            return mSideMenuItems[position];
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View view, ViewGroup viewGroup) {
            if (view == null) {
                view = getActivity().getLayoutInflater()
                        .inflate(android.R.layout.simple_list_item_activated_1, viewGroup, false);
            }

            final SideMenuItem item = getItem(position);
            final TextView textView = (TextView) view.findViewById(android.R.id.text1);
            textView.setText(StringManager.getStringResId(item));

            return view;
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
