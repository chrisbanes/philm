/*
 * Copyright 2014 Chris Banes
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package app.philm.in.adapters;

import com.google.common.base.Objects;

import android.app.Activity;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
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
    private final LayoutInflater mLayoutInflater;

    private List<ListItem<PhilmMovie>> mItems;

    public MovieGridAdapter(Activity activity) {
        mActivity = activity;
        mLayoutInflater = mActivity.getLayoutInflater();
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
            view = mLayoutInflater.inflate(R.layout.item_grid_movie, viewGroup, false);
        }

        final PhilmMovie movie = getItem(position).getListItem();

        final TextView title = (TextView) view.findViewById(R.id.textview_title);
        title.setText(movie.getTitle());
        title.setVisibility(View.VISIBLE);

        final PhilmImageView imageView = (PhilmImageView) view.findViewById(R.id.imageview_poster);
        imageView.setAutoFade(false);
        imageView.loadPoster(movie, new PhilmImageView.Listener() {
            @Override
            public void onSuccess(PhilmImageView imageView, Bitmap bitmap) {
                title.setVisibility(View.GONE);
            }

            @Override
            public void onError(PhilmImageView imageView) {
                title.setVisibility(View.VISIBLE);
            }
        });

        return view;
    }
}
