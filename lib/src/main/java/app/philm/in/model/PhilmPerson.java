package app.philm.in.model;

import com.uwetrottmann.tmdb.entities.Credits;
import com.uwetrottmann.tmdb.entities.Person;

import java.util.List;

public class PhilmPerson implements PhilmModel {

    Integer tmdbId;
    String name;
    String pictureUrl;

    String placeOfBirth;
    long dateOfBirth;
    String biography;

    int pictureType;

    transient List<PhilmPersonCredit> castCredits;
    transient List<PhilmPersonCredit> crewCredits;
    transient boolean fetchedCredits;

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

    public void setFromTmdb(Person person) {
        tmdbId = person.id;
        name = person.name;
        pictureUrl = person.profile_path;
        biography = person.biography;
        dateOfBirth = person.birthday != null ? person.birthday.getTime() : 0;
        placeOfBirth = person.place_of_birth;
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

    public List<PhilmPersonCredit> getCastCredits() {
        return castCredits;
    }

    public void setCastCredits(List<PhilmPersonCredit> castCredits) {
        this.castCredits = castCredits;
    }

    public List<PhilmPersonCredit> getCrewCredits() {
        return crewCredits;
    }

    public void setCrewCredits(List<PhilmPersonCredit> crewCredits) {
        this.crewCredits = crewCredits;
    }

    public boolean hasFetchedCredits() {
        return fetchedCredits;
    }

    public void setFetchedCredits(boolean fetchedCredits) {
        this.fetchedCredits = fetchedCredits;
    }

    public String getBiography() {
        return biography;
    }

    public long getDateOfBirth() {
        return dateOfBirth;
    }

    public String getPlaceOfBirth() {
        return placeOfBirth;
    }
}
