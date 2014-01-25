package app.philm.in.model;


import com.uwetrottmann.tmdb.entities.Credits;

public class PhilmCast {

    Integer tmdbId;
    Integer order;
    String name;
    String character;
    String pictureUrl;

    public void setFromCast(Credits.CastMember tmdbCastMember) {
        tmdbId = tmdbCastMember.id;
        order = tmdbCastMember.order;
        name = tmdbCastMember.name;
        character = tmdbCastMember.character;
        pictureUrl = tmdbCastMember.profile_path;
    }

    public Integer getTmdbId() {
        return tmdbId;
    }

    public Integer getOrder() {
        return order;
    }

    public String getName() {
        return name;
    }

    public String getCharacter() {
        return character;
    }

    public String getPictureUrl() {
        return pictureUrl;
    }
}
