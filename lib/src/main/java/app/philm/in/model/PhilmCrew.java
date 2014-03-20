package app.philm.in.model;


import com.uwetrottmann.tmdb.entities.Credits;

import java.util.Comparator;

public class PhilmCrew extends BasePhilmCast {

    private static int ORDER_DIRECTOR = 0;
    private static int ORDER_WRITER = 1;
    private static int ORDER_PRODUCER = 2;
    private static int ORDER_PRODUCTION = 3;
    private static int ORDER_EDITING = 4;
    private static int ORDER_CAMERA = 5;
    private static int ORDER_ART = 6;
    private static int ORDER_SOUND = 7;

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

    public static final Comparator<PhilmCrew> COMPARATOR = new Comparator<PhilmCrew>() {
        @Override
        public int compare(PhilmCrew o1, PhilmCrew o2) {
            final int order1 = getOrder(o1);
            final int order2 = getOrder(o2);

            if (order1 != order2) {
                return order1 - order2;
            } else {
                return o1.name.compareTo(o2.name);
            }
        }

        private int getOrder(PhilmCrew crew) {
            if (crew.job.equals("Director")) {
                return ORDER_DIRECTOR;
            } else if (crew.department.equals("Writing")) {
                return ORDER_WRITER;
            } else if (crew.job.equals("Producer")) {
                return ORDER_PRODUCER;
            } else if (crew.department.equals("Production")) {
                return ORDER_PRODUCTION;
            } else if (crew.department.equals("Editing")) {
                return ORDER_EDITING;
            } else if (crew.department.equals("Camera")) {
                return ORDER_CAMERA;
            } else if (crew.department.equals("Art")) {
                return ORDER_ART;
            } else if (crew.department.equals("Sound")) {
                return ORDER_SOUND;
            }
            return 100;
        }
    };

}
