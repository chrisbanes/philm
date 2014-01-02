package app.philm.in.state;

import android.accounts.Account;

import app.philm.in.model.PhilmUserProfile;

interface BaseState {

    public PhilmUserProfile getUserProfile();

    public String getUsername();

    public Account getCurrentAccount();

    public void registerForEvents(Object receiver);

    public void unregisterForEvents(Object receiver);

    static class BaseArgumentEvent<T> {
        public final T item;

        public BaseArgumentEvent(T item) {
            this.item = item;
        }
    }

}
