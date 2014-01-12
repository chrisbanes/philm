package app.philm.in.view;

import com.squareup.picasso.Picasso;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ImageView;

import app.philm.in.Constants;

public class PhilmFlagImageView extends ImageView {

    private String mUrlToLoadOnLayout;

    public PhilmFlagImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void loadUrl(String url) {
        if (canLoadImage()) {
            loadUrlImmediate(url);
        } else {
            mUrlToLoadOnLayout = url;
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        if (changed && mUrlToLoadOnLayout != null && canLoadImage()) {
            loadUrlImmediate(mUrlToLoadOnLayout);
            mUrlToLoadOnLayout = null;
        }
    }

    private boolean canLoadImage() {
        return getWidth() != 0 && getHeight() != 0;
    }

    private void loadUrlImmediate(String url) {
        final String mangledUrl = mangleUrl(url, getWidth(), getHeight());
        Picasso.with(getContext()).load(mangledUrl).into(this);

        if (Constants.DEBUG) {
            Log.d("PhilmImageView", "Loading " + mangledUrl);
        }
    }

    private static String mangleUrl(String url, int width, int height) {
        StringBuffer sb = new StringBuffer("http://api.imgble.com/");
        sb.append(url);
        sb.append('/').append(width);
        sb.append('/').append(height);
        sb.append("/gif");
        return sb.toString();
    }

}
