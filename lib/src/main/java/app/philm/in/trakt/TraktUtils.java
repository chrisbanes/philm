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

package app.philm.in.trakt;

import com.jakewharton.trakt.enumerations.Rating;

public class TraktUtils {

    public static final int UNRATE = 0;

    public static int mapRatingToInt(Rating rating) {
        switch (rating) {
            default:
            case Unrate:
                return UNRATE;
            case WeakSauce:
                return 1;
            case Terrible:
                return 2;
            case Bad:
                return 3;
            case Poor:
                return 4;
            case Meh:
                return 5;
            case Fair:
                return 6;
            case Good:
                return 7;
            case Great:
                return 8;
            case Superb:
                return 9;
            case TotallyNinja:
                return 10;
        }
    }

    public static Rating mapIntToRating(int rating) {
        switch (rating) {
            default:
            case UNRATE:
                return Rating.Unrate;
            case 1:
                return Rating.WeakSauce;
            case 2:
                return Rating.Terrible;
            case 3:
                return Rating.Bad;
            case 4:
                return Rating.Poor;
            case 5:
                return Rating.Meh;
            case 6:
                return Rating.Fair;
            case 7:
                return Rating.Good;
            case 8:
                return Rating.Great;
            case 9:
                return Rating.Superb;
            case 10:
                return Rating.TotallyNinja;
        }
    }

}
