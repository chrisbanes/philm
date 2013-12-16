package app.philm.in.adapters;

import com.jakewharton.trakt.entities.Movie;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import app.philm.in.Constants;
import app.philm.in.Container;
import app.philm.in.R;
import app.philm.in.util.TraktImageHelper;

public class MovieGridAdapter extends BaseAdapter {

    private static final String LOG_TAG = MovieGridAdapter.class.getSimpleName();

    private final Activity mActivity;
    private final TraktImageHelper mTraktImageHelper;
    private List<Movie> mItems;

    public MovieGridAdapter(Activity activity) {
        mActivity = activity;
        mTraktImageHelper = new TraktImageHelper(activity.getResources());
    }

    public void setItems(List<Movie> items) {
        mItems = items;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mItems != null ? mItems.size() : 0;
    }

    @Override
    public Movie getItem(int position) {
        return mItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return getItem(position).tmdbId.hashCode();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup viewGroup) {
        View view = convertView;
        if (view == null) {
            view = mActivity.getLayoutInflater().inflate(R.layout.item_grid_movie, viewGroup, false);
        }

        final Movie movie = getItem(position);

        ImageView imageView = (ImageView) view.findViewById(R.id.imageview_poster);
        Picasso.with(mActivity)
                .load(mTraktImageHelper.getPosterUrl(movie))
                .into(imageView);

        TextView title = (TextView) view.findViewById(R.id.textview_title);
        title.setText(movie.title);

        return view;
    }
}
