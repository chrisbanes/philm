package app.philm.in.model;

import com.jakewharton.trakt.entities.UserProfile;

public class PhilmUserProfile {

    String username;
    String avatarUrl;

    public PhilmUserProfile() {}

    public PhilmUserProfile(UserProfile user) {
        setFromTraktEntity(user);
    }

    public void setFromTraktEntity(UserProfile user) {
        username = user.username;
        avatarUrl = user.avatar;
    }

    public String getUsername() {
        return username;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }
}
