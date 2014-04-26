package app.philm.in.fragments.base;

import com.github.johnpersano.supertoasts.SuperCardToast;
import com.github.johnpersano.supertoasts.SuperToast;
import com.github.johnpersano.supertoasts.util.Style;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;

import app.philm.in.Constants;
import app.philm.in.PhilmApplication;
import app.philm.in.R;
import app.philm.in.controllers.MovieController;
import app.philm.in.model.ColorScheme;
import app.philm.in.network.NetworkError;
import app.philm.in.view.StringManager;


public abstract class BaseMovieControllerListFragment<E extends AbsListView, T>
        extends ListFragment<E>
        implements MovieController.BaseMovieListUi<T>, AbsListView.OnScrollListener {

    private static final String LOG_TAG = BaseMovieControllerListFragment.class.getSimpleName();

    private MovieController.MovieUiCallbacks mCallbacks;

    private SuperCardToast mToast;

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
        cancelToast();
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
        final E list = getListView();
        if (mFirstVisiblePosition != AdapterView.INVALID_POSITION
                && list.getFirstVisiblePosition() <= 0) {
            list.post(new Runnable() {
                @Override
                public void run() {
                    list.setSelection(mFirstVisiblePosition);
                }
            });
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
            case UNAUTHORIZED_TRAKT:
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

    protected final void cancelToast() {
        if (mToast != null) {
            mToast.dismiss();
        }
    }

    protected final void showToast(int text, Style style) {
        cancelToast();

        mToast = SuperCardToast.create(
                getActivity(), getText(text), SuperToast.Duration.MEDIUM, style);
        mToast.setIcon(SuperToast.Icon.Dark.INFO, SuperToast.IconPosition.LEFT);
        mToast.show();
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
        MovieController.MovieQueryType queryType = getMovieQueryType();
        if (queryType != null) {
            return getString(StringManager.getStringResId(queryType));
        }
        return null;
    }

    @Override
    public void setColorScheme(ColorScheme colorScheme) {
        // NO-OP
    }
}
