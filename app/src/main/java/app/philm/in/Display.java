package app.philm.in;

import com.google.common.base.Preconditions;

import android.app.ActionBar;
import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;

import app.philm.in.controllers.MovieController;
import app.philm.in.fragments.LicencesFragment;
import app.philm.in.fragments.LoginFragment;
import app.philm.in.fragments.MovieDetailFragment;
import app.philm.in.fragments.MovieGridFragment;
import app.philm.in.fragments.MovieListFragment;
import app.philm.in.fragments.RateMovieFragment;
import app.philm.in.fragments.SearchListFragment;

public class Display {

    private static final String FRAGMENT_TAG_LICENCES = "licences";
    private static final String FRAGMENT_TAG_RATE_MOVIE = "rate_movie";

    private final Activity mActivity;

    private ActionBarDrawerToggle mActionBarDrawerToggle;

    public Display(Activity activity) {
        this(activity, null);
    }

    public Display(Activity activity, ActionBarDrawerToggle actionBarDrawerToggle) {
        mActivity = Preconditions.checkNotNull(activity, "activity cannot be null");
        mActionBarDrawerToggle = actionBarDrawerToggle;
    }

    public void showLibrary() {
        MovieGridFragment fragment = MovieGridFragment
                .create(MovieController.MovieQueryType.LIBRARY);

        mActivity.getFragmentManager().beginTransaction()
                .replace(R.id.fragment_main, fragment)
                .commit();
    }

    public void showTrending() {
        MovieGridFragment fragment = MovieGridFragment
                .create(MovieController.MovieQueryType.TRENDING);

        mActivity.getFragmentManager().beginTransaction()
                .replace(R.id.fragment_main, fragment)
                .commit();
    }

    public void showWatchlist() {
        MovieListFragment fragment = MovieListFragment
                .create(MovieController.MovieQueryType.WATCHLIST);

        mActivity.getFragmentManager().beginTransaction()
                .replace(R.id.fragment_main, fragment)
                .commit();
    }

    public void showLogin() {
        LoginFragment fragment = LoginFragment.create();

        mActivity.getFragmentManager().beginTransaction()
                .replace(R.id.fragment_main, fragment)
                .commit();
    }

    public void showMovieDetailFragment(String movieId) {
        MovieDetailFragment fragment = MovieDetailFragment.create(movieId);

        mActivity.getFragmentManager().beginTransaction()
                .replace(R.id.fragment_main, fragment)
                .addToBackStack(null)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .commit();
    }

    public void showSearchFragment() {
        SearchListFragment fragment = new SearchListFragment();

        mActivity.getFragmentManager().beginTransaction()
                .replace(R.id.fragment_main, fragment)
                .commit();
    }

    public void showLicencesFragment() {
        LicencesFragment fragment = new LicencesFragment();
        fragment.show(mActivity.getFragmentManager(), FRAGMENT_TAG_LICENCES);
    }

    public void showRateMovieFragment(String movieId) {
        RateMovieFragment fragment = RateMovieFragment.create(movieId);
        fragment.show(mActivity.getFragmentManager(), FRAGMENT_TAG_RATE_MOVIE);
    }

    public void closeDrawerLayout() {
        DrawerLayout drawerLayout = (DrawerLayout) mActivity.findViewById(R.id.drawer_layout);
        if (drawerLayout != null) {
            drawerLayout.closeDrawers();
        }
    }

    public void startAddAccountActivity() {
        Intent intent = new Intent(AccountActivity.ACTION_LOGIN);
        mActivity.startActivity(intent);
    }

    public void setActionBarTitle(int titleResId) {
        setActionBarTitle(mActivity.getString(titleResId));
    }

    public void setDrawerToggleEnabled(boolean enabled) {
        if (mActionBarDrawerToggle != null) {
            mActionBarDrawerToggle.setDrawerIndicatorEnabled(enabled);
        }
    }

    public void setActionBarTitle(String title) {
        ActionBar ab = mActivity.getActionBar();
        if (ab != null) {
            ab.setTitle(title);
        }
    }

}
