package app.philm.in.controllers;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;

import com.jakewharton.trakt.entities.UserProfile;
import com.squareup.otto.Subscribe;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import java.util.concurrent.ExecutorService;

import app.philm.in.AccountActivity;
import app.philm.in.Constants;
import app.philm.in.Display;
import app.philm.in.model.PhilmUserProfile;
import app.philm.in.network.TraktNetworkCallRunnable;
import app.philm.in.state.UserState;
import app.philm.in.trakt.Trakt;
import app.philm.in.util.AccountManagerHelper;
import app.philm.in.util.Sha1;
import retrofit.RetrofitError;

public class UserController extends BaseUiController<UserController.UserUi,
        UserController.UserUiCallbacks> {

    private static final String LOG_TAG = UserController.class.getSimpleName();

    public static enum Error {
        BAD_AUTH
    }

    interface ControllerCallbacks {
        void onAddAccountCompleted(Bundle extras);
    }

    public interface UserUi extends BaseUiController.Ui<UserUiCallbacks> {
        void showError(Error error);
    }

    public interface UserUiCallbacks {

        boolean isUsernameValid(String username);

        boolean isPasswordValid(String password);

        void login(String username, String password);
    }

    private final UserState mUserState;
    private final ExecutorService mExecutor;
    private final Trakt mTraktClient;
    private final AccountManagerHelper mAccountManagerHelper;

    private ControllerCallbacks mControllerCallbacks;

    public UserController(
            UserState userState,
            Trakt traktClient,
            ExecutorService executor,
            AccountManagerHelper accountManagerHelper) {
        super();
        mUserState = Preconditions.checkNotNull(userState, "userState cannot be null");
        mTraktClient = Preconditions.checkNotNull(traktClient, "trackClient cannot be null");
        mExecutor = Preconditions.checkNotNull(executor, "executor cannot be null");
        mAccountManagerHelper = Preconditions.checkNotNull(accountManagerHelper,
                "accountManagerHelper cannot be null");
    }

    @Override
    protected void onInited() {
        super.onInited();

        mUserState.registerForEvents(this);

        Account account = mUserState.getCurrentAccount();
        Account[] accounts = mAccountManagerHelper.getAccounts();

        if (account == null) {
            if (accounts.length > 0) {
                mUserState.setCurrentAccount(accounts[0]);
            }
        } else {
            // Try and find account in account list, if removed, remove our reference
            boolean found = false;
            for (int i = 0, z = accounts.length ; i < z ; i++) {
                if (Objects.equal(accounts[i].name, account.name)) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                mUserState.setCurrentAccount(null);
            }
        }
    }

    @Subscribe
    public void onAccountChanged(UserState.AccountChangedEvent event) {
        Account currentAccount = mUserState.getCurrentAccount();
        if (currentAccount != null) {
            mUserState.setCredentials(currentAccount.name,
                    mAccountManagerHelper.getPassword(currentAccount));
            fetchUserProfile(mUserState.getUsername());
        } else {
            mUserState.setCredentials(null, null);
            mUserState.setUserProfile(null);
            // TODO: Also nuke rest of state
        }

        // Update TraktClient
        mTraktClient.setAuthentication(mUserState.getUsername(), mUserState.getHashedPassword());

        if (Constants.DEBUG) {
            Log.d(LOG_TAG, "onAccountChanged: " + mUserState.getUsername());
        }
    }

    @Override
    public boolean handleIntent(Intent intent) {
        final Display display = getDisplay();
        if (display != null && AccountActivity.ACTION_LOGIN.equals(intent.getAction())) {
            display.showLogin();
            return true;
        }
        return super.handleIntent(intent);
    }

    void setControllerCallbacks(ControllerCallbacks controllerCallbacks) {
        mControllerCallbacks = controllerCallbacks;
    }

    @Override
    protected void onSuspended() {
        mUserState.unregisterForEvents(this);
        super.onSuspended();
    }

    private void doLogin(String username, String password) {
        mExecutor.execute(new CheckUserCredentialsRunnable(username, Sha1.encode(password)));
    }

    private void fetchUserProfile(String username) {
        mExecutor.execute(new FetchUserProfileRunnable(username));
    }

    @Override
    protected UserUiCallbacks createUiCallbacks(final UserUi ui) {
        return new UserUiCallbacks() {

            @Override
            public boolean isUsernameValid(String username) {
                return !TextUtils.isEmpty(username);
            }

            @Override
            public boolean isPasswordValid(String password) {
                return !TextUtils.isEmpty(password);
            }

            @Override
            public void login(String username, String password) {
                doLogin(username, password);
            }
        };
    }

    private class FetchUserProfileRunnable extends TraktNetworkCallRunnable<UserProfile> {
        private final String mUsername;

        FetchUserProfileRunnable(String username) {
            super(mTraktClient);
            mUsername = Preconditions.checkNotNull(username, "username cannot be null");
        }

        @Override
        public UserProfile doTraktCall(Trakt trakt) throws RetrofitError {
            return trakt.userService().profile(mUsername);
        }

        @Override
        public void onSuccess(UserProfile result) {
            mUserState.setUserProfile(new PhilmUserProfile(result));
        }

        @Override
        public void onError(RetrofitError re) {
            // TODO Ignore
        }
    }

    private class CheckUserCredentialsRunnable extends TraktNetworkCallRunnable<String> {
        private final String mUsername, mPassword;

        CheckUserCredentialsRunnable(String username, String password) {
            super(mTraktClient);
            mUsername = Preconditions.checkNotNull(username, "username cannot be null");
            mPassword = Preconditions.checkNotNull(password, "password cannot be null");
        }

        @Override
        public String doTraktCall(Trakt trakt) {
            trakt.setAuthentication(mUsername, mPassword);
            return trakt.accountService().test().status;
        }

        @Override
        public void onSuccess(String result) {
            if (!"success".equals(result)) {
                for (UserUi ui : getUis()) {
                    ui.showError(Error.BAD_AUTH);
                }
                return;
            }

            Account[] accounts = mAccountManagerHelper.getAccounts();
            Account existingAccount = null;
            for (int i = 0, count = accounts.length; i < count; i++) {
                if (mUsername.equals(accounts[i].name)) {
                    existingAccount = accounts[i];
                }
            }

            Bundle callbackResult = new Bundle();

            if (existingAccount == null) {
                final Account account = new Account(mUsername, Constants.TRAKT_ACCOUNT_TYPE);
                mAccountManagerHelper.addAccount(account, mPassword);
                mAccountManagerHelper.setAuthToken(account, mPassword,
                        Constants.TRAKT_AUTHTOKEN_PASSWORD_TYPE);
            } else {
                mAccountManagerHelper.setPassword(existingAccount, mPassword);
            }

            callbackResult.putString(AccountManager.KEY_ACCOUNT_NAME, mUsername);
            callbackResult.putString(AccountManager.KEY_ACCOUNT_TYPE, Constants.TRAKT_ACCOUNT_TYPE);
            callbackResult.putString(AccountManager.KEY_AUTHTOKEN, mPassword);

            if (mControllerCallbacks != null) {
                // TODO: Shouldn't always finish
                mControllerCallbacks.onAddAccountCompleted(callbackResult);
            }
        }

        @Override
        public void onError(RetrofitError re) {
            for (UserUi ui : getUis()) {
                ui.showError(Error.BAD_AUTH);
            }
        }
    }

}
