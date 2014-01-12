package app.philm.in.state;

import com.google.common.base.Objects;

import app.philm.in.controllers.MainController;
import app.philm.in.model.PhilmAccount;
import app.philm.in.model.PhilmUserProfile;

interface BaseState {

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

}
