package app.philm.in.controllers;

import com.google.common.base.Preconditions;

import android.util.Log;

import app.philm.in.Constants;
import app.philm.in.Display;
import app.philm.in.R;

public class MainController extends BaseUiController<MainController.MainControllerUi,
        MainController.MainControllerUiCallbacks> {

    private static final String LOG_TAG = MainController.class.getSimpleName();

    public static enum SideMenuItem {
        TRENDING(R.string.trending_title),
        LIBRARY(R.string.library_title);

        private final int mTitleResId;

        private SideMenuItem(int titleResId) {
            mTitleResId = titleResId;
        }

        public int getTitle() {
            return mTitleResId;
        }
    }

    public interface MainControllerUi extends BaseUiController.Ui<MainControllerUiCallbacks> {
        void setSideMenuItems(SideMenuItem... items);
    }

    public interface MainControllerUiCallbacks {
        void onSideMenuItemSelected(SideMenuItem item);
    }

    public interface MainControllerProvider {
        MainController getMainController();
    }

    private final UserController mUserController;
    private final MovieController mMovieController;
    private final Display mDisplay;

    public MainController(Display display,
            UserController userController,
            MovieController movieController) {
        super();
        mUserController = Preconditions.checkNotNull(userController,
                "userController cannot be null");
        mMovieController = Preconditions.checkNotNull(movieController,
                "movieController cannot be null");
        mDisplay = Preconditions.checkNotNull(display, "display cannot be null");
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
        showUiItem(SideMenuItem.TRENDING);
    }

    @Override
    protected MainControllerUiCallbacks createUiCallbacks() {
        return new MainControllerUiCallbacks() {
            @Override
            public void onSideMenuItemSelected(SideMenuItem item) {
                mDisplay.closeDrawerLayout();
                showUiItem(item);
            }
        };
    }

    private void showUiItem(SideMenuItem item) {
        if (Constants.DEBUG) {
            Log.d(LOG_TAG, "showUiItem: " + item.name());
        }
        switch (item) {
            case TRENDING:
                mDisplay.showTrending();
                break;
            case LIBRARY:
                mDisplay.showLibrary();
                break;
        }
    }

    @Override
    protected void onSuspended() {
        mUserController.suspend();
        mMovieController.suspend();
    }

    public final MovieController getMovieController() {
        return mMovieController;
    }

    public final UserController getUserController() {
        return mUserController;
    }
}
