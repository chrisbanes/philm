package app.philm.in.model;

import com.google.common.base.Preconditions;

public class PhilmCastCredit implements PhilmCredit {

    final Person person;
    final String character;
    final int order;

    public PhilmCastCredit(Person person, String character, int order) {
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
