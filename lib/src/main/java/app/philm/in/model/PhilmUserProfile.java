package app.philm.in.model;

import com.jakewharton.trakt.services.AccountService;

public class PhilmUserProfile {

    Long _id;
    String username;
    String fullName;
    String avatarUrl;

    boolean twitterConnected;
    boolean facebookConnected;
    boolean pathConnected;
    boolean tumblrConnected;

    long lastFetched;

    public PhilmUserProfile() {}

    public PhilmUserProfile(AccountService.Settings settings) {
        setFromTraktEntity(settings);
    }

    public void setFromTraktEntity(AccountService.Settings settings) {
        final AccountService.Settings.Profile profile = settings.profile;
        username = profile.username;
        avatarUrl = profile.avatar;
        fullName = profile.full_name;

        final AccountService.Settings.Connections connections = settings.connections;
        twitterConnected = connections.twitter != null && connections.twitter.connected;
        facebookConnected = connections.facebook != null && connections.facebook.connected;
        pathConnected = connections.path != null && connections.path.connected;
        tumblrConnected = connections.tumblr != null && connections.tumblr.connected;

        lastFetched = System.currentTimeMillis();
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

    public boolean isTwitterConnected() {
        return twitterConnected;
    }

    public boolean isFacebookConnected() {
        return facebookConnected;
    }

    public boolean isPathConnected() {
        return pathConnected;
    }

    public boolean isTumblrConnected() {
        return tumblrConnected;
    }

    public long getLastFetched() {
        return lastFetched;
    }
}
