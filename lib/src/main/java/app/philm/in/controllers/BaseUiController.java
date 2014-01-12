package app.philm.in.controllers;

import com.google.common.base.Preconditions;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import app.philm.in.Display;
import app.philm.in.util.TextUtils;

abstract class BaseUiController<U extends BaseUiController.Ui<UC>, UC>
        extends BaseController {

    public interface Ui<UC> {
        String getUiTitle();
        void setCallbacks(UC callbacks);
        boolean isModal();
    }

    public interface SubUi {}

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
            if (!ui.isModal() && !(ui instanceof SubUi)) {
                final String uiTitle = ui.getUiTitle();
                if (!TextUtils.isEmpty(uiTitle)) {
                    updateDisplayTitle(ui.getUiTitle());
                }
            }

            onUiAttached(ui);
            populateUis();
        }
    }

    protected void updateDisplayTitle(String title) {
        Display display = getDisplay();
        if (display != null) {
            display.setActionBarTitle(title);
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
