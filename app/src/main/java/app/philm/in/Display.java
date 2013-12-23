package app.philm.in;

import com.google.common.base.Preconditions;

import android.app.Activity;
import android.support.v4.widget.DrawerLayout;

import app.philm.in.controllers.MovieController;
import app.philm.in.fragments.LoginFragment;
import app.philm.in.fragments.MovieDetailFragment;
import app.philm.in.fragments.MovieGridFragment;
import app.philm.in.fragments.MovieListFragment;

public class Display {

    private final Activity mActivity;

    public Display(Activity activity) {
        mActivity = Preconditions.checkNotNull(activity, "activity cannot be null");
    }

    public void showLibrary() {
        MovieGridFragment fragment = MovieGridFragment
                .create(MovieController.MovieQueryType.LIBRARY);

        mActivity.getFragmentManager().beginTransaction()
                .replace(R.id.fragment_main, fragment)
                .commit();

        setActionBarTitle(R.string.library_title);
    }

    public void showTrending() {
        MovieGridFragment fragment = MovieGridFragment
                .create(MovieController.MovieQueryType.TRENDING);

        mActivity.getFragmentManager().beginTransaction()
                .replace(R.id.fragment_main, fragment)
                .commit();

        setActionBarTitle(R.string.trending_title);
    }

    public void showWatchlist() {
        MovieListFragment fragment = MovieListFragment
                .create(MovieController.MovieQueryType.WATCHLIST);

        mActivity.getFragmentManager().beginTransaction()
                .replace(R.id.fragment_main, fragment)
                .commit();

        setActionBarTitle(R.string.watchlist_title);
    }

    public void showLogin() {
        LoginFragment fragment = LoginFragment.create();

        mActivity.getFragmentManager().beginTransaction()
                .replace(R.id.fragment_main, fragment)
                .commit();
    }

    public void showMovieDetailFragment() {
        MovieDetailFragment fragment = new MovieDetailFragment();

        mActivity.getFragmentManager().beginTransaction()
                .replace(R.id.fragment_main, fragment)
                .addToBackStack(null)
                .commit();
    }

    public void closeDrawerLayout() {
        DrawerLayout drawerLayout = (DrawerLayout) mActivity.findViewById(R.id.drawer_layout);
        if (drawerLayout != null) {
            drawerLayout.closeDrawers();
        }
    }

    private void setActionBarTitle(int titleResId) {
        mActivity.getActionBar().setTitle(titleResId);
    }

}
