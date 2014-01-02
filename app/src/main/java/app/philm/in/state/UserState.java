package app.philm.in.state;

import android.accounts.Account;

import app.philm.in.model.PhilmUserProfile;

public interface UserState extends BaseState {

    public void setUserProfile(PhilmUserProfile profile);

    public void setCurrentAccount(Account account);

    public void setUsername(String username);

    public static class AccountChangedEvent {
    }

    public static class UserProfileChangedEvent {
    }

}
