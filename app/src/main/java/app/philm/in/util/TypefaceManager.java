package app.philm.in.util;

import com.google.common.base.Preconditions;

import android.content.res.AssetManager;
import android.graphics.Typeface;
import android.support.v4.util.LruCache;

public class TypefaceManager {

    private static final String ROBOTO_LIGHT_FILENAME = "Roboto-Light.ttf";
    private static final String ROBOTO_CONDENSED_FILENAME = "RobotoCondensed-Regular.ttf";
    private static final String ROBOTO_CONDENSED_BOLD_FILENAME = "RobotoCondensed-Bold.ttf";
    private static final String ROBOTO_CONDENSED_LIGHT_FILENAME = "RobotoCondensed-Light.ttf";
    private static final String ROBOTO_SLAB_FILENAME = "RobotoSlab-Regular.ttf";

    private final LruCache<String, Typeface> mCache;
    private final AssetManager mAssetManager;

    public TypefaceManager(AssetManager assetManager) {
        mAssetManager = Preconditions.checkNotNull(assetManager, "assetManager cannot be null");
        mCache = new LruCache<>(5);
    }

    public Typeface getRobotoLight() {
        return getTypeface(ROBOTO_LIGHT_FILENAME);
    }

    public Typeface getRobotoCondensed() {
        return getTypeface(ROBOTO_CONDENSED_FILENAME);
    }

    public Typeface getRobotoCondensedBold() {
        return getTypeface(ROBOTO_CONDENSED_BOLD_FILENAME);
    }

    public Typeface getRobotoCondensedLight() {
        return getTypeface(ROBOTO_CONDENSED_LIGHT_FILENAME);
    }

    public Typeface getRobotoSlab() {
        return getTypeface(ROBOTO_SLAB_FILENAME);
    }

    private Typeface getTypeface(final String filename) {
        Typeface typeface = mCache.get(filename);
        if (typeface == null) {
            typeface = Typeface.createFromAsset(mAssetManager, "fonts/" + filename);
            mCache.put(filename, typeface);
        }
        return typeface;
    }
}
