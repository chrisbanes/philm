package app.philm.in.model;

public class ListItem<T> {

    public static final int TYPE_ITEM = 0;
    public static final int TYPE_SECTION = 1;

    private final int type;
    private final T item;
    private final int titleResId;

    public ListItem(T item) {
        type = TYPE_ITEM;
        titleResId = 0;
        this.item = item;
    }

    public ListItem(int sectionTitle) {
        type = TYPE_SECTION;
        titleResId = sectionTitle;
        item = null;
    }

    public int getType() {
        return type;
    }

    public T getItem() {
        return item;
    }

    public int getSectionTitle() {
        return titleResId;
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

        if (titleResId != listItem.titleResId) {
            return false;
        }
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
        result = 31 * result + titleResId;
        return result;
    }
}
