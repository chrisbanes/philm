package app.philm.in.lib.state;

import app.philm.in.lib.model.PhilmAccount;
import app.philm.in.lib.model.PhilmUserProfile;

public interface UserState extends BaseState {

    public void setUserProfile(PhilmUserProfile profile);

    public void setCurrentAccount(PhilmAccount account);

    public void setUsername(String username);

    public static class AccountChangedEvent {
    }

    public static class UserProfileChangedEvent {
    }

}
