package app.philm.in.model;


import com.uwetrottmann.tmdb.entities.Credits;

public class PhilmCast extends BasePhilmCast {

    String character;

    public void setFromTmdb(Credits.CastMember tmdbCastMember) {
        tmdbId = tmdbCastMember.id;
        order = tmdbCastMember.order;
        name = tmdbCastMember.name;
        character = tmdbCastMember.character;
        pictureUrl = tmdbCastMember.profile_path;
        pictureType = TYPE_TMDB;
    }

    public String getCharacter() {
        return character;
    }

}
