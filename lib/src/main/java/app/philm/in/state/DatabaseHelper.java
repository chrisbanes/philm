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

public interface DatabaseHelper {

    List<PhilmMovie> getLibrary();

    List<PhilmMovie> getWatchlist();

    void put(PhilmMovie movie);

    void put(Collection<PhilmMovie> movies);

    void delete(Collection<PhilmMovie> movies);

    PhilmUserProfile getUserProfile(String username);

    void put(PhilmUserProfile profile);

    void delete(PhilmUserProfile profile);

    void deleteAllPhilmMovies();

    void close();

    boolean isClosed();
}
