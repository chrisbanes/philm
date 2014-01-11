package app.philm.in.util;

import com.google.common.base.Preconditions;

import android.widget.TextView;

public class ViewUtils {

    public static boolean isEmpty(TextView textView) {
        Preconditions.checkNotNull(textView, "textView cannot be null");
        return TextUtils.isEmpty(textView.getText());
    }

}
