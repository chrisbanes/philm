package app.philm.in.util;

import java.util.Collection;

public class PhilmCollections {

    public static boolean isEmpty(Collection<?> collection) {
        return collection == null || collection.isEmpty();
    }

}
