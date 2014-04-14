package app.philm.in.fragments.base;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.View;
import android.widget.GridView;

import java.util.List;

import app.philm.in.R;
import app.philm.in.adapters.MovieGridAdapter;
import app.philm.in.lib.model.ListItem;
import app.philm.in.lib.model.PhilmMovie;

public abstract class MovieGridFragment extends BasePhilmMovieListFragment<GridView> {

    private MovieGridAdapter mMovieGridAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mMovieGridAdapter = new MovieGridAdapter(getActivity());
        setListAdapter(mMovieGridAdapter);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final GridView gridView = getListView();
        final Resources res = getResources();

        gridView.setNumColumns(GridView.AUTO_FIT);
        gridView.setColumnWidth(res.getDimensionPixelSize(R.dimen.movie_grid_item_width));
        gridView.setHorizontalSpacing(res.getDimensionPixelSize(R.dimen.movie_grid_spacing));
        gridView.setVerticalSpacing(res.getDimensionPixelSize(R.dimen.movie_grid_spacing));
        gridView.setFastScrollAlwaysVisible(true);
        gridView.setDrawSelectorOnTop(true);
    }

    @Override
    public void onListItemClick(GridView l, View v, int position, long id) {
        if (hasCallbacks()) {
            ListItem<PhilmMovie> item = (ListItem<PhilmMovie>) l.getItemAtPosition(position);
            if (item.getType() == ListItem.TYPE_ITEM) {
                getCallbacks().showMovieDetail(item.getItem());
            }
        }
    }

    @Override
    public void populateInsets(Rect insets) {
        super.populateInsets(insets);

        final int spacing = getResources().getDimensionPixelSize(R.dimen.movie_grid_spacing);
        getListView().setPadding(
                insets.left + spacing,
                insets.top + spacing,
                insets.right + spacing,
                insets.bottom + spacing);
    }

    @Override
    public void setItems(List<ListItem<PhilmMovie>> items) {
        mMovieGridAdapter.setItems(items);
        moveListViewToSavedPositions();
    }

    @Override
    public String getRequestParameter() {
        return null;
    }

    @Override
    public boolean isModal() {
        return false;
    }

    @Override
    protected GridView createListView(Context context) {
        return new GridView(context);
    }

}
