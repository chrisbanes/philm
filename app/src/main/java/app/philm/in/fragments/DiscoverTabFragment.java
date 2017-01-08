/*
 * Copyright 2014 Chris Banes
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package app.philm.in.fragments;

import android.support.v4.app.Fragment;

import com.google.common.base.Preconditions;

import java.util.ArrayList;

import app.philm.in.controllers.MovieController;
import app.philm.in.fragments.base.BasePhilmTabFragment;
import app.philm.in.view.StringManager;

public class DiscoverTabFragment extends BasePhilmTabFragment
        implements MovieController.MovieDiscoverUi {

    private MovieController.DiscoverTab[] mTabs;

    @Override
    public MovieController.MovieQueryType getMovieQueryType() {
        return MovieController.MovieQueryType.DISCOVER;
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
