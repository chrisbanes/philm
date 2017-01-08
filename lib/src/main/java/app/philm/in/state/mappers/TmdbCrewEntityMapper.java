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

package app.philm.in.state.mappers;

import com.uwetrottmann.tmdb.entities.CrewMember;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import app.philm.in.model.PhilmMovieCredit;
import app.philm.in.model.PhilmPerson;
import app.philm.in.state.MoviesState;

@Singleton
public class TmdbCrewEntityMapper extends BaseEntityMapper<CrewMember, PhilmPerson> {

    @Inject
    public TmdbCrewEntityMapper(MoviesState state) {
        super(state);
    }

    @Override
    public PhilmPerson map(CrewMember entity) {
        PhilmPerson item = getEntity(String.valueOf(entity.id));

        if (item == null) {
            // No item, so create one
            item = new PhilmPerson();
        }

        // We already have a movie, so just update it wrapped value
        item.setFromTmdb(entity);
        putEntity(item);

        return item;
    }

    public List<PhilmMovieCredit> mapCredits(List<CrewMember> entities) {
        final ArrayList<PhilmMovieCredit> credits = new ArrayList<>(entities.size());
        for (CrewMember entity : entities) {
            credits.add(new PhilmMovieCredit(map(entity), entity.job, entity.department));
        }
        Collections.sort(credits);
        return credits;
    }

    @Override
    PhilmPerson getEntity(String id) {
        return mMoviesState.getPeople().get(id);
    }

    @Override
    void putEntity(PhilmPerson entity) {
        mMoviesState.getPeople().put(String.valueOf(entity.getTmdbId()), entity);
    }
}
