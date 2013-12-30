package app.philm.in.adapters;

import com.hb.views.PinnedSectionListView;
import com.jakewharton.trakt.entities.Movie;
import com.jakewharton.trakt.entities.Ratings;
import com.squareup.picasso.Picasso;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import app.philm.in.Constants;
import app.philm.in.R;
import app.philm.in.controllers.MovieController;
import app.philm.in.model.ListItem;
import app.philm.in.model.PhilmMovie;
import app.philm.in.trakt.TraktImageHelper;
import app.philm.in.util.PhilmCollections;

public class MovieSectionedListAdapter extends BaseAdapter implements
        PinnedSectionListView.PinnedSectionListAdapter {

    private static final String LOG_TAG = MovieSectionedListAdapter.class.getSimpleName();

    private final Activity mActivity;
    private final TraktImageHelper mTraktImageHelper;
    private final DateFormat mDateFormat;

    private List<ListItem<PhilmMovie>> mItems;

    public MovieSectionedListAdapter(Activity activity) {
        mActivity = activity;
        mTraktImageHelper = new TraktImageHelper(activity.getResources());
        mDateFormat = android.text.format.DateFormat.getMediumDateFormat(activity);
    }

    public void setItems(List<ListItem<PhilmMovie>> listItems) {
        mItems = listItems;
        notifyDataSetChanged();
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
                title.setText(mActivity.getString(R.string.movie_title_year, movie.getTitle(),
                        movie.getYear()));

                final TextView ratingTextView = (TextView) view.findViewById(R.id.textview_rating);
                ratingTextView.setText(mActivity.getString(R.string.movie_rating_votes,
                        movie.getRatingPercent() != 0 ? String.valueOf(movie.getRatingPercent()) : "?",
                        movie.getRatingVotes()));

                final TextView release = (TextView) view.findViewById(R.id.textview_release);
                release.setText(mActivity.getString(R.string.movie_release_date,
                        mDateFormat.format(movie.getReleasedTime())));

                final ImageView imageView = (ImageView) view.findViewById(R.id.imageview_poster);
                Picasso.with(mActivity)
                        .load(mTraktImageHelper.getPosterUrl(item.getItem()))
                        .into(imageView);

                break;
            }
            case ListItem.TYPE_SECTION:
                ((TextView) view).setText(item.getSectionTitle());
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
