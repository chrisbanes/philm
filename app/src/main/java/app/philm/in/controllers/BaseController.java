package app.philm.in.controllers;

import com.google.common.base.Preconditions;

public abstract class BaseController {

    private boolean mInited;

    public BaseController() {
    }

    public final boolean init() {
        Preconditions.checkState(mInited == false, "Already inited");
        mInited = true;
        return onInited();
    }

    public final void suspend() {
        Preconditions.checkState(mInited == true, "Not inited");
        onSuspended();
        mInited = false;
    }

    public final boolean isInited() {
        return mInited;
    }

    protected boolean onInited() {
        return true;
    }

    protected void onSuspended() {}

}
