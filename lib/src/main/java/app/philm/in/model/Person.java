package app.philm.in.model;

import com.uwetrottmann.tmdb.entities.Credits;

import java.util.List;

public class Person implements PhilmModel {

    Integer tmdbId;
    String name;
    String pictureUrl;

    int pictureType;

    transient List<PhilmCastCredit> castCredits;
    transient List<PhilmCrewCredit> crewCredits;

    public void setFromTmdb(Credits.CrewMember tmdbCrewMember) {
        tmdbId = tmdbCrewMember.id;
        name = tmdbCrewMember.name;
        pictureUrl = tmdbCrewMember.profile_path;
        pictureType = TYPE_TMDB;
    }

    public void setFromTmdb(Credits.CastMember tmdbCastMember) {
        tmdbId = tmdbCastMember.id;
        name = tmdbCastMember.name;
        pictureUrl = tmdbCastMember.profile_path;
        pictureType = TYPE_TMDB;
    }

    public Integer getTmdbId() {
        return tmdbId;
    }

    public String getName() {
        return name;
    }

    public String getPictureUrl() {
        return pictureUrl;
    }

    public int getPictureType() {
        return pictureType;
    }

    public List<PhilmCastCredit> getCastCredits() {
        return castCredits;
    }

    public void setCastCredits(List<PhilmCastCredit> castCredits) {
        this.castCredits = castCredits;
    }

    public List<PhilmCrewCredit> getCrewCredits() {
        return crewCredits;
    }

    public void setCrewCredits(List<PhilmCrewCredit> crewCredits) {
        this.crewCredits = crewCredits;
    }
}
