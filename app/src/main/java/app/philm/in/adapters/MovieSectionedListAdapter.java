package app.philm.in.adapters;

import com.hb.views.PinnedSectionListView;
import com.jakewharton.trakt.entities.Movie;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import app.philm.in.R;
import app.philm.in.trakt.TraktImageHelper;
import app.philm.in.util.PhilmCollections;

public class MovieSectionedListAdapter extends BaseAdapter implements
        PinnedSectionListView.PinnedSectionListAdapter {

    private static final String LOG_TAG = MovieSectionedListAdapter.class.getSimpleName();

    private static final class Item {
        static final int TYPE_ITEM = 0;
        static final int TYPE_SECTION = 1;

        private final Movie movie;
        private final int type;

        Item(int type, Movie movie) {
            this.type = type;
            this.movie = movie;
        }
    }

    private final Activity mActivity;
    private final TraktImageHelper mTraktImageHelper;
    private List<Item> mItems;

    public MovieSectionedListAdapter(Activity activity) {
        mActivity = activity;
        mTraktImageHelper = new TraktImageHelper(activity.getResources());
    }

    public void setItems(List<Movie> items) {
        if (PhilmCollections.isEmpty(items)) {
            mItems = null;
        } else {
            mItems = new ArrayList<Item>();
            for (Movie movie : items) {
                mItems.add(new Item(Item.TYPE_ITEM, movie));
            }
        }
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mItems != null ? mItems.size() : 0;
    }

    @Override
    public Item getItem(int position) {
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
            view = mActivity.getLayoutInflater().inflate(R.layout.item_list_movie, viewGroup, false);
        }

        final Item item = getItem(position);

        if (item.type == Item.TYPE_ITEM) {
            final TextView title = (TextView) view.findViewById(R.id.textview_title);
            title.setText(item.movie.title);

            final TextView rating = (TextView) view.findViewById(R.id.textview_rating);
            rating.setText(item.movie.ratings.percentage + "%");

            final TextView votes = (TextView) view.findViewById(R.id.textview_rating_votes);
            int numberVotes = item.movie.ratings.votes;
            votes.setText(mActivity.getResources()
                    .getQuantityString(R.plurals.ratings, numberVotes, numberVotes));

            final ImageView imageView = (ImageView) view.findViewById(R.id.imageview_poster);
            Picasso.with(mActivity)
                    .load(mTraktImageHelper.getPosterUrl(item.movie))
                    .into(imageView);
        }

        return view;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public int getItemViewType(int position) {
        return getItem(position).type;
    }

    @Override
    public boolean isItemViewTypePinned(int position) {
        return getItemViewType(position) == Item.TYPE_SECTION;
    }
}
