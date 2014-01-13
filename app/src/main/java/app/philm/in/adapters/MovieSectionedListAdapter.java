package app.philm.in.adapters;

import com.google.common.base.Objects;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;

import app.philm.in.Container;
import app.philm.in.R;
import app.philm.in.model.ListItem;
import app.philm.in.model.PhilmMovie;
import app.philm.in.view.PhilmImageView;
import app.philm.in.view.PinnedSectionListView;
import app.philm.in.view.StringManager;

public class MovieSectionedListAdapter extends BaseAdapter implements
        PinnedSectionListView.PinnedSectionListAdapter {

    private static final String LOG_TAG = MovieSectionedListAdapter.class.getSimpleName();

    private final Activity mActivity;
    private final Date mDate;

    private List<ListItem<PhilmMovie>> mItems;

    public MovieSectionedListAdapter(Activity activity) {
        mActivity = activity;
        mDate = new Date();
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
        final ListItem<PhilmMovie> item = getItem(position);
        View view = convertView;

        if (view == null) {
            final int layout = item.getType() == ListItem.TYPE_ITEM
                    ? R.layout.item_list_movie
                    : R.layout.item_list_movie_section_header;
            view = mActivity.getLayoutInflater().inflate(layout, viewGroup, false);
        }

        switch (item.getType()) {
            case ListItem.TYPE_ITEM: {
                PhilmMovie movie = item.getItem();

                final TextView title = (TextView) view.findViewById(R.id.textview_title);
                title.setText(mActivity.getString(R.string.movie_title_year,
                        movie.getTitle(), movie.getYear()));

                final TextView ratingTextView = (TextView) view.findViewById(R.id.textview_rating);
                ratingTextView.setText(mActivity.getString(R.string.movie_rating_votes,
                        movie.getRatingPercent(), movie.getRatingVotes()));

                final TextView release = (TextView) view.findViewById(R.id.textview_release);
                DateFormat dateFormat = Container.getInstance(mActivity).getMediumDateFormat();
                mDate.setTime(movie.getReleasedTime());
                release.setText(mActivity.getString(R.string.movie_release_date,
                        dateFormat.format(mDate)));

                final PhilmImageView imageView =
                        (PhilmImageView) view.findViewById(R.id.imageview_poster);
                imageView.loadPosterUrl(movie);
                break;
            }
            case ListItem.TYPE_SECTION:
                ((TextView) view).setText(StringManager.getStringResId(item.getFilter()));
                break;
        }

        return view;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public int getItemViewType(int position) {
        return getItem(position).getType();
    }

    @Override
    public boolean isItemViewTypePinned(int type) {
        return type == ListItem.TYPE_SECTION;
    }
}
