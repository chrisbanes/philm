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

package app.philm.in;

import android.os.Bundle;

import app.philm.in.model.ColorScheme;

public interface Display {

    public static final String FRAGMENT_TAG_RATE_MOVIE = "rate_movie";
    public static final String FRAGMENT_TAG_CHECKIN_MOVIE = "checkin_movie";
    public static final String FRAGMENT_TAG_TRAKT_CREDENTIALS_WRONG = "trakt_credentials_wrong";

    public static final String PARAM_ID = "_id";

    public void showLibrary();

    public void showTrending();

    public void showDiscover();

    public void showWatchlist();

    public void showLogin();

    public void startMovieDetailActivity(String movieId, Bundle bundle);

    public void showMovieDetailFragment(String movieId);

    public void startMovieImagesActivity(String movieId);

    public void showMovieImagesFragment(String movieId);

    public void showSearchFragment();

    public void showSearchMoviesFragment();

    public void showSearchPeopleFragment();

    public void showAboutFragment();

    public void showLicencesFragment();

    public void showRateMovieFragment(String movieId);

    public void closeDrawerLayout();

    public boolean hasMainFragment();

    public void startAddAccountActivity();

    public void startAboutActivity();

    public void setActionBarTitle(CharSequence title);

    public void setActionBarSubtitle(CharSequence title);

    public boolean popEntireFragmentBackStack();

    public void showUpNavigation(boolean show);

    public void finishActivity();

    public void showSettings();

    public void showRelatedMovies(String movieId);

    public void showCastList(String movieId);

    public void showCrewList(String movieId);

    public void showCheckin(String movieId);

    public void showCancelCheckin();

    public void startPersonDetailActivity(String id, Bundle bundle);

    public void showPersonDetail(String id);

    public void showPersonCastCredits(String id);

    public void showPersonCrewCredits(String id);

    public void showCredentialsChanged();

    public void playYoutubeVideo(String id);

    public void setColorScheme(ColorScheme colorScheme);

}
