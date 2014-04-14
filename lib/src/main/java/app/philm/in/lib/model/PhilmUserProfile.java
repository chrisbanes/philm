package app.philm.in.lib.model;

import com.jakewharton.trakt.entities.Settings;

public class PhilmUserProfile {

    Long _id;
    String username;
    String fullName;
    String avatarUrl;

    boolean twitterConnected;
    boolean facebookConnected;
    boolean pathConnected;
    boolean tumblrConnected;

    String defaultShareMessage;

    long lastFetched;

    public PhilmUserProfile() {}

    public PhilmUserProfile(Settings settings) {
        setFromTraktEntity(settings);
    }

    public void setFromTraktEntity(Settings settings) {
        final Settings.Profile profile = settings.profile;
        username = profile.username;
        avatarUrl = profile.avatar;
        fullName = profile.full_name;

        final Settings.Connections connections = settings.connections;
        twitterConnected = connections.twitter != null && connections.twitter.connected;
        facebookConnected = connections.facebook != null && connections.facebook.connected;
        pathConnected = connections.path != null && connections.path.connected;
        tumblrConnected = connections.tumblr != null && connections.tumblr.connected;

        final Settings.SharingText shareText = settings.sharing_text;
        if (shareText != null) {
            defaultShareMessage = shareText.watching;
        }

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

    public String getDefaultShareMessage() {
        return defaultShareMessage;
    }
}
