package app.philm.in.controllers;

import com.google.common.base.Preconditions;

import com.squareup.otto.Subscribe;

import javax.inject.Inject;

import app.philm.in.Display;
import app.philm.in.model.PhilmUserProfile;
import app.philm.in.model.WatchingMovie;
import app.philm.in.state.ApplicationState;
import app.philm.in.state.AsyncDatabaseHelper;
import app.philm.in.state.MoviesState;
import app.philm.in.state.UserState;
import app.philm.in.util.Logger;
import app.philm.in.util.PhilmPreferences;

public class MainController extends BaseUiController<MainController.MainControllerUi,
        MainController.MainControllerUiCallbacks> {

    private static final String LOG_TAG = MainController.class.getSimpleName();

    public enum SideMenuItem {
        DISCOVER, TRENDING, LIBRARY, WATCHLIST, SEARCH;
    }

    public interface HostCallbacks {
        void finish();

        void setAccountAuthenticatorResult(String username, String authToken, String accountType);
    }

    public interface MainControllerUi extends BaseUiController.Ui<MainControllerUiCallbacks> {
    }

    public interface SideMenuUi extends MainControllerUi {
        void setSideMenuItems(SideMenuItem[] items, SideMenuItem selected);

        void showUserProfile(PhilmUserProfile profile);

        void showAddAccountButton();

        void showMovieCheckin(WatchingMovie movie);

        void hideMovieCheckin();
    }

    public interface MainUi extends MainControllerUi {

        void showLoginPrompt();

    }

    public interface MainControllerUiCallbacks {
        void onSideMenuItemSelected(SideMenuItem item);

        void addAccountRequested();

        void showMovieCheckin();
    }

    private final UserController mUserController;
    private final MovieController mMovieController;
    private final AboutController mAboutController;

    private final AsyncDatabaseHelper mDbHelper;
    private final ApplicationState mState;
    private final PhilmPreferences mPreferences;
    private final Logger mLogger;

    private HostCallbacks mHostCallbacks;

    @Inject
    public MainController(
            ApplicationState state,
            UserController userController,
            MovieController movieController,
            AboutController aboutController,
            AsyncDatabaseHelper dbHelper,
            PhilmPreferences preferences,
            Logger logger) {
        super();

        mState = Preconditions.checkNotNull(state, "state cannot be null");

        mUserController = Preconditions.checkNotNull(userController,
                "userController cannot be null");
        mMovieController = Preconditions.checkNotNull(movieController,
                "movieController cannot be null");
        mAboutController = Preconditions.checkNotNull(aboutController,
                "aboutController cannot be null");
        mDbHelper = Preconditions.checkNotNull(dbHelper, "dbHelper cannot be null");
        mPreferences = Preconditions.checkNotNull(preferences, "preferences cannot be null");
        mLogger = Preconditions.checkNotNull(logger, "logger cannot be null");

        mUserController.setControllerCallbacks(new UserController.ControllerCallbacks() {
            @Override
            public void onAddAccountCompleted(String username, String authToken,
                    String accountType) {
                if (mHostCallbacks != null) {
                    mHostCallbacks.setAccountAuthenticatorResult(username, authToken, accountType);
                    mHostCallbacks.finish();
                }
            }
        });
    }

    @Subscribe
    public void onUserProfileChanged(UserState.UserProfileChangedEvent event) {
        populateUis();
    }

    @Subscribe
    public void onAccountChanged(UserState.AccountChangedEvent event) {
        populateUis();
    }

    @Subscribe
    public void onMovieWatchingChanged(MoviesState.WatchingMovieUpdatedEvent event) {
        populateUis();
    }

    @Override
    public boolean handleIntent(String intentAction) {
        mLogger.d(LOG_TAG, "handleIntent: " + intentAction);

        if (Display.ACTION_MAIN.equals(intentAction)) {
            Display display = getDisplay();
            if (display != null && !display.hasMainFragment()) {
                showUiItem(display, SideMenuItem.DISCOVER);
            }
            return true;
        }

        return mUserController.handleIntent(intentAction)
                || mMovieController.handleIntent(intentAction)
                || mAboutController.handleIntent(intentAction);
    }

    @Override
    protected void onInited() {
        super.onInited();
        mState.registerForEvents(this);

        mUserController.init();
        mMovieController.init();
        mAboutController.init();
    }

    @Override
    protected void populateUi(MainControllerUi ui) {
        if (ui instanceof SideMenuUi) {
            populateUi((SideMenuUi) ui);
        } else if (ui instanceof MainUi) {
            populateUi((MainUi) ui);
        }
    }

    private void populateUi(SideMenuUi ui) {
        ui.setSideMenuItems(getEnabledSideMenuItems(), mState.getSelectedSideMenuItem());

        PhilmUserProfile profile = mState.getUserProfile();
        if (profile != null) {
            ui.showUserProfile(profile);
        } else {
            ui.showAddAccountButton();
        }

        WatchingMovie checkin = mState.getWatchingMovie();
        if (checkin != null) {
            ui.showMovieCheckin(checkin);
        } else {
            ui.hideMovieCheckin();
        }
    }

    private void populateUi(MainUi ui) {
        //if (mState.getCurrentAccount() == null && !mPreferences.hasShownTraktLoginPrompt()) {
            ui.showLoginPrompt();
            mPreferences.setShownTraktLoginPrompt();
        //}
    }

    @Override
    protected MainControllerUiCallbacks createUiCallbacks(final MainControllerUi ui) {
        return new MainControllerUiCallbacks() {
            @Override
            public void onSideMenuItemSelected(SideMenuItem item) {
                Display display = getDisplay();
                if (display != null) {
                    showUiItem(display, item);
                    display.closeDrawerLayout();
                }
            }

            @Override
            public void addAccountRequested() {
                Display display = getDisplay();
                if (display != null) {
                    display.startAddAccountActivity();
                    display.closeDrawerLayout();
                }
            }

            @Override
            public void showMovieCheckin() {
                Display display = getDisplay();
                WatchingMovie checkin = mState.getWatchingMovie();

                if (display != null && checkin != null) {
                    display.closeDrawerLayout();
                    display.showMovieDetailFragment(checkin.movie.getImdbId());
                }
            }
        };
    }

    private void showUiItem(Display display, SideMenuItem item) {
        Preconditions.checkNotNull(display, "display cannot be null");
        Preconditions.checkNotNull(item, "item cannot be null");

        mLogger.d(LOG_TAG, "showUiItem: " + item.name());

        switch (item) {
            case DISCOVER:
                display.showDiscover();
                break;
            case TRENDING:
                display.showTrending();
                break;
            case LIBRARY:
                display.showLibrary();
                break;
            case WATCHLIST:
                display.showWatchlist();
                break;
            case SEARCH:
                display.showSearchFragment();
                break;
        }

        mState.setSelectedSideMenuItem(item);
        populateUis();
    }

    @Override
    protected void onSuspended() {
        mAboutController.suspend();
        mUserController.suspend();
        mMovieController.suspend();

        mDbHelper.close();
        mState.unregisterForEvents(this);

        super.onSuspended();
    }

    @Override
    public void setDisplay(Display display) {
        super.setDisplay(display);
        mMovieController.setDisplay(display);
        mUserController.setDisplay(display);
        mAboutController.setDisplay(display);
    }

    public boolean onAboutButtonPressed() {
        Display display = getDisplay();
        if (display != null) {
            display.startAboutActivity();
        }
        return true;
    }

    public boolean onSettingsButtonPressed() {
        Display display = getDisplay();
        if (display != null) {
            display.showSettings();
        }
        return true;
    }

    public boolean onHomeButtonPressed() {
        Display display = getDisplay();
        if (display != null) {
            if (display.popEntireFragmentBackStack()) {
                return true;
            }
            display.finishActivity();
        }
        return true;
    }

    private SideMenuItem[] getEnabledSideMenuItems() {
        return new SideMenuItem[]{
                SideMenuItem.DISCOVER,
                SideMenuItem.LIBRARY,
                SideMenuItem.WATCHLIST,
                SideMenuItem.SEARCH};
    }

    public void setHostCallbacks(HostCallbacks hostCallbacks) {
        mHostCallbacks = hostCallbacks;
    }

    public final MovieController getMovieController() {
        return mMovieController;
    }

    public final UserController getUserController() {
        return mUserController;
    }

    public final AboutController getAboutController() {
        return mAboutController;
    }
}
