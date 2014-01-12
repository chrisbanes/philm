package app.philm.in.fragments;

import android.support.v4.app.Fragment;

import java.util.ArrayList;

import app.philm.in.R;
import app.philm.in.controllers.MovieController;
import app.philm.in.fragments.base.BasePhilmMovieTabFragment;
import app.philm.in.view.StringManager;

public class DiscoverTabFragment extends BasePhilmMovieTabFragment
        implements MovieController.MovieDiscoverUi {

    private MovieController.DiscoverTab[] mTabs;

    @Override
    public MovieController.MovieQueryType getMovieQueryType() {
        return MovieController.MovieQueryType.NONE;
    }

    @Override
    public String getRequestParameter() {
        return null;
    }

    @Override
    public String getUiTitle() {
        return getString(R.string.discover_title);
    }

    @Override
    public boolean isModal() {
        return false;
    }

    @Override
    public void setTabs(MovieController.DiscoverTab[] tabs) {
        mTabs = tabs;

        if (getAdapter().getCount() == 0) {
            ArrayList<Fragment> fragments = new ArrayList<Fragment>();
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

    private Fragment createFragmentForTab(MovieController.DiscoverTab tab) {
        switch (tab) {
            case POPULAR:
                return new PopularMoviesFragment();
            case IN_THEATRES:
                return new InTheatresMoviesFragment();
        }
        return null;
    }
}
