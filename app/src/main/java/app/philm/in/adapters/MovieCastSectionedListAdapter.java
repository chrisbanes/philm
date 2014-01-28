package app.philm.in.adapters;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.google.common.base.Objects;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;

import app.philm.in.R;
import app.philm.in.model.ListItem;
import app.philm.in.model.PhilmCast;
import app.philm.in.view.PhilmImageView;
import app.philm.in.view.PinnedSectionListView;
import app.philm.in.view.StringManager;

public class MovieCastSectionedListAdapter extends BaseAdapter implements
        PinnedSectionListView.PinnedSectionListAdapter {

    private static final String LOG_TAG = MovieCastSectionedListAdapter.class.getSimpleName();

    private final Activity mActivity;

    private List<ListItem<PhilmCast>> mItems;

    public MovieCastSectionedListAdapter(Activity activity) {
        mActivity = activity;
    }

    public void setItems(List<ListItem<PhilmCast>> items) {
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
    public ListItem<PhilmCast> getItem(int position) {
        return mItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup viewGroup) {
        final ListItem<PhilmCast> item = getItem(position);
        View view = convertView;

        if (view == null) {
            final int layout = item.getType() == ListItem.TYPE_ITEM
                    ? R.layout.item_list_movie_cast
                    : R.layout.item_list_movie_section_header;
            view = mActivity.getLayoutInflater().inflate(layout, viewGroup, false);
        }

        switch (item.getType()) {
            case ListItem.TYPE_ITEM: {
                PhilmCast castMember = item.getItem();

                final TextView nameTextView = (TextView) view.findViewById(R.id.textview_name);
                nameTextView.setText(castMember.getName());

                final TextView characterTextView =
                        (TextView) view.findViewById(R.id.textview_character);
                characterTextView.setText(castMember.getCharacter());

                final PhilmImageView imageView =
                        (PhilmImageView) view.findViewById(R.id.imageview_profile);
                imageView.loadProfileUrl(castMember);
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
