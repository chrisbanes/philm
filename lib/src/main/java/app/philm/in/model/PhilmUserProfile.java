package app.philm.in.model;

import com.jakewharton.trakt.entities.UserProfile;

public class PhilmUserProfile {

    Long _id;
    String username;
    String fullName;
    String avatarUrl;

    public PhilmUserProfile() {}

    public PhilmUserProfile(UserProfile user) {
        setFromTraktEntity(user);
    }

    public void setFromTraktEntity(UserProfile user) {
        username = user.username;
        avatarUrl = user.avatar;
        fullName = user.fullName;
    }

    public String getUsername() {
        return username;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public String getFullName() {
        return fullName;
    }
}
