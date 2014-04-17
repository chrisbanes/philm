package app.philm.in;

import android.view.Menu;

import app.philm.in.lib.controllers.MainController;

public class MainActivity extends BasePhilmActivity implements MainController.MainUi {

    private MainController.MainControllerUiCallbacks mUiCallbacks;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        getMainController().attachUi(this);
    }

    @Override
    protected void onPause() {
        getMainController().detachUi(this);
        super.onPause();
    }

    @Override
    public void showLoginPrompt() {
        // TODO: Show delayed prompt
        //if (mUiCallbacks != null) {
        //    mUiCallbacks.setShownLoginPrompt();
        //}
    }

    @Override
    public String getUiTitle() {
        return null;
    }

    @Override
    public void setCallbacks(MainController.MainControllerUiCallbacks callbacks) {
        mUiCallbacks = callbacks;
    }

    @Override
    public boolean isModal() {
        return false;
    }
}
