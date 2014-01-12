package app.philm.in.fragments.base;


import android.support.v4.app.DialogFragment;

import app.philm.in.PhilmApplication;
import app.philm.in.R;
import app.philm.in.controllers.MovieController;
import app.philm.in.network.NetworkError;
import app.philm.in.view.StringManager;
import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;


public abstract class BasePhilmMovieDialogFragment extends DialogFragment
        implements MovieController.MovieUi {

    private MovieController.MovieUiCallbacks mCallbacks;
    private Crouton mCurrentCrouton;

    @Override
    public void onResume() {
        super.onResume();
        getController().attachUi(this);
    }

    @Override
    public void onPause() {
        cancelCrouton();
        getController().detachUi(this);
        super.onPause();
    }

    @Override
    public void showLoadingProgress(boolean visible) {
        // TODO: Implement
    }

    @Override
    public void showError(NetworkError error) {
        showCrouton(StringManager.getStringResId(error), Style.ALERT);
    }

    protected final void cancelCrouton() {
        if (mCurrentCrouton != null) {
            mCurrentCrouton.cancel();
        }
    }

    protected final void showCrouton(int text, Style style) {
        cancelCrouton();
        mCurrentCrouton = Crouton.makeText(getActivity(), text, style);
        mCurrentCrouton.show();
    }

    protected final boolean hasCallbacks() {
        return mCallbacks != null;
    }

    protected final MovieController.MovieUiCallbacks getCallbacks() {
        return mCallbacks;
    }

    @Override
    public void setCallbacks(MovieController.MovieUiCallbacks callbacks) {
        mCallbacks = callbacks;
    }

    protected String getTitle() {
        switch (getMovieQueryType()) {
            case LIBRARY:
                return getString(R.string.library_title);
            case TRENDING:
                return getString(R.string.trending_title);
            case WATCHLIST:
                return getString(R.string.watchlist_title);
        }
        return null;
    }

    private MovieController getController() {
        return PhilmApplication.from(getActivity()).getMainController().getMovieController();
    }

}
