package app.philm.in;

import com.google.common.base.Preconditions;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;

import app.philm.in.controllers.MovieController;
import app.philm.in.fragments.AboutFragment;
import app.philm.in.fragments.LicencesFragment;
import app.philm.in.fragments.LoginFragment;
import app.philm.in.fragments.MovieDetailFragment;
import app.philm.in.fragments.MovieGridFragment;
import app.philm.in.fragments.MovieListFragment;
import app.philm.in.fragments.RateMovieFragment;
import app.philm.in.fragments.SearchListFragment;
import app.philm.in.view.StringManager;

public class AndroidDisplay implements Display {

    private final Activity mActivity;
    private final ActionBarDrawerToggle mActionBarDrawerToggle;

    public AndroidDisplay(Activity activity) {
        this(activity, null);
    }

    public AndroidDisplay(Activity activity, ActionBarDrawerToggle actionBarDrawerToggle) {
        mActivity = Preconditions.checkNotNull(activity, "activity cannot be null");
        mActionBarDrawerToggle = actionBarDrawerToggle;
    }

    @Override
    public void showLibrary() {
        MovieGridFragment fragment = MovieGridFragment
                .create(MovieController.MovieQueryType.LIBRARY);

        showFragmentFromDrawer(fragment);
    }

    @Override
    public void showTrending() {
        MovieGridFragment fragment = MovieGridFragment
                .create(MovieController.MovieQueryType.TRENDING);

        showFragmentFromDrawer(fragment);
    }

    @Override
    public void showWatchlist() {
        MovieListFragment fragment = MovieListFragment
                .create(MovieController.MovieQueryType.WATCHLIST);

        showFragmentFromDrawer(fragment);
    }

    @Override
    public void showLogin() {
        LoginFragment fragment = LoginFragment.create();

        mActivity.getFragmentManager().beginTransaction()
                .replace(R.id.fragment_main, fragment)
                .commit();
    }

    @Override
    public void showMovieDetailFragment(String movieId) {
        MovieDetailFragment fragment = MovieDetailFragment.create(movieId);

        mActivity.getFragmentManager().beginTransaction()
                .replace(R.id.fragment_main, fragment)
                .addToBackStack(null)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .commit();
    }

    @Override
    public void showSearchFragment() {
        SearchListFragment fragment = new SearchListFragment();

        mActivity.getFragmentManager().beginTransaction()
                .replace(R.id.fragment_main, fragment)
                .commit();
    }

    @Override
    public void showAboutFragment() {
        AboutFragment fragment = new AboutFragment();
        mActivity.getFragmentManager().beginTransaction()
                .replace(R.id.fragment_main, fragment)
                .commit();
    }

    @Override
    public void showLicencesFragment() {
        LicencesFragment fragment = new LicencesFragment();

        mActivity.getFragmentManager().beginTransaction()
                .replace(R.id.fragment_main, fragment)
                .addToBackStack(null)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .commit();
    }

    @Override
    public void showRateMovieFragment(String movieId) {
        RateMovieFragment fragment = RateMovieFragment.create(movieId);
        fragment.show(mActivity.getFragmentManager(), FRAGMENT_TAG_RATE_MOVIE);
    }

    @Override
    public void closeDrawerLayout() {
        DrawerLayout drawerLayout = (DrawerLayout) mActivity.findViewById(R.id.drawer_layout);
        if (drawerLayout != null) {
            drawerLayout.closeDrawers();
        }
    }

    @Override
    public boolean hasMainFragment() {
        return mActivity.getFragmentManager().findFragmentById(R.id.fragment_main) != null;
    }

    @Override
    public void startAddAccountActivity() {
        Intent intent = new Intent(ACTION_LOGIN);
        mActivity.startActivity(intent);
    }

    @Override
    public void startAboutActivity() {
        Intent intent = new Intent(ACTION_ABOUT);
        mActivity.startActivity(intent);
    }

    @Override
    public void setActionBarTitle(int titleResId) {
        if (titleResId != 0) {
            setActionBarTitle(mActivity.getString(titleResId));
        }
    }

    @Override
    public void setActionBarTitle(MovieController.MovieQueryType movieQueryType) {
        setActionBarTitle(StringManager.getStringResId(movieQueryType));
    }

    @Override
    public void showUpNavigation(boolean show) {
        if (mActionBarDrawerToggle != null) {
            mActionBarDrawerToggle.setDrawerIndicatorEnabled(!show);
        } else {
            mActivity.getActionBar().setDisplayHomeAsUpEnabled(show);
            mActivity.getActionBar().setHomeButtonEnabled(true);
        }
    }

    @Override
    public void setActionBarTitle(String title) {
        ActionBar ab = mActivity.getActionBar();
        if (ab != null) {
            ab.setTitle(title);
        }
    }

    private void showFragmentFromDrawer(Fragment fragment) {
        popEntireFragmentBackStack();

        mActivity.getFragmentManager().beginTransaction()
                .replace(R.id.fragment_main, fragment)
                .commit();
    }

    @Override
    public boolean popEntireFragmentBackStack() {
        final FragmentManager fm = mActivity.getFragmentManager();
        final int backStackCount = fm.getBackStackEntryCount();
        // Clear Back Stack
        for (int i = 0; i < backStackCount; i++) {
            fm.popBackStack();
        }
        return backStackCount > 0;
    }

    @Override
    public void finishActivity() {
        mActivity.finish();
    }

}
