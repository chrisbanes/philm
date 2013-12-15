package app.philm.in.controllers;

import com.google.common.base.Preconditions;

import com.squareup.otto.Bus;

abstract class BaseUiController<U extends BaseUiController.Ui<UC>, UC>
        extends BaseController {

    public interface Ui<UC> {
        void setCallbacks(UC callbacks);
    }

    private final UC mUiCallbacks;
    private U mUi;

    public BaseUiController() {
        mUiCallbacks = createUiCallbacks();
    }

    public final void attachUi(U ui) {
        Preconditions.checkState(mUi == null, "UI is already attached");
        Preconditions.checkArgument(ui != null, "ui cannot be null");
        mUi = ui;
        mUi.setCallbacks(mUiCallbacks);

        if (isInited()) {
            populateUi();
        }
    }

    public final void detachUi(Ui ui) {
        Preconditions.checkArgument(ui != null, "ui cannot be null");
        Preconditions.checkState(mUi == ui, "ui is not attached");
        mUi.setCallbacks(null);
        mUi = null;
    }

    protected final U getUi() {
        return mUi;
    }

    protected void onInited() {
        if (mUi != null) {
            populateUi();
        }
    }

    protected void populateUi() {}

    protected abstract UC createUiCallbacks();

}
