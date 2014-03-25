package app.philm.in.model;

import com.google.common.base.Preconditions;

public class ListItem<T> {

    public static enum SectionTitle {
        RELATED, UPCOMING, SOON, RELEASED, SEEN, MOVIE_CAST, MOVIE_CREW
    }

    public static final int TYPE_ITEM = 0;
    public static final int TYPE_SECTION = 1;

    private final int type;

    private T item;
    private SectionTitle sectionTitle;

    public ListItem(T item) {
        type = TYPE_ITEM;
        this.item = Preconditions.checkNotNull(item, "item cannot be null");
    }

    public ListItem(SectionTitle sectionTitle) {
        type = TYPE_SECTION;
        this.sectionTitle = Preconditions.checkNotNull(sectionTitle, "sectionTitle cannot be null");
    }

    public int getType() {
        return type;
    }

    public T getItem() {
        return item;
    }

    public SectionTitle getSectionTitle() {
        return sectionTitle;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ListItem listItem = (ListItem) o;

        if (type != listItem.type) {
            return false;
        }
        if (item != null ? !item.equals(listItem.item) : listItem.item != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = type;
        result = 31 * result + (item != null ? item.hashCode() : 0);
        return result;
    }
}
