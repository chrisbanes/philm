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

}
