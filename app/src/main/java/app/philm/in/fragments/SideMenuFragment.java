package app.philm.in.fragments;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.LightingColorFilter;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.util.ArrayMap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import app.philm.in.PhilmApplication;
import app.philm.in.R;
import app.philm.in.controllers.MainController;
import app.philm.in.controllers.MainController.MainControllerUiCallbacks;
import app.philm.in.controllers.MainController.SideMenuItem;
import app.philm.in.drawable.RoundedAvatarDrawable;
import app.philm.in.drawable.TintingBitmapDrawable;
import app.philm.in.fragments.base.InsetAwareFragment;
import app.philm.in.model.PhilmMovie;
import app.philm.in.model.PhilmUserProfile;
import app.philm.in.model.WatchingMovie;
import app.philm.in.util.TextUtils;
import app.philm.in.view.PhilmImageView;
import app.philm.in.view.StringManager;

public class SideMenuFragment extends InsetAwareFragment implements MainController.SideMenuUi,
        View.OnClickListener, AdapterView.OnItemClickListener {

    private static final float CHECKIN_BACKDROP_DARKEN = 0.65f;

    private SideMenuItem[] mSideMenuItems;

    private MainControllerUiCallbacks mCallbacks;

    private ListView mListView;
    private SideMenuItemAdapter mAdapter;

    private View mAddAccountLayout;

    private View mProfileInfoLayout;
    private TextView mFullnameTextView;
    private TextView mUsernameTextView;
    private ImageView mAvatarImageView;

    private View mCheckinLayout;
    private PhilmImageView mCheckinImageView;
    private TextView mCheckinTitleTextView;

    private PhilmUserProfile mUserProfile;

    private LightingColorFilter mColorFilter;

    private final ArrayMap<SideMenuItem, Drawable> mIcons = new ArrayMap<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedState) {
        View view = inflater.inflate(R.layout.fragment_drawer, container, false);

        mListView = (ListView) view.findViewById(android.R.id.list);
        mListView.setOnItemClickListener(this);
        mListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

        mAdapter = new SideMenuItemAdapter();
        mListView.setAdapter(mAdapter);

        mAddAccountLayout = view.findViewById(R.id.layout_add_account);
        mAddAccountLayout.setOnClickListener(this);

        mProfileInfoLayout = view.findViewById(R.id.layout_profile_inner);
        mProfileInfoLayout.setOnClickListener(this);
        mUsernameTextView = (TextView) view.findViewById(R.id.textview_username);
        mFullnameTextView = (TextView) view.findViewById(R.id.textview_fullname);
        mAvatarImageView = (ImageView) view.findViewById(R.id.imageview_account_avatar);

        mCheckinLayout = view.findViewById(R.id.layout_checkin);
        mCheckinLayout.setOnClickListener(this);

        mCheckinImageView = (PhilmImageView) view.findViewById(R.id.imageview_checkin_movie);
        mCheckinImageView.setAutoFade(false);
        mCheckinTitleTextView = (TextView) mCheckinLayout.findViewById(R.id.textview_title);

        final int darkenByte = Math.round(255 * CHECKIN_BACKDROP_DARKEN);
        mColorFilter = new LightingColorFilter(Color.rgb(darkenByte, darkenByte, darkenByte), 0);

        return view;
    }

    @Override
    public boolean isModal() {
        return false;
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
        mAddAccountLayout.setVisibility(View.VISIBLE);
        mProfileInfoLayout.setVisibility(View.GONE);
    }

    @Override
    public void showUserProfile(PhilmUserProfile profile) {
        mUserProfile = profile;
        mAddAccountLayout.setVisibility(View.GONE);
        mProfileInfoLayout.setVisibility(View.VISIBLE);

        Picasso.with(getActivity())
                .load(profile.getAvatarUrl())
                .resizeDimen(R.dimen.drawer_account_avatar_width,
                        R.dimen.drawer_account_avatar_height)
                .centerCrop()
                .into(mAvatarTarget);

        mUsernameTextView.setText(profile.getUsername());

        if (!TextUtils.isEmpty(profile.getFullName())) {
            mFullnameTextView.setText(profile.getFullName());
            mFullnameTextView.setVisibility(View.VISIBLE);
        } else {
            mFullnameTextView.setVisibility(View.GONE);
        }
    }

    @Override
    public void showMovieCheckin(WatchingMovie checkin) {
        mCheckinLayout.setVisibility(View.VISIBLE);
        mCheckinImageView.setVisibility(View.VISIBLE);

        final PhilmMovie movie = checkin.movie;

        mCheckinTitleTextView.setText(movie.getTitle());

        mCheckinImageView.loadBackdrop(movie);
        mCheckinImageView.setColorFilter(mColorFilter);
    }

    @Override
    public void hideMovieCheckin() {
        mCheckinLayout.setVisibility(View.GONE);
        mCheckinImageView.setVisibility(View.GONE);
    }

    @Override
    public void onClick(View view) {
        if (mCallbacks != null) {
            switch (view.getId()) {
                case R.id.layout_add_account:
                    mCallbacks.addAccountRequested();
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
    public void populateInsets(Rect insets) {
        getView().setPadding(insets.left, insets.top, 0, 0);
        mListView.setPadding(0, 0, 0, insets.bottom);
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
                view = getLayoutInflater(null)
                        .inflate(R.layout.simple_list_item_activated, viewGroup, false);
            }

            final SideMenuItem item = getItem(position);
            final TextView textView = (TextView) view.findViewById(android.R.id.text1);
            textView.setText(StringManager.getStringResId(item));

            textView.setCompoundDrawablesWithIntrinsicBounds(getIcon(item), null, null, null);

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

    private final Target mAvatarTarget = new Target() {

        @Override
        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
            mAvatarImageView.setImageDrawable(new RoundedAvatarDrawable(bitmap));
        }

        @Override
        public void onBitmapFailed(Drawable errorDrawable) {
            mAvatarImageView.setImageDrawable(null);
        }

        @Override
        public void onPrepareLoad(Drawable placeHolderDrawable) {
        }
    };

    Drawable getIcon(SideMenuItem item) {
        Drawable d = mIcons.get(item);

        if (d != null) {
            return d;
        }

        switch (item) {
            case DISCOVER:
                d = TintingBitmapDrawable.createFromColorResource(
                        getResources(), R.drawable.ic_btn_movie, R.color.grey_45);
                break;
            case LIBRARY:
                d = TintingBitmapDrawable.createFromColorResource(
                        getResources(), R.drawable.ic_btn_collection, R.color.grey_45);
                break;
            case WATCHLIST:
                d = TintingBitmapDrawable.createFromColorResource(
                        getResources(), R.drawable.ic_btn_watchlist, R.color.grey_45);
                break;
            case SEARCH:
                d = TintingBitmapDrawable.createFromColorResource(
                        getResources(), R.drawable.ic_btn_search, R.color.grey_45);
                break;
        }

        mIcons.put(item, d);
        return d;
    }

}
