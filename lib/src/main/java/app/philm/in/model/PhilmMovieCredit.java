package app.philm.in.model;

import com.google.common.base.Preconditions;

public class PhilmMovieCredit implements Comparable<PhilmMovieCredit> {

    private static int ORDER_DIRECTOR = 0;
    private static int ORDER_WRITER = 1;
    private static int ORDER_PRODUCER = 2;
    private static int ORDER_PRODUCTION = 3;
    private static int ORDER_EDITING = 4;
    private static int ORDER_CAMERA = 5;
    private static int ORDER_ART = 6;
    private static int ORDER_SOUND = 7;

    PhilmPerson person;
    String job;
    String department;
    int order;

    public PhilmMovieCredit(PhilmPerson person, String character, int order) {
        this.person = Preconditions.checkNotNull(person, "person cannot be null");
        this.job = Preconditions.checkNotNull(character, "character cannot be null");
        this.order = order;
    }

    public PhilmMovieCredit(PhilmPerson person, String job, String department) {
        this.person = Preconditions.checkNotNull(person, "person cannot be null");
        this.job = Preconditions.checkNotNull(job, "job cannot be null");
        this.department = Preconditions.checkNotNull(department, "department cannot be null");
        this.order = calculateCrewOrder(this);
    }

    public PhilmPerson getPerson() {
        return person;
    }

    public String getJob() {
        return job;
    }

    public String getDepartment() {
        return department;
    }

    public int getOrder() {
        return order;
    }

    @Override
    public int compareTo(PhilmMovieCredit o) {
        final int orderThis = getOrder();
        final int orderOther = o.getOrder();

        if (orderThis != orderOther) {
            return orderThis - orderOther;
        } else {
            return this.person.name.compareTo(o.person.name);
        }
    }

    private static int calculateCrewOrder(PhilmMovieCredit crew) {
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
}
