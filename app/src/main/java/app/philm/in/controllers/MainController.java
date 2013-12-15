package app.philm.in.controllers;

import com.google.common.base.Preconditions;

public class MainController extends BaseController {

    private final UserController mUserController;
    private final MovieController mMovieController;

    public MainController(UserController userController, MovieController movieController) {
        mUserController = Preconditions.checkNotNull(userController,
                "userController cannot be null");
        mMovieController = Preconditions.checkNotNull(movieController,
                "movieController cannot be null");
    }

    @Override
    protected void onInited() {
        mUserController.init();
        mMovieController.init();
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
