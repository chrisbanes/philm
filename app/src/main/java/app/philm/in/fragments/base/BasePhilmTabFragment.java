package app.philm.in.fragments.base;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import app.philm.in.R;
import app.philm.in.view.SlidingTabLayout;

public abstract class BasePhilmTabFragment extends BasePhilmMovieFragment {

    private static final int HIDE_DELAY = 2500;

    private static final String SAVE_SELECTED_TAB = "selected_tab";

    private ViewPager mViewPager;
    private TabPagerAdapter mAdapter;
    private SlidingTabLayout mSlidingTabStrip;

    private int mCurrentItem;

    private boolean mInline;

    public BasePhilmTabFragment(boolean inline) {
        super();
        mInline = inline;
    }

    public BasePhilmTabFragment() {
        this(false);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        return inflater.inflate(mInline
                ? R.layout.fragment_viewpager_inline
                : R.layout.fragment_viewpager,
                container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mAdapter = new TabPagerAdapter(getChildFragmentManager());

        mViewPager = (ViewPager) view.findViewById(R.id.viewpager);
        mViewPager.setAdapter(mAdapter);
        mViewPager.setPageMargin(getResources().getDimensionPixelSize(R.dimen.spacing_minor));

        mSlidingTabStrip = (SlidingTabLayout) view.findViewById(R.id.viewpager_tabs);
        mSlidingTabStrip.setViewPager(mViewPager);

        mSlidingTabStrip.setSelectedIndicatorColors(getResources().getColor(R.color.primary_accent_color));
        mSlidingTabStrip.setDividerColors(getResources().getColor(R.color.primary_accent_color_dark_10));

        if (!mInline) {
            mSlidingTabStrip.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {

                private int mState = ViewPager.SCROLL_STATE_IDLE;

                @Override
                public void onPageScrollStateChanged(final int state) {
                    if (state == ViewPager.SCROLL_STATE_IDLE) {
                        scheduleHideTabs();
                    } else if (mState == ViewPager.SCROLL_STATE_IDLE
                            && state == ViewPager.SCROLL_STATE_DRAGGING) {
                        showTabs();
                    }
                    mState = state;
                }
            });

            mSlidingTabStrip.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent ev) {
                    switch (ev.getAction()) {
                        case MotionEvent.ACTION_MOVE:
                            removeScheduledHideTabs();
                            break;
                        case MotionEvent.ACTION_UP:
                            scheduleHideTabs();
                            break;
                    }
                    return false;
                }
            });

            scheduleHideTabs();
        }

        mSlidingTabStrip.getBackground().setAlpha(255);

        if (savedInstanceState != null) {
            mCurrentItem = savedInstanceState.getInt(SAVE_SELECTED_TAB);
        }
    }

    @Override
    public void showSecondaryLoadingProgress(boolean visible) {
        // NO-OP
    }

    @Override
    public void onPause() {
        super.onPause();
        mCurrentItem = mViewPager.getCurrentItem();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putInt(SAVE_SELECTED_TAB, mCurrentItem);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onInsetsChanged(Rect insets) {
        ((ViewGroup.MarginLayoutParams) mSlidingTabStrip.getLayoutParams()).topMargin = insets.top;
        mSlidingTabStrip.setPadding(insets.left, 0, insets.right, 0);
    }

    protected ViewPager getViewPager() {
        return mViewPager;
    }

    protected void setFragments(List<Fragment> fragments) {
        mAdapter.setFragments(fragments);
        mSlidingTabStrip.notifyDataSetChanged();
        mViewPager.setCurrentItem(mCurrentItem);
    }

    protected SlidingTabLayout getSlidingTabStrip() {
        return mSlidingTabStrip;
    }

    protected TabPagerAdapter getAdapter() {
        return mAdapter;
    }

    protected abstract String getTabTitle(int position);

    protected class TabPagerAdapter extends FragmentPagerAdapter {
        private final ArrayList<Fragment> mFragments;

        private TabPagerAdapter(FragmentManager fm) {
            super(fm);
            mFragments = new ArrayList<>();
        }

        void setFragments(List<Fragment> fragments) {
            mFragments.clear();
            mFragments.addAll(fragments);
            notifyDataSetChanged();
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

    private final Runnable mHideTabsRunnable = new Runnable() {
        @Override
        public void run() {
            if (mSlidingTabStrip.getVisibility() == View.VISIBLE) {
                mSlidingTabStrip.animate().alpha(0f).setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        mSlidingTabStrip.setVisibility(View.GONE);
                    }
                }).start();
            }
        }
    };

    private void scheduleHideTabs() {
        removeScheduledHideTabs();
        getView().postDelayed(mHideTabsRunnable, HIDE_DELAY);
    }

    private void removeScheduledHideTabs() {
        getView().removeCallbacks(mHideTabsRunnable);
    }

    private void showTabs() {
        removeScheduledHideTabs();

        if (mSlidingTabStrip.getVisibility() != View.VISIBLE) {
            mSlidingTabStrip.animate().alpha(1f).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationStart(Animator animation) {
                    mSlidingTabStrip.setVisibility(View.VISIBLE);
                }
            }).start();
        }
    }

}
