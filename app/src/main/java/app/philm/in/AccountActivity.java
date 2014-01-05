package app.philm.in;

import android.accounts.AccountAuthenticatorActivity;
import android.accounts.AccountManager;
import android.content.Intent;
import android.os.Bundle;

import app.philm.in.controllers.MainController;

public class AccountActivity extends AccountAuthenticatorActivity
        implements MainController.HostCallbacks {

    private MainController mMainController;
    private Intent mLaunchIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);
        mMainController = PhilmApplication.from(this).getMainController();
        mLaunchIntent = getIntent();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        mLaunchIntent = intent;
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        mMainController.setDisplay(new AndroidDisplay(this));
        mMainController.setHostCallbacks(this);
        mMainController.init();

        if (mLaunchIntent != null) {
            mMainController.handleIntent(mLaunchIntent.getAction());
            mLaunchIntent = null;
        }
    }

    @Override
    protected void onPause() {
        mMainController.suspend();
        mMainController.setHostCallbacks(null);
        mMainController.setDisplay(null);
        super.onPause();
    }

    @Override
    public void setAccountAuthenticatorResult(String username, String authToken,
            String accountType) {
        Bundle callbackResult = new Bundle();
        callbackResult.putString(AccountManager.KEY_ACCOUNT_NAME, username);
        callbackResult.putString(AccountManager.KEY_ACCOUNT_TYPE, accountType);
        callbackResult.putString(AccountManager.KEY_AUTHTOKEN, authToken);
        setAccountAuthenticatorResult(callbackResult);
    }

}
