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

package app.philm.in.state;

import java.util.Collection;
import java.util.List;

import app.philm.in.model.PhilmMovie;
import app.philm.in.model.PhilmUserProfile;

public interface AsyncDatabaseHelper {

    public void mergeLibrary(List<PhilmMovie> library);

    public void mergeWatchlist(List<PhilmMovie> watchlist);

    public void getWatchlist(Callback<List<PhilmMovie>> callback);

    public void getLibrary(Callback<List<PhilmMovie>> callback);

    public void put(Collection<PhilmMovie> movies);

    public void put(PhilmMovie movie);

    public void delete(Collection<PhilmMovie> movies);

    public void getUserProfile(String username, Callback<PhilmUserProfile> callback);

    public void put(PhilmUserProfile profile);

    public void delete(PhilmUserProfile profile);

    public void close();

    public void deleteAllPhilmMovies();

    public interface Callback<T> {
        public void onFinished(T result);
    }

}
