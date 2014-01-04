package app.philm.in;

public interface Display {

    public static final String FRAGMENT_TAG_LICENCES = "licences";
    public static final String FRAGMENT_TAG_RATE_MOVIE = "rate_movie";

    public void showLibrary();

    public void showTrending();

    public void showWatchlist();

    public void showLogin();

    public void showMovieDetailFragment(String movieId);

    public void showSearchFragment();

    public void showAboutFragment();

    public void showLicencesFragment();

    public void showRateMovieFragment(String movieId);

    public void closeDrawerLayout();

    public boolean hasMainFragment();

    public void startAddAccountActivity();

    public void startAboutActivity();

    public void setActionBarTitle(int titleResId);

    public void setDrawerToggleEnabled(boolean enabled);

    public void setActionBarTitle(String title);

    public void popBackStack();

}
