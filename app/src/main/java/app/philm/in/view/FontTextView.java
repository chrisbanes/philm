package app.philm.in.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.TextView;

import app.philm.in.Container;
import app.philm.in.R;
import app.philm.in.util.TypefaceManager;

public class FontTextView extends TextView {

    public static final int FONT_ROBOTO_LIGHT = 1;
    public static final int FONT_ROBOTO_CONDENSED = 2;
    public static final int FONT_ROBOTO_CONDENSED_LIGHT = 3;
    public static final int FONT_ROBOTO_CONDENSED_BOLD = 4;
    public static final int FONT_ROBOTO_SLAB = 5;

    public FontTextView(Context context) {
        this(context, null);
    }

    public FontTextView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FontTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.FontTextView);
        setFont(a.getInt(R.styleable.FontTextView_font, 0));
        a.recycle();
    }


    public void setFont(final int customFont) {
        Typeface typeface = getFont(getContext(), customFont);
        if (typeface != null) {
            setPaintFlags(getPaintFlags() | Paint.SUBPIXEL_TEXT_FLAG);
            setTypeface(typeface);
        }
    }

    public static Typeface getFont(Context context, final int customFont) {
        TypefaceManager typefaceManager = Container.getInstance(context).getTypefaceManager();
        Typeface typeface = null;

        switch (customFont) {
            case FONT_ROBOTO_LIGHT:
                typeface = typefaceManager.getRobotoLight();
                break;
            case FONT_ROBOTO_CONDENSED:
                typeface = typefaceManager.getRobotoCondensed();
                break;
            case FONT_ROBOTO_CONDENSED_LIGHT:
                typeface = typefaceManager.getRobotoCondensedLight();
                break;
            case FONT_ROBOTO_CONDENSED_BOLD:
                typeface = typefaceManager.getRobotoCondensedBold();
                break;
            case FONT_ROBOTO_SLAB:
                typeface = typefaceManager.getRobotoSlab();
                break;
        }

        return typeface;
    }
}
