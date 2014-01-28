package app.philm.in.state;

import com.google.common.base.Objects;

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

    static class BaseArgumentEvent<T> {
        public final T item;

        public BaseArgumentEvent(T item) {
            this.item = item;
        }
    }

    abstract static class PaginatedResult<T> {
        public T items;
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

    public static class ShowCastLoadingProgressEvent extends ShowLoadingProgressEvent {
        public ShowCastLoadingProgressEvent(int callingId, boolean show) {
            super(callingId, show);
        }
    }

    public static class ShowTrailersLoadingProgressEvent extends ShowLoadingProgressEvent {
        public ShowTrailersLoadingProgressEvent(int callingId, boolean show) {
            super(callingId, show);
        }
    }

    public static class ShowErrorEvent {
        public final int callingId;
        public final NetworkError error;

        public ShowErrorEvent(int callingId, NetworkError error) {
            this.callingId = callingId;
            this.error = error;
        }
    }

}
