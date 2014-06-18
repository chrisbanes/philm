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

package app.philm.in.fragments.base;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ListView;

import java.util.List;

import app.philm.in.R;
import app.philm.in.adapters.MovieCreditSectionedListAdapter;
import app.philm.in.controllers.MovieController;
import app.philm.in.model.ListItem;
import app.philm.in.model.PhilmMovieCredit;
import app.philm.in.util.ActivityTransitions;


public abstract class BaseMovieCreditListFragment
        extends BaseMovieControllerListFragment<ListView, PhilmMovieCredit>
        implements MovieController.MovieCreditListUi {

    private MovieCreditSectionedListAdapter mMovieListAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mMovieListAdapter = new MovieCreditSectionedListAdapter(getActivity());
        setListAdapter(mMovieListAdapter);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        if (hasCallbacks()) {
            ListItem<PhilmMovieCredit> item = (ListItem<PhilmMovieCredit>) l.getItemAtPosition(position);
            if (item.getType() == ListItem.TYPE_ITEM) {
                PhilmMovieCredit cast = item.getItem();
                if (cast != null && cast.getPerson() != null) {
                    getCallbacks().showPersonDetail(cast.getPerson(),
                            ActivityTransitions.scaleUpAnimation(v));
                }
            }
        }
    }

    @Override
    public void setItems(List<ListItem<PhilmMovieCredit>> items) {
        mMovieListAdapter.setItems(items);
    }

    @Override
    public boolean isModal() {
        return false;
    }

    @Override
    protected ListView createListView(Context context, LayoutInflater inflater) {
        return (ListView) inflater.inflate(R.layout.view_pinned_list, null);
    }
}
