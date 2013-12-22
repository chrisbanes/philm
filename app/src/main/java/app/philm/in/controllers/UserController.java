package app.philm.in.controllers;

import com.google.common.base.Preconditions;

import com.squareup.otto.Subscribe;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;

import java.util.concurrent.ExecutorService;

import app.philm.in.Constants;
import app.philm.in.Display;
import app.philm.in.PhilmActivity;
import app.philm.in.state.UserState;
import app.philm.in.trakt.Trakt;
import app.philm.in.util.AccountManagerHelper;
import app.philm.in.util.BackgroundRunnable;
import app.philm.in.util.Sha1;

public class UserController extends BaseUiController<UserController.UserUi,
        UserController.UserUiCallbacks> {

    private static final String LOG_TAG = UserController.class.getSimpleName();

    public interface UserUi extends BaseUiController.Ui<UserUiCallbacks> {
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
            // Try and find account in account list, if removed remove our reference
            boolean found = false;
            for (int i = 0, z = accounts.length ; i < z ; i++) {
                if (accounts[i] == account) {
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
        } else {
            mUserState.setCredentials(null, null);

            // TODO: Also nuke rest of state
        }

        mTraktClient.setAuthentication(mUserState.getUsername(), mUserState.getHashedPassword());

        if (Constants.DEBUG) {
            Log.d(LOG_TAG, "onAccountChanged: " + mUserState.getUsername());
        }
    }

    @Override
    public boolean handleIntent(Intent intent) {
        final Display display = getDisplay();
        if (display != null && PhilmActivity.ACTION_LOGIN.equals(intent.getAction())) {
            display.showLogin();
            return true;
        }
        return super.handleIntent(intent);
    }

    @Override
    protected void onSuspended() {
        mUserState.unregisterForEvents(this);
        super.onSuspended();
    }

    void doLogin(String username, String password) {
        mExecutor.execute(new CheckUserCredentialsRunnable(username, Sha1.encode(password)));
    }

    @Override
    protected UserUiCallbacks createUiCallbacks() {
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

    private class CheckUserCredentialsRunnable extends BackgroundRunnable<Intent> {
        private final String mUsername, mPassword;

        CheckUserCredentialsRunnable(String username, String password) {
            mUsername = Preconditions.checkNotNull(username, "username cannot be null");
            mPassword = Preconditions.checkNotNull(password, "password cannot be null");
        }

        @Override
        public Intent runAsync() {
            mTraktClient.setAuthentication(mUsername, mPassword);
            if ("success".equals(mTraktClient.accountService().test().status)) {
                Intent res = new Intent();
                res.putExtra(AccountManager.KEY_ACCOUNT_NAME, mUsername);
                res.putExtra(AccountManager.KEY_ACCOUNT_TYPE, Constants.TRAKT_ACCOUNT_TYPE);
                res.putExtra(AccountManager.KEY_AUTHTOKEN, Constants.TRAKT_AUTHTOKEN_PASSWORD_TYPE);
                return res;
            }
            return null;
        }

        @Override
        public void postExecute(Intent result) {
            Account[] accounts = mAccountManagerHelper.getAccounts();
            Account existingAccount = null;
            for (int i = 0, count = accounts.length; i < count; i++) {
                if (mUsername.equals(accounts[i].name)) {
                    existingAccount = accounts[i];
                }
            }

            if (existingAccount == null) {
                final Account account = new Account(mUsername, Constants.TRAKT_ACCOUNT_TYPE);
                mAccountManagerHelper.addAccount(account, mPassword);
                mAccountManagerHelper.setAuthToken(account, mPassword,
                        Constants.TRAKT_AUTHTOKEN_PASSWORD_TYPE);
            } else {
                mAccountManagerHelper.setPassword(existingAccount, mPassword);
            }
        }
    }

}
