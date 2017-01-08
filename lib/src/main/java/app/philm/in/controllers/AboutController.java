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

package app.philm.in.controllers;


import com.google.common.base.Preconditions;

import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import app.philm.in.Display;
import app.philm.in.lib.R;
import app.philm.in.util.StringFetcher;

@Singleton
public class AboutController extends BaseUiController<AboutController.AboutUi,
        AboutController.AboutUiCallbacks> {

    public static enum AboutItem {
        BUILD_VERSION, BUILD_TIME, OPEN_SOURCE, POWERED_BY_TMDB, POWERED_BY_TRAKT;
    }

    public interface AboutUi extends BaseUiController.Ui<AboutUiCallbacks> {}

    public interface AboutListUi extends AboutUi {
        void setItems(List<AboutItem> items);
    }

    public interface AboutOpenSourcesUi extends AboutUi {
        void showLicences(String url);
    }

    public interface AboutUiCallbacks {
        void onTitleChanged(String newTitle);
        void onItemClick(AboutItem item);
    }

    private final StringFetcher mStringFetcher;

    @Inject
    public AboutController(StringFetcher stringFetcher) {
        mStringFetcher = Preconditions.checkNotNull(stringFetcher, "stringFetcher cannot be null");
    }

    @Override
    protected AboutUiCallbacks createUiCallbacks(AboutUi ui) {
        return new AboutUiCallbacks() {
            @Override
            public void onTitleChanged(String newTitle) {
                updateDisplayTitle(newTitle);
            }

            @Override
            public void onItemClick(AboutItem item) {
                switch (item) {
                    case OPEN_SOURCE:
                        Display display = getDisplay();
                        if (display != null) {
                            display.showLicencesFragment();
                        }
                        break;
                }
            }
        };
    }

    @Override
    protected void onUiAttached(AboutUi ui) {
        super.onUiAttached(ui);
    }

    @Override
    protected void populateUi(AboutUi ui) {
        if (ui instanceof AboutListUi) {
            ((AboutListUi) ui).setItems(Arrays.asList(AboutItem.values()));
        } else if (ui instanceof AboutOpenSourcesUi) {
            ((AboutOpenSourcesUi) ui).showLicences("file:///android_asset/licences.html");
        }
    }

    @Override
    protected String getUiTitle(AboutUi ui) {
        if (ui instanceof AboutListUi) {
            return mStringFetcher.getString(R.string.about_title);
        } else if (ui instanceof AboutOpenSourcesUi) {
            return mStringFetcher.getString(R.string.about_open_source_title);
        }
        return null;
    }
}
