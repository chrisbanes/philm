package app.philm.in.adapters;

import com.google.common.base.Objects;

import android.app.Activity;
import android.graphics.Bitmap;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

import app.philm.in.R;
import app.philm.in.model.ListItem;
import app.philm.in.model.PhilmMovie;
import app.philm.in.view.PhilmImageView;

public class MovieGridAdapter extends BaseAdapter {

    private static final String LOG_TAG = MovieGridAdapter.class.getSimpleName();

    private final Activity mActivity;
    private List<ListItem<PhilmMovie>> mItems;

    public MovieGridAdapter(Activity activity) {
        mActivity = activity;
    }

    public void setItems(List<ListItem<PhilmMovie>> items) {
        if (!Objects.equal(items, mItems)) {
            mItems = items;
            notifyDataSetChanged();
        }
    }

    @Override
    public int getCount() {
        return mItems != null ? mItems.size() : 0;
    }

    @Override
    public ListItem<PhilmMovie> getItem(int position) {
        return mItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup viewGroup) {
        View view = convertView;
        if (view == null) {
            view = mActivity.getLayoutInflater().inflate(R.layout.item_grid_movie, viewGroup, false);
        }

        final PhilmMovie movie = getItem(position).getItem();

        final TextView title = (TextView) view.findViewById(R.id.textview_title);
        title.setText(movie.getTitle());
        title.setVisibility(View.VISIBLE);

        final PhilmImageView imageView = (PhilmImageView) view.findViewById(R.id.imageview_poster);
        imageView.loadPosterUrl(movie, new PhilmImageView.Listener() {
            @Override
            public void onSuccess(Bitmap bitmap) {
                title.setVisibility(View.GONE);
            }

            @Override
            public void onError() {
                title.setVisibility(View.VISIBLE);
            }
        });

        return view;
    }
}
