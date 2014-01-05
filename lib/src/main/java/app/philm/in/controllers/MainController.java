package app.philm.in.controllers;

import com.google.common.base.Preconditions;

import com.squareup.otto.Subscribe;

import app.philm.in.Display;
import app.philm.in.model.PhilmUserProfile;
import app.philm.in.state.ApplicationState;
import app.philm.in.state.AsyncDatabaseHelper;
import app.philm.in.state.UserState;
import app.philm.in.util.Logger;

public class MainController extends BaseUiController<MainController.MainControllerUi,
        MainController.MainControllerUiCallbacks> {

    private static final String LOG_TAG = MainController.class.getSimpleName();

    public enum SideMenuItem {
        TRENDING, LIBRARY, WATCHLIST, SEARCH;
    }

    public interface HostCallbacks {
        void finish();

        void setAccountAuthenticatorResult(String username, String authToken, String accountType);
    }

    public interface MainControllerUi extends BaseUiController.Ui<MainControllerUiCallbacks> {
        void setSideMenuItems(SideMenuItem[] items, SideMenuItem selected);

        void showUserProfile(PhilmUserProfile profile);

        void showAddAccountButton();
    }

    public interface MainControllerUiCallbacks {
        void onSideMenuItemSelected(SideMenuItem item);

        void addAccountRequested();
    }

    private final UserController mUserController;
    private final MovieController mMovieController;
    private final AboutController mAboutController;

    private final AsyncDatabaseHelper mDbHelper;
    private final ApplicationState mState;
    private final Logger mLogger;

    private HostCallbacks mHostCallbacks;

    public MainController(
            ApplicationState state,
            UserController userController,
            MovieController movieController,
            AboutController aboutController,
            AsyncDatabaseHelper dbHelper,
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

    @Override
    public boolean handleIntent(String intentAction) {
        mLogger.d(LOG_TAG, "handleIntent: " + intentAction);

        if (Display.ACTION_MAIN.equals(intentAction)) {
            Display display = getDisplay();
            if (display != null && !display.hasMainFragment()) {
                showUiItem(display, SideMenuItem.TRENDING);
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
        ui.setSideMenuItems(SideMenuItem.values(), mState.getSelectedSideMenuItem());

        PhilmUserProfile profile = mState.getUserProfile();
        if (profile != null) {
            ui.showUserProfile(profile);
        } else {
            ui.showAddAccountButton();
        }
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
        };
    }

    private void showUiItem(Display display, SideMenuItem item) {
        Preconditions.checkNotNull(display, "display cannot be null");
        Preconditions.checkNotNull(item, "item cannot be null");

        mLogger.d(LOG_TAG, "showUiItem: " + item.name());

        switch (item) {
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

    public boolean onHomeButtonPressed() {
        Display display = getDisplay();
        if (display != null) {
            display.popBackStack();
        }
        return true;
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
