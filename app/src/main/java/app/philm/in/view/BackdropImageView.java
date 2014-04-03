package app.philm.in.view;

import android.content.Context;
import android.graphics.Canvas;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;

public class BackdropImageView extends PhilmImageView {

    private int mOffset;

    public BackdropImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void offsetBackdrop(int offset) {
        if (offset != mOffset) {
            mOffset = offset;
            ViewCompat.postInvalidateOnAnimation(this);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mOffset != 0) {
            canvas.save();
            canvas.translate(0f, mOffset);
            canvas.clipRect(0f, 0f, canvas.getWidth(), canvas.getHeight() + mOffset);
            super.onDraw(canvas);
            canvas.restore();
        } else {
            super.onDraw(canvas);
        }
    }
}
