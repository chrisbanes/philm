package app.philm.in.fragments.base;


import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;

import app.philm.in.lib.Constants;
import app.philm.in.R;
import app.philm.in.view.MovieDetailCardLayout;
import app.philm.in.view.PhilmImageView;
import app.philm.in.view.ViewRecycler;

public abstract class BaseDetailFragment extends BasePhilmMovieFragment {

    private ListView mListView;
    private ListAdapter mAdapter;

    private TextView mEmptyView;
    private PhilmImageView mBigPosterImageView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_detail_list, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mAdapter = createListAdapter();

        mListView = (ListView) view.findViewById(android.R.id.list);
        mListView.setAdapter(mAdapter);

        mEmptyView = (TextView) view.findViewById(android.R.id.empty);
        mListView.setEmptyView(mEmptyView);

        mBigPosterImageView = (PhilmImageView) view.findViewById(R.id.imageview_poster);
    }

    protected void setEmptyText(int stringId) {
        if (mEmptyView != null) {
            mEmptyView.setText(stringId);
        }
    }

    protected PhilmImageView getBigPosterView() {
        return mBigPosterImageView;
    }

    protected boolean hasBigPosterView() {
        return mBigPosterImageView != null;
    }

    @Override
    public void showLoadingProgress(boolean visible) {
        getActivity().setProgressBarIndeterminateVisibility(visible);
    }

    @Override
    public void populateInsets(Rect insets) {
        getListView().setPadding(insets.left, insets.top, insets.right, insets.bottom);
    }

    protected abstract ListAdapter createListAdapter();

    protected ListView getListView() {
        return mListView;
    }

    protected ListAdapter getListAdapter() {
        return mAdapter;
    }

    protected interface DetailType<E> {

        public String name();

        public int ordinal();

        public int getLayoutId();

        public int getViewType();

    }

    protected abstract class BaseDetailAdapter<E extends DetailType> extends BaseAdapter {

        private List<E> mListItems;

        public void setItems(List<E> listItems) {
            mListItems = listItems;
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return mListItems != null ? mListItems.size() : 0;
        }

        @Override
        public E getItem(int position) {
            return mListItems.get(position);
        }

        @Override
        public long getItemId(int position) {
            return getItem(position).ordinal();
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }

        @Override
        public abstract int getViewTypeCount();

        @Override
        public int getItemViewType(int position) {
            return getItem(position).getViewType();
        }

        @Override
        public boolean isEnabled(int position) {
            return false;
        }

        @Override
        public boolean areAllItemsEnabled() {
            return false;
        }

        @Override
        public View getView(int position, View view, ViewGroup viewGroup) {
            final E item = getItem(position);

            if (view == null) {
                final LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
                view = inflater.inflate(item.getLayoutId(), viewGroup, false);
            }

            // Now bind to the view
            bindView(item, view);

            return view;
        }

        protected abstract void bindView(final E item, final View view);

        protected void populateDetailGrid(
                final ViewGroup layout,
                final MovieDetailCardLayout cardLayout,
                final View.OnClickListener seeMoreClickListener,
                final BaseAdapter adapter) {

            if (layout.getWidth() == 0) {
                layout.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
                    @Override
                    public void onLayoutChange(View v, int left, int top, int right, int bottom,
                            int oldLeft, int oldTop, int oldRight, int oldBottom) {
                        layout.post(new Runnable() {
                            @Override
                            public void run() {
                                populateDetailGrid(layout, cardLayout, seeMoreClickListener, adapter);
                            }
                        });
                        layout.removeOnLayoutChangeListener(this);
                    }
                });
                return;
            }

            final ViewRecycler viewRecycler = new ViewRecycler(layout);
            viewRecycler.recycleViews();

            if (!adapter.isEmpty()) {
                final int numItems = layout.getWidth() / mListView.getResources()
                        .getDimensionPixelSize(R.dimen.detail_card_item_width);
                final int adapterCount = adapter.getCount();
                final int gridSpacing = getResources().getDimensionPixelSize(R.dimen.movie_grid_spacing);

                for (int i = 0; i < Math.min(numItems, adapterCount); i++) {
                    View view = adapter.getView(i, viewRecycler.getRecycledView(), layout);
                    LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) view.getLayoutParams();

                    if (lp.weight > .1f && adapterCount < numItems) {
                        lp.weight = 0f;
                        lp.width = layout.getWidth() / numItems;
                    }

                    lp.rightMargin = i < (numItems - 1) ? gridSpacing : 0;

                    layout.addView(view);
                }

                final boolean showSeeMore = numItems < adapter.getCount();
                cardLayout.setSeeMoreVisibility(showSeeMore);
                cardLayout.setSeeMoreOnClickListener(showSeeMore ? seeMoreClickListener : null);
            }

            viewRecycler.clearRecycledViews();
        }

        protected void rebindView(final E item) {
            if (Constants.DEBUG) {
                Log.d(getClass().getSimpleName(), "rebindView. Item: " + item.name());
            }

            ListView listView = getListView();

            for (int i = 0, z = listView.getChildCount(); i < z; i++) {
                View child = listView.getChildAt(i);
                if (child != null && child.getTag() == item) {
                    bindView(item, child);
                    return;
                }
            }
        }
    }

}
