package app.philm.in.model;


import com.uwetrottmann.tmdb.entities.Credits;

public class PhilmCrew extends BasePhilmCast {

    String job;
    String department;

    public void setFromTmdb(Credits.CrewMember tmdbCrewMember) {
        tmdbId = tmdbCrewMember.id;
        name = tmdbCrewMember.name;
        job = tmdbCrewMember.job;
        department = tmdbCrewMember.department;
        pictureUrl = tmdbCrewMember.profile_path;
        pictureType = TYPE_TMDB;
    }

    public String getJob() {
        return job;
    }

    public String getDepartment() {
        return department;
    }
}
