package app.philm.in;

import com.google.common.base.Preconditions;

import android.content.Context;

import app.philm.in.util.StringFetcher;

public class AndroidStringFetcher implements StringFetcher {

    private final Context mContext;

    public AndroidStringFetcher(Context context) {
        mContext = Preconditions.checkNotNull(context, "context cannot be null");
    }

    @Override
    public String getString(int id) {
        return mContext.getString(id);
    }

    @Override
    public String getString(int id, Object... format) {
        return mContext.getString(id, format);
    }
}
