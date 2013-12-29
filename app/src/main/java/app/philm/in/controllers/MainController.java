package app.philm.in.controllers;

import com.google.common.base.Preconditions;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import app.philm.in.Constants;
import app.philm.in.Display;
import app.philm.in.R;
import app.philm.in.state.DatabaseHelper;

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
        void setSideMenuItems(SideMenuItem... items);
    }

    public interface MainControllerUiCallbacks {
        void onSideMenuItemSelected(SideMenuItem item);
    }

    private final UserController mUserController;
    private final MovieController mMovieController;

    private final DatabaseHelper mDbHelper;

    private HostCallbacks mHostCallbacks;

    public MainController(
            UserController userController,
            MovieController movieController,
            DatabaseHelper dbHelper) {
        super();
        mUserController = Preconditions.checkNotNull(userController,
                "userController cannot be null");
        mMovieController = Preconditions.checkNotNull(movieController,
                "movieController cannot be null");
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

    @Override
    public boolean handleIntent(Intent intent) {
        if (Constants.DEBUG) {
            Log.d(LOG_TAG, "handleIntent: " + intent);
        }

        if (Intent.ACTION_MAIN.equals(intent.getAction())) {
            showUiItem(SideMenuItem.TRENDING);
            return true;
        }

        return mUserController.handleIntent(intent) || mMovieController.handleIntent(intent);
    }

    @Override
    protected void onInited() {
        super.onInited();
        mUserController.init();
        mMovieController.init();
    }

    @Override
    protected void populateUi() {
        getUi().setSideMenuItems(SideMenuItem.values());
    }

    @Override
    protected MainControllerUiCallbacks createUiCallbacks() {
        return new MainControllerUiCallbacks() {
            @Override
            public void onSideMenuItemSelected(SideMenuItem item) {
                Display display = getDisplay();
                if (display != null) {
                    display.closeDrawerLayout();
                }
                showUiItem(item);
            }
        };
    }

    private void showUiItem(SideMenuItem item) {
        if (Constants.DEBUG) {
            Log.d(LOG_TAG, "showUiItem: " + item.name());
        }

        Display display = getDisplay();
        if (display != null) {
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
        }
    }

    @Override
    protected void onSuspended() {
        mUserController.suspend();
        mMovieController.suspend();

        mDbHelper.close();
    }

    @Override
    public void setDisplay(Display display) {
        super.setDisplay(display);
        mMovieController.setDisplay(display);
        mUserController.setDisplay(display);
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
}
