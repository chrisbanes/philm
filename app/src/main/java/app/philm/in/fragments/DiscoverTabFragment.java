package app.philm.in.fragments;

import com.google.common.base.Preconditions;

import android.support.v4.app.Fragment;

import java.util.ArrayList;

import app.philm.in.R;
import app.philm.in.controllers.MovieController;
import app.philm.in.fragments.base.BasePhilmTabFragment;
import app.philm.in.view.StringManager;

public class DiscoverTabFragment extends BasePhilmTabFragment
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

    private Fragment createFragmentForTab(MovieController.DiscoverTab tab) {
        switch (tab) {
            case POPULAR:
                return new PopularMoviesFragment();
            case IN_THEATRES:
                return new InTheatresMoviesFragment();
            case UPCOMING:
                return new UpcomingMoviesFragment();
            case RECOMMENDED:
                return new RecommendedMoviesFragment();
        }
        return null;
    }
}
