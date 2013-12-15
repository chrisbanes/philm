package app.philm.in.adapters;

import com.jakewharton.trakt.entities.Movie;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

public class MovieGridAdapter extends BaseAdapter {

    private final Activity mActivity;
    private List<Movie> mItems;

    public MovieGridAdapter(Activity activity) {
        mActivity = activity;
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
            view = mActivity.getLayoutInflater()
                    .inflate(android.R.layout.simple_list_item_1, viewGroup, false);
        }

        final Movie movie = getItem(position);

        TextView title = (TextView) view.findViewById(android.R.id.text1);
        title.setText(movie.title);

        return view;
    }
}
