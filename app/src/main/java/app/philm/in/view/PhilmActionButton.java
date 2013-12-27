package app.philm.in.view;

import android.content.Context;
import android.util.AttributeSet;

public class PhilmActionButton extends CheckableImageButton {

    public PhilmActionButton(Context context, AttributeSet attrs) {
        this(context, attrs, android.R.attr.borderlessButtonStyle);
    }

    public PhilmActionButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        CheatSheet.setup(this);
    }

}
