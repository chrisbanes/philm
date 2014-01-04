package app.philm.in.controllers;

import com.google.common.base.Preconditions;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

abstract class BaseUiController<U extends BaseUiController.Ui<UC>, UC>
        extends BaseController {

    public interface Ui<UC> {
        void setCallbacks(UC callbacks);
    }

    private HashSet<U> mUis;

    public BaseUiController() {
        mUis = new HashSet<U>();
    }

    public final void attachUi(U ui) {
        Preconditions.checkArgument(ui != null, "ui cannot be null");
        Preconditions.checkState(!mUis.contains(ui), "UI is already attached");

        mUis.add(ui);
        ui.setCallbacks(createUiCallbacks(ui));

        if (isInited()) {
            onUiAttached(ui);
            populateUis();
        }
    }

    public final void detachUi(U ui) {
        Preconditions.checkArgument(ui != null, "ui cannot be null");
        Preconditions.checkState(mUis.contains(ui), "ui is not attached");
        onUiDetached(ui);
        ui.setCallbacks(null);
        mUis.remove(ui);
    }

    protected final Set<U> getUis() {
        return Collections.unmodifiableSet(mUis);
    }

    protected void onInited() {
        if (!mUis.isEmpty()) {
            for (U ui : mUis) {
                onUiAttached(ui);
            }
            populateUis();
        }
    }

    protected void onUiAttached(U ui) {}

    protected void onUiDetached(U ui) {}

    protected void populateUis() {
        for (U ui : mUis) {
            populateUi(ui);
        }
    }

    protected void populateUi(U ui) {

    }

    protected abstract UC createUiCallbacks(U ui);

}
