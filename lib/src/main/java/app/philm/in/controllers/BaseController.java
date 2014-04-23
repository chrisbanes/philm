package app.philm.in.controllers;

import com.google.common.base.Preconditions;

import android.content.Intent;

import app.philm.in.Display;

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

    public void onSync() {
    }

    public final boolean isInited() {
        return mInited;
    }

    protected void onInited() {}

    protected void onSuspended() {}

    public boolean handleIntent(Intent intent) {
        return false;
    }

    protected void setDisplay(Display display) {
        mDisplay = display;
    }

    protected final Display getDisplay() {
        return mDisplay;
    }

    protected final void assertInited() {
        Preconditions.checkState(mInited, "Must be init'ed to perform action");
    }
}
