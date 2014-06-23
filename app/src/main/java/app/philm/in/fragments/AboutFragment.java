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

package app.philm.in.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ListView;

import java.util.List;

import app.philm.in.PhilmApplication;
import app.philm.in.R;
import app.philm.in.adapters.AboutListAdapter;
import app.philm.in.controllers.AboutController;
import app.philm.in.fragments.base.ListFragment;


public class AboutFragment extends ListFragment<ListView>
        implements AboutController.AboutListUi {

    private AboutController.AboutUiCallbacks mCallbacks;
    private AboutListAdapter mAboutListAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAboutListAdapter = new AboutListAdapter(getActivity());
        setListAdapter(mAboutListAdapter);
    }

    @Override
    public void onResume() {
        super.onResume();
        getController().attachUi(this);
    }

    @Override
    public void onPause() {
        getController().detachUi(this);
        super.onPause();
    }

    protected final boolean hasCallbacks() {
        return mCallbacks != null;
    }

    @Override
    public boolean isModal() {
        return false;
    }

    protected final AboutController.AboutUiCallbacks getCallbacks() {
        return mCallbacks;
    }

    @Override
    public void setCallbacks(AboutController.AboutUiCallbacks callbacks) {
        mCallbacks = callbacks;
    }

    private AboutController getController() {
        return PhilmApplication.from(getActivity()).getMainController().getAboutController();
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        AboutController.AboutItem item = (AboutController.AboutItem) l.getItemAtPosition(position);
        if (item != null && hasCallbacks()) {
            getCallbacks().onItemClick(item);
        }
    }

    @Override
    public void setItems(List<AboutController.AboutItem> items) {
        mAboutListAdapter.setItems(items);
    }

    @Override
    protected ListView createListView(Context context, LayoutInflater inflater) {
        return (ListView) inflater.inflate(R.layout.view_list, null);
    }
}
