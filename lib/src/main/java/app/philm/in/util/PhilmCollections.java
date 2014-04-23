package app.philm.in.util;

import java.util.Collection;

public class PhilmCollections {

    public static boolean isEmpty(Collection<?> collection) {
        return collection == null || collection.isEmpty();
    }

    public static int size(Collection<?> collection) {
        return collection != null ? collection.size() : 0;
    }

}
