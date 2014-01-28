package app.philm.in.fragments.base;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;

import app.philm.in.Constants;
import app.philm.in.PhilmApplication;
import app.philm.in.R;
import app.philm.in.controllers.MovieController;
import app.philm.in.network.NetworkError;
import app.philm.in.view.StringManager;
import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;


public abstract class BaseMovieControllerListFragment<E extends AbsListView, T>
        extends ListFragment<E>
        implements MovieController.BaseMovieListUi<T>, AbsListView.OnScrollListener {

    private static final String LOG_TAG = BaseMovieControllerListFragment.class.getSimpleName();

    private MovieController.MovieUiCallbacks mCallbacks;

    private Crouton mCurrentCrouton;

    private int mFirstVisiblePosition;
    private int mFirstVisiblePositionTop;

    private boolean mLoadMoreIsAtBottom;
    private int mLoadMoreRequestedItemCount;

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getListView().setOnScrollListener(this);
    }

    @Override
    public String getUiTitle() {
        return getString(StringManager.getStringResId(getMovieQueryType()));
    }

    @Override
    public void onResume() {
        super.onResume();
        getController().attachUi(this);
    }

    @Override
    public void onPause() {
        saveListViewPosition();
        cancelCrouton();
        getController().detachUi(this);
        super.onPause();
    }

    private void saveListViewPosition() {
        E listView = getListView();

        mFirstVisiblePosition = listView.getFirstVisiblePosition();

        if (mFirstVisiblePosition != AdapterView.INVALID_POSITION && listView.getChildCount() > 0) {
            mFirstVisiblePositionTop = listView.getChildAt(0).getTop();
        }
    }

    protected void moveListViewToSavedPositions() {
        if (mFirstVisiblePosition != AdapterView.INVALID_POSITION
                && getListView().getFirstVisiblePosition() <= 0) {
            getListView().setSelection(mFirstVisiblePosition);
        }
    }

    @Override
    public String getRequestParameter() {
        return null;
    }

    @Override
    public void showLoadingProgress(boolean visible) {
        if (visible) {
            setListShown(false);
        } else {
            setListShown(true);
        }
    }

    @Override
    public void showSecondaryLoadingProgress(boolean visible) {
        setSecondaryProgressShown(visible);
    }

    @Override
    public void showError(NetworkError error) {
        setListShown(true);
        switch (error) {
            case UNAUTHORIZED:
                setEmptyText(getString(R.string.empty_missing_account, getTitle()));
                break;
            case NETWORK_ERROR:
                setEmptyText(getString(R.string.empty_network_error, getTitle()));
                break;
            case UNKNOWN:
                setEmptyText(getString(R.string.empty_unknown_error, getTitle()));
                break;
        }
    }

    @Override
    public final void onScrollStateChanged(AbsListView view, int scrollState) {
        if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE && mLoadMoreIsAtBottom) {
            if (onScrolledToBottom()) {
                mLoadMoreRequestedItemCount = view.getCount();
                mLoadMoreIsAtBottom = false;
            }
        }
    }

    @Override
    public final void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount,
            int totalItemCount) {
        mLoadMoreIsAtBottom = totalItemCount > mLoadMoreRequestedItemCount
                && firstVisibleItem + visibleItemCount == totalItemCount;
    }

    protected boolean onScrolledToBottom() {
        if (Constants.DEBUG) {
            Log.d(LOG_TAG, "onScrolledToBottom");
        }
        if (hasCallbacks()) {
            getCallbacks().onScrolledToBottom();
            return true;
        }
        return false;
    }

    private void cancelCrouton() {
        if (mCurrentCrouton != null) {
            mCurrentCrouton.cancel();
        }
    }

    protected void showCrouton(int text, Style style) {
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

    private MovieController getController() {
        return PhilmApplication.from(getActivity()).getMainController().getMovieController();
    }

    private String getTitle() {
        switch (getMovieQueryType()) {
            case POPULAR:
                return getString(R.string.popular_title);
            case LIBRARY:
                return getString(R.string.library_title);
            case TRENDING:
                return getString(R.string.trending_title);
            case WATCHLIST:
                return getString(R.string.watchlist_title);
        }
        return null;
    }

}
