package app.philm.in.controllers;

import com.google.common.base.Preconditions;

public class MainController extends BaseController {

    private final MovieController mMovieController;

    public MainController(MovieController movieController) {
        mMovieController = Preconditions.checkNotNull(movieController,
                "movieController cannot be null");
    }

    @Override
    protected void onInited() {
        mMovieController.init();
    }

    @Override
    protected void onSuspended() {
        mMovieController.suspend();
    }

    public final MovieController getMovieController() {
        return mMovieController;
    }
}
