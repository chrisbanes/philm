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

public class ListItem<T> {

    public static enum SectionTitle {
        RELATED, UPCOMING, SOON, RELEASED, SEEN, MOVIE_CAST, MOVIE_CREW
    }

    public static final int TYPE_ITEM = 0;
    public static final int TYPE_SECTION = 1;

    private final int type;
    private final T item;
    private final String sectionTitle;

    public ListItem(T item) {
        type = TYPE_ITEM;
        this.item = Preconditions.checkNotNull(item, "item cannot be null");
        sectionTitle = null;
    }

    public ListItem(String sectionTitle) {
        type = TYPE_SECTION;
        item = null;
        this.sectionTitle = Preconditions.checkNotNull(sectionTitle, "sectionTitle cannot be null");
    }

    public int getType() {
        return type;
    }

    public T getItem() {
        return item;
    }

    public String getSectionTitle() {
        return sectionTitle;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ListItem listItem = (ListItem) o;

        if (type != listItem.type) {
            return false;
        }
        if (item != null ? !item.equals(listItem.item) : listItem.item != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = type;
        result = 31 * result + (item != null ? item.hashCode() : 0);
        return result;
    }
}
