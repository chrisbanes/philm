package app.philm.in.model;

import com.google.common.base.Preconditions;

public class PhilmMovieCastCredit implements PhilmMovieCredit {

    final Person person;
    final String character;
    final int order;

    public PhilmMovieCastCredit(Person person, String character, int order) {
        this.person = Preconditions.checkNotNull(person, "person cannot be null");
        this.character = Preconditions.checkNotNull(character, "character cannot be null");
        this.order = order;
    }

    public Person getPerson() {
        return person;
    }

    public String getCharacter() {
        return character;
    }

    public Integer getOrder() {
        return order;
    }

}
