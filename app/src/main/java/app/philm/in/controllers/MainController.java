package app.philm.in.controllers;

import com.google.common.base.Preconditions;

import com.squareup.otto.Subscribe;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import app.philm.in.Constants;
import app.philm.in.Display;
import app.philm.in.R;
import app.philm.in.model.PhilmUserProfile;
import app.philm.in.state.ApplicationState;
import app.philm.in.state.DatabaseHelper;
import app.philm.in.state.UserState;

public class MainController extends BaseUiController<MainController.MainControllerUi,
        MainController.MainControllerUiCallbacks> {

    private static final String LOG_TAG = MainController.class.getSimpleName();

    public static enum SideMenuItem {
        TRENDING(R.string.trending_title),
        LIBRARY(R.string.library_title),
        WATCHLIST(R.string.watchlist_title),
        SEARCH(R.string.search_title);

        private final int mTitleResId;

        private SideMenuItem(int titleResId) {
            mTitleResId = titleResId;
        }

        public int getTitle() {
            return mTitleResId;
        }
    }

    public interface HostCallbacks {
        void finish();

        void setAccountAuthenticatorResult(Bundle bundle);
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

    private final DatabaseHelper mDbHelper;

    private ApplicationState mState;

    private HostCallbacks mHostCallbacks;

    public MainController(
            ApplicationState state,
            UserController userController,
            MovieController movieController,
            AboutController aboutController,
            DatabaseHelper dbHelper) {
        super();

        mState = Preconditions.checkNotNull(state, "state cannot be null");

        mUserController = Preconditions.checkNotNull(userController,
                "userController cannot be null");
        mMovieController = Preconditions.checkNotNull(movieController,
                "movieController cannot be null");
        mAboutController = Preconditions.checkNotNull(aboutController,
                "aboutController cannot be null");
        mDbHelper = Preconditions.checkNotNull(dbHelper, "dbHelper cannot be null");

        mUserController.setControllerCallbacks(new UserController.ControllerCallbacks() {
            @Override
            public void onAddAccountCompleted(Bundle result) {
                if (mHostCallbacks != null) {
                    mHostCallbacks.setAccountAuthenticatorResult(result);
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
    public boolean handleIntent(Intent intent) {
        if (Constants.DEBUG) {
            Log.d(LOG_TAG, "handleIntent: " + intent);
        }

        if (Intent.ACTION_MAIN.equals(intent.getAction())) {
            Display display = getDisplay();
            if (display != null && !display.hasMainFragment()) {
                showUiItem(display, SideMenuItem.TRENDING);
            }
            return true;
        }

        return mUserController.handleIntent(intent)
                || mMovieController.handleIntent(intent)
                || mAboutController.handleIntent(intent);
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

        if (Constants.DEBUG) {
            Log.d(LOG_TAG, "showUiItem: " + item.name());
        }

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

    public boolean onActivityMenuItemSelected(int menuItemId) {
        Display display = getDisplay();

        switch (menuItemId) {
            case R.id.menu_about:
                if (display != null) {
                    display.startAboutActivity();
                }
                return true;
            case android.R.id.home:
                if (display != null) {
                    if (display.popEntireFragmentBackStack()) {
                        return true;
                    }
                    display.finishActivity();
                }
                return true;
        }

        return false;
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
