package app.philm.in.controllers;

import com.google.common.base.Preconditions;

abstract class BaseController {

    private boolean mInited;

    public final void init() {
        Preconditions.checkState(mInited == false, "Already inited");
        mInited = true;
        onInited();
    }

    public final void suspend() {
        Preconditions.checkState(mInited == true, "Not inited");
        onSuspended();
        mInited = false;
    }

    public final boolean isInited() {
        return mInited;
    }

    protected void onInited() {}

    protected void onSuspended() {}

}
