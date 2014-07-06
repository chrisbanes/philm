package app.philm.in.util;

import android.R;
import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import java.lang.reflect.Method;

public class ActionBarUpIndicatorHelper {

    private static final String TAG = "ActionBarUpIndicatorHelper";

    private static final int[] THEME_ATTRS = new int[] {
            R.attr.homeAsUpIndicator
    };

    public static void setActionBarUpIndicator(Activity activity, Drawable drawable) {
        if (Build.VERSION.SDK_INT >= 18) {
            setActionBarUpIndicatorJbMr2(activity, drawable);
        } else {
            setActionBarUpIndicatorHc(activity, drawable);
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    static void setActionBarUpIndicatorJbMr2(Activity activity, Drawable drawable) {
        final ActionBar actionBar = activity.getActionBar();
        actionBar.setHomeAsUpIndicator(drawable);
    }

    static void setActionBarUpIndicatorHc(Activity activity, Drawable drawable) {
            SetIndicatorInfo   info = new SetIndicatorInfo(activity);

        final SetIndicatorInfo sii = (SetIndicatorInfo) info;
        if (sii.setHomeAsUpIndicator != null) {
            try {
                final ActionBar actionBar = activity.getActionBar();
                sii.setHomeAsUpIndicator.invoke(actionBar, drawable);
            } catch (Exception e) {
                Log.w(TAG, "Couldn't set home-as-up indicator via JB-MR2 API", e);
            }
        } else if (sii.upIndicatorView != null) {
            sii.upIndicatorView.setImageDrawable(drawable);
        } else {
            Log.w(TAG, "Couldn't set home-as-up indicator");
        }
    }

    public static Drawable getThemeUpIndicator(Activity activity) {
        final TypedArray a = activity.obtainStyledAttributes(THEME_ATTRS);
        final Drawable result = a.getDrawable(0);
        a.recycle();
        return result;
    }

    private static class SetIndicatorInfo {
        public Method setHomeAsUpIndicator;
        public ImageView upIndicatorView;

        SetIndicatorInfo(Activity activity) {
            try {
                setHomeAsUpIndicator = ActionBar.class.getDeclaredMethod("setHomeAsUpIndicator",
                        Drawable.class);
                // If we got the method we won't need the stuff below.
                return;
            } catch (NoSuchMethodException e) {
                // Oh well. We'll use the other mechanism below instead.
            }

            final View home = activity.findViewById(android.R.id.home);
            if (home == null) {
                // Action bar doesn't have a known configuration, an OEM messed with things.
                return;
            }

            final ViewGroup parent = (ViewGroup) home.getParent();
            final int childCount = parent.getChildCount();
            if (childCount != 2) {
                // No idea which one will be the right one, an OEM messed with things.
                return;
            }

            final View first = parent.getChildAt(0);
            final View second = parent.getChildAt(1);
            final View up = first.getId() == android.R.id.home ? second : first;

            if (up instanceof ImageView) {
                // Jackpot! (Probably...)
                upIndicatorView = (ImageView) up;
            }
        }
    }
}