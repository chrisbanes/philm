package app.philm.in;

public interface Display {

    public static final String FRAGMENT_TAG_RATE_MOVIE = "rate_movie";
    public static final String ACTION_ABOUT = "philm.intent.action.ABOUT";
    public static final String ACTION_LOGIN = "philm.intent.action.LOGIN";
    public static final String ACTION_MAIN = "android.intent.action.MAIN";

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

    public void setActionBarTitle(String title);

    public boolean popEntireFragmentBackStack();

    public void showUpNavigation(boolean show);

    public void finishActivity();

}
