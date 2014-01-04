package app.philm.in.state;

import app.philm.in.model.PhilmAccount;
import app.philm.in.controllers.MainController;
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

}
