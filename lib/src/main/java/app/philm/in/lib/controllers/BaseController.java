package app.philm.in.lib.controllers;

import com.google.common.base.Preconditions;

import app.philm.in.lib.Display;

abstract class BaseController {

    private Display mDisplay;
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

    public boolean handleIntent(String intentAction) {
        return false;
    }

    public void setDisplay(Display display) {
        mDisplay = display;
    }

    public final Display getDisplay() {
        return mDisplay;
    }

    protected final void assertInited() {
        Preconditions.checkState(mInited, "Must be init'ed to perform action");
    }
}
