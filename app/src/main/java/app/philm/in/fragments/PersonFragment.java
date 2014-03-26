package app.philm.in.fragments;

import com.google.common.base.Preconditions;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;

import java.util.ArrayList;

import app.philm.in.controllers.MovieController;
import app.philm.in.fragments.base.BasePhilmTabFragment;
import app.philm.in.view.StringManager;

public class PersonFragment extends BasePhilmTabFragment
        implements MovieController.PersonUi {

    private static final String KEY_QUERY_PERSON_ID = "person_id";

    public static PersonFragment create(String personId) {
        Preconditions.checkArgument(!TextUtils.isEmpty(personId), "personId cannot be empty");

        Bundle bundle = new Bundle();
        bundle.putString(KEY_QUERY_PERSON_ID, personId);

        PersonFragment fragment = new PersonFragment();
        fragment.setArguments(bundle);

        return fragment;
    }

    private MovieController.PersonTab[] mTabs;

    public PersonFragment() {
        super(true);
    }

    @Override
    public MovieController.MovieQueryType getMovieQueryType() {
        return MovieController.MovieQueryType.NONE;
    }

    @Override
    public String getRequestParameter() {
        return getArguments().getString(KEY_QUERY_PERSON_ID);
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
    public void setTabs(MovieController.PersonTab[] tabs) {
        Preconditions.checkNotNull(tabs, "tabs cannot be null");
        mTabs = tabs;

        if (getAdapter().getCount() != tabs.length) {
            ArrayList<Fragment> fragments = new ArrayList<>();
            for (int i = 0; i < tabs.length; i++) {
                fragments.add(createFragmentForTab(tabs[i]));
            }
            setFragments(fragments);
        }
    }

    @Override
    protected String getTabTitle(int position) {
        if (mTabs != null) {
            return getString(StringManager.getStringResId(mTabs[position]));
        }
        return null;
    }

    private Fragment createFragmentForTab(MovieController.PersonTab tab) {
        switch (tab) {
            case CREDIT_CREW:
                return PersonCrewListFragment.create(getRequestParameter());
            case CREDITS_CAST:
                return PersonCastListFragment.create(getRequestParameter());
        }
        return null;
    }
}
