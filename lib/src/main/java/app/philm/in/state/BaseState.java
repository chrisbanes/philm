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

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;

import java.util.List;

import app.philm.in.controllers.MainController;
import app.philm.in.model.PhilmAccount;
import app.philm.in.model.PhilmUserProfile;
import app.philm.in.network.NetworkError;

public interface BaseState {

    public void setSelectedSideMenuItem(MainController.SideMenuItem item);

    public MainController.SideMenuItem getSelectedSideMenuItem();

    public PhilmUserProfile getUserProfile();

    public String getUsername();

    public PhilmAccount getCurrentAccount();

    public void registerForEvents(Object receiver);

    public void unregisterForEvents(Object receiver);

    public static class UiCausedEvent {
        public final int callingId;

        public UiCausedEvent(int callingId) {
            this.callingId = callingId;
        }
    }

    static class BaseArgumentEvent<T> extends UiCausedEvent {
        public final T item;

        public BaseArgumentEvent(int callingId, T item) {
            super(callingId);
            this.item = Preconditions.checkNotNull(item, "item cannot be null");
        }
    }

    public abstract static class PaginatedResult<T> {
        public List<T> items;
        public int page;
        public int totalPages;

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            PaginatedResult that = (PaginatedResult) o;
            return Objects.equal(items, that.items);
        }

        @Override
        public int hashCode() {
            return items != null ? items.hashCode() : 0;
        }
    }

    public static class ShowLoadingProgressEvent {
        public final int callingId;
        public final boolean show;
        public final boolean secondary;

        public ShowLoadingProgressEvent(int callingId, boolean show) {
            this(callingId, show, false);
        }

        public ShowLoadingProgressEvent(int callingId, boolean show, boolean secondary) {
            this.callingId = callingId;
            this.show = show;
            this.secondary = secondary;
        }
    }

    public static class ShowRelatedLoadingProgressEvent extends ShowLoadingProgressEvent {
        public ShowRelatedLoadingProgressEvent(int callingId, boolean show) {
            super(callingId, show);
        }
    }

    public static class ShowCreditLoadingProgressEvent extends ShowLoadingProgressEvent {
        public ShowCreditLoadingProgressEvent(int callingId, boolean show) {
            super(callingId, show);
        }
    }

    public static class ShowVideosLoadingProgressEvent extends ShowLoadingProgressEvent {
        public ShowVideosLoadingProgressEvent(int callingId, boolean show) {
            super(callingId, show);
        }
    }

    public static class OnErrorEvent {
        public final int callingId;
        public final NetworkError error;

        public OnErrorEvent(int callingId, NetworkError error) {
            this.callingId = callingId;
            this.error = error;
        }
    }

}
