package app.philm.in;

import com.google.common.base.Preconditions;

import android.app.ActionBar;
import android.content.Intent;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.text.Spannable;
import android.text.SpannableString;

import app.philm.in.fragments.AboutFragment;
import app.philm.in.fragments.CancelCheckinMovieFragment;
import app.philm.in.fragments.CastListFragment;
import app.philm.in.fragments.CheckinMovieFragment;
import app.philm.in.fragments.CredentialsChangedFragment;
import app.philm.in.fragments.CrewListFragment;
import app.philm.in.fragments.DiscoverTabFragment;
import app.philm.in.fragments.LibraryMoviesFragment;
import app.philm.in.fragments.LicencesFragment;
import app.philm.in.fragments.LoginFragment;
import app.philm.in.fragments.MovieDetailFragment;
import app.philm.in.fragments.RateMovieFragment;
import app.philm.in.fragments.RelatedMoviesFragment;
import app.philm.in.fragments.SearchListFragment;
import app.philm.in.fragments.TrendingMoviesFragment;
import app.philm.in.fragments.WatchlistMoviesFragment;
import app.philm.in.util.PhilmTypefaceSpan;
import app.philm.in.view.FontTextView;

public class AndroidDisplay implements Display {

    private final FragmentActivity mActivity;
    private final ActionBarDrawerToggle mActionBarDrawerToggle;
    private final PhilmTypefaceSpan mDefaultTitleSpan;

    public AndroidDisplay(FragmentActivity activity, ActionBarDrawerToggle actionBarDrawerToggle) {
        mActivity = Preconditions.checkNotNull(activity, "activity cannot be null");
        mActionBarDrawerToggle = actionBarDrawerToggle;
        mDefaultTitleSpan = new PhilmTypefaceSpan(activity, FontTextView.FONT_ROBOTO_CONDENSED);
    }

    @Override
    public void showLibrary() {
        showFragmentFromDrawer(new LibraryMoviesFragment());
    }

    @Override
    public void showTrending() {
        showFragmentFromDrawer(new TrendingMoviesFragment());
    }

    @Override
    public void showDiscover() {
        showFragmentFromDrawer(new DiscoverTabFragment());
    }

    @Override
    public void showWatchlist() {
        showFragmentFromDrawer(new WatchlistMoviesFragment());
    }

    @Override
    public void showLogin() {
        LoginFragment fragment = LoginFragment.create();

        mActivity.getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_main, fragment)
                .commit();
    }

    @Override
    public void showMovieDetailFragment(String movieId) {
        MovieDetailFragment fragment = MovieDetailFragment.create(movieId);

        mActivity.getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_main, fragment)
                .addToBackStack(null)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .commit();
    }

    @Override
    public void showSearchFragment() {
        showFragmentFromDrawer(new SearchListFragment());
    }

    @Override
    public void showAboutFragment() {
        AboutFragment fragment = new AboutFragment();
        mActivity.getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_main, fragment)
                .commit();
    }

    @Override
    public void showLicencesFragment() {
        LicencesFragment fragment = new LicencesFragment();

        mActivity.getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_main, fragment)
                .addToBackStack(null)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .commit();
    }

    @Override
    public void showRateMovieFragment(String movieId) {
        RateMovieFragment fragment = RateMovieFragment.create(movieId);
        fragment.show(mActivity.getSupportFragmentManager(), FRAGMENT_TAG_RATE_MOVIE);
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
        return mActivity.getSupportFragmentManager().findFragmentById(R.id.fragment_main) != null;
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
            SpannableString s = new SpannableString(title);
            s.setSpan(mDefaultTitleSpan, 0, s.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            ab.setTitle(s);
        }
    }

    @Override
    public void setActionBarTitle(String title, int color) {
        ActionBar ab = mActivity.getActionBar();
        if (ab != null) {
            SpannableString s = new SpannableString(title);
            s.setSpan(new PhilmTypefaceSpan(mActivity, FontTextView.FONT_ROBOTO_CONDENSED, color),
                    0, s.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            ab.setTitle(s);
        }
    }

    @Override
    public boolean popEntireFragmentBackStack() {
        final FragmentManager fm = mActivity.getSupportFragmentManager();
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

    @Override
    public void showSettings() {
        mActivity.startActivity(new Intent(mActivity, SettingsActivity.class));
    }

    @Override
    public void showRelatedMovies(String movieId) {
        RelatedMoviesFragment fragment = RelatedMoviesFragment.create(movieId);

        mActivity.getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_main, fragment)
                .addToBackStack(null)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .commit();
    }

    @Override
    public void showCastList(String movieId) {
        CastListFragment fragment = CastListFragment.create(movieId);

        mActivity.getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_main, fragment)
                .addToBackStack(null)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .commit();
    }

    @Override
    public void showCrewList(String movieId) {
        CrewListFragment fragment = CrewListFragment.create(movieId);

        mActivity.getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_main, fragment)
                .addToBackStack(null)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .commit();
    }

    @Override
    public void showCastDetail(String personId) {
        // TODO
    }

    @Override
    public void showCheckin(String movieId) {
        CheckinMovieFragment fragment = CheckinMovieFragment.create(movieId);
        fragment.show(mActivity.getSupportFragmentManager(), FRAGMENT_TAG_CHECKIN_MOVIE);
    }

    @Override
    public void showCancelCheckin() {
        CancelCheckinMovieFragment fragment = CancelCheckinMovieFragment.create();
        fragment.show(mActivity.getSupportFragmentManager(), FRAGMENT_TAG_CHECKIN_MOVIE);
    }

    @Override
    public void showCredentialsChanged() {
        new CredentialsChangedFragment().show(mActivity.getSupportFragmentManager(),
                FRAGMENT_TAG_TRAKT_CREDENTIALS_WRONG);
    }

    private void showFragmentFromDrawer(Fragment fragment) {
        popEntireFragmentBackStack();

        mActivity.getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_main, fragment)
                .commit();
    }

}
