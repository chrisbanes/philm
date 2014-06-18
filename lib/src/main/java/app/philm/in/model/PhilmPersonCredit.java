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

package app.philm.in.model;

import com.uwetrottmann.tmdb.entities.PersonCredits;

import java.util.Comparator;

public class PhilmPersonCredit extends PhilmModel {

    public static final Comparator<PhilmPersonCredit> COMPARATOR_SORT_DATE
            = new Comparator<PhilmPersonCredit>() {
        @Override
        public int compare(PhilmPersonCredit movie, PhilmPersonCredit movie2) {
            if (movie.releaseDate > movie2.releaseDate) {
                return -1;
            } else if (movie.releaseDate < movie2.releaseDate) {
                return 1;
            }
            return 0;
        }
    };

    final String title;
    final int id;
    final String job;
    final String posterPath;
    final long releaseDate;

    public PhilmPersonCredit(PersonCredits.CastCredit credit) {
        this.id = credit.id;
        this.title = credit.title;
        this.posterPath = credit.poster_path;
        this.job = credit.character;
        this.releaseDate = credit.release_date != null ? credit.release_date.getTime() : 0;
    }

    public PhilmPersonCredit(PersonCredits.CrewCredit credit) {
        this.id = credit.id;
        this.title = credit.title;
        this.posterPath = credit.poster_path;
        this.job = credit.job;
        this.releaseDate = credit.release_date != null ? credit.release_date.getTime() : 0;
    }

    public String getTitle() {
        return title;
    }

    public int getId() {
        return id;
    }

    public String getJob() {
        return job;
    }

    public String getPosterPath() {
        return posterPath;
    }

    public long getReleaseDate() {
        return releaseDate;
    }
}
