package app.philm.in.adapters;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.common.base.Objects;
import com.hb.views.PinnedSectionListView;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import java.text.DateFormat;
import java.util.List;

import app.philm.in.R;
import app.philm.in.controllers.AboutController;
import app.philm.in.model.ListItem;
import app.philm.in.model.PhilmMovie;
import app.philm.in.trakt.TraktImageHelper;
import app.philm.in.view.StringManager;

public class AboutListAdapter extends BaseAdapter {

    private static final String LOG_TAG = AboutListAdapter.class.getSimpleName();

    private final Activity mActivity;

    private List<AboutController.AboutItem> mItems;

    public AboutListAdapter(Activity activity) {
        mActivity = activity;
    }

    public void setItems(List<AboutController.AboutItem> items) {
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
    public AboutController.AboutItem getItem(int position) {
        return mItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup viewGroup) {
        final AboutController.AboutItem item = getItem(position);

        View view = convertView;
        if (view == null) {
            view = mActivity.getLayoutInflater().inflate(R.layout.item_about, viewGroup, false);
        }

        ((TextView) view.findViewById(android.R.id.text1))
                .setText(StringManager.getTitleResId(item));
        ((TextView) view.findViewById(android.R.id.text2))
                .setText(StringManager.getSubtitle(mActivity, item));

        return view;
    }
}
