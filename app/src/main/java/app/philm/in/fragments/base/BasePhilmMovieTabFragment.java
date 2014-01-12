package app.philm.in.fragments.base;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import app.philm.in.R;
import app.philm.in.view.SlidingTabLayout;

public abstract class BasePhilmMovieTabFragment extends BasePhilmMovieFragment {

    private ViewPager mViewPager;
    private TabPagerAdapter mAdapter;
    private SlidingTabLayout mSlidingTabStrip;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_viewpager, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mAdapter = new TabPagerAdapter(getChildFragmentManager());

        mViewPager = (ViewPager) view.findViewById(R.id.viewpager);
        mViewPager.setAdapter(mAdapter);

        mSlidingTabStrip = (SlidingTabLayout) view.findViewById(R.id.viewpager_tabs);
        mSlidingTabStrip.setViewPager(mViewPager);

        mSlidingTabStrip.setSelectedIndicatorColors(getResources().getColor(R.color.primary_accent_color));
        mSlidingTabStrip.setDividerColors(getResources().getColor(R.color.primary_accent_color_dark_10));
    }

    protected ViewPager getViewPager() {
        return mViewPager;
    }

    protected SlidingTabLayout getSlidingTabStrip() {
        return mSlidingTabStrip;
    }

    protected TabPagerAdapter getAdapter() {
        return mAdapter;
    }

    protected abstract String getTabTitle(int position);

    public class TabPagerAdapter extends FragmentPagerAdapter {
        private final ArrayList<Fragment> mFragments;

        private TabPagerAdapter(FragmentManager fm) {
            super(fm);
            mFragments = new ArrayList<Fragment>();
        }

        public void addFragment(Fragment fragment) {
            mFragments.add(fragment);
        }

        @Override
        public final Fragment getItem(int position) {
            return mFragments.get(position);
        }

        @Override
        public final int getCount() {
            return mFragments.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return getTabTitle(position);
        }
    }
}
