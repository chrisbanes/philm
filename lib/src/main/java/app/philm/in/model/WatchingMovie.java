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

import com.google.common.base.Preconditions;
import com.jakewharton.trakt.enumerations.ActivityAction;

public class WatchingMovie {

    public enum Type {
        CHECKIN, SCROBBLE, WATCHING
    }

    public final PhilmMovie movie;
    public final Type type;
    public final long startTime;
    public final long endTime;
    public final long duration;

    public WatchingMovie(PhilmMovie movie, Type type, long startTime, long duration) {
        this.movie = Preconditions.checkNotNull(movie, "movie cannot be null");
        this.type = Preconditions.checkNotNull(type, "type cannot be null");
        this.startTime = startTime;
        this.duration = duration;
        this.endTime = startTime + duration;
    }

    public static boolean validAction(ActivityAction action) {
        return from(action) != null;
    }

    public static Type from(ActivityAction action) {
        switch (action) {
            case Checkin:
                return Type.CHECKIN;
            case Scrobble:
                return Type.SCROBBLE;
            case Watching:
                return Type.WATCHING;
        }
        return null;
    }
}
