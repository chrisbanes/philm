package app.philm.in.controllers;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;

import com.jakewharton.trakt.Trakt;
import com.jakewharton.trakt.services.AccountService;
import com.squareup.otto.Subscribe;

import java.util.List;

import javax.inject.Inject;

import app.philm.in.Constants;
import app.philm.in.Display;
import app.philm.in.accounts.PhilmAccountManager;
import app.philm.in.model.PhilmAccount;
import app.philm.in.model.PhilmUserProfile;
import app.philm.in.modules.qualifiers.GeneralPurpose;
import app.philm.in.network.NetworkCallRunnable;
import app.philm.in.state.AsyncDatabaseHelper;
import app.philm.in.state.UserState;
import app.philm.in.util.BackgroundExecutor;
import app.philm.in.util.Logger;
import app.philm.in.util.PhilmCollections;
import app.philm.in.util.Sha1;
import app.philm.in.util.TextUtils;
import app.philm.in.util.TimeUtils;
import retrofit.RetrofitError;

public class UserController extends BaseUiController<UserController.UserUi,
        UserController.UserUiCallbacks> {

    private static final String LOG_TAG = UserController.class.getSimpleName();

    public static enum Error {
        BAD_AUTH
    }

    interface ControllerCallbacks {
        void onAddAccountCompleted(String username, String authToken, String authTokenType);
    }

    public interface UserUi extends BaseUiController.Ui<UserUiCallbacks> {
        void showLoadingProgress(boolean visible);

        void showError(Error error);
    }

    public interface UserUiCallbacks {
        void onTitleChanged(String newTitle);

        boolean isUsernameValid(String username);

        boolean isPasswordValid(String password);

        void login(String username, String password);
    }

    private final UserState mUserState;
    private final BackgroundExecutor mExecutor;
    private final Trakt mTraktClient;
    private final PhilmAccountManager mPhilmAccountManager;
    private final AsyncDatabaseHelper mDbHelper;
    private final Logger mLogger;

    private ControllerCallbacks mControllerCallbacks;

    @Inject
    public UserController(
            UserState userState,
            Trakt traktClient,
            @GeneralPurpose BackgroundExecutor executor,
            PhilmAccountManager accountFetcher,
            AsyncDatabaseHelper dbHelper,
            Logger logger) {
        super();
        mUserState = Preconditions.checkNotNull(userState, "userState cannot be null");
        mTraktClient = Preconditions.checkNotNull(traktClient, "trackClient cannot be null");
        mExecutor = Preconditions.checkNotNull(executor, "executor cannot be null");
        mPhilmAccountManager = Preconditions.checkNotNull(accountFetcher,
                "accountFetcher cannot be null");
        mDbHelper = Preconditions.checkNotNull(dbHelper, "dbHelper cannot be null");
        mLogger = Preconditions.checkNotNull(logger, "logger cannot be null");
    }

    @Override
    protected void onInited() {
        super.onInited();

        mUserState.registerForEvents(this);

        PhilmAccount account = mUserState.getCurrentAccount();
        List<PhilmAccount> accounts = mPhilmAccountManager.getAccounts();

        if (account == null) {
            if (!PhilmCollections.isEmpty(accounts)) {
                mUserState.setCurrentAccount(accounts.get(0));
            }
        } else {
            // Try and find account in account list, if removed, remove our reference
            boolean found = false;
            for (int i = 0, z = accounts.size() ; i < z ; i++) {
                if (Objects.equal(accounts.get(i).getAccountName(), account.getAccountName())) {
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
        PhilmAccount currentAccount = mUserState.getCurrentAccount();

        if (currentAccount != null) {
            final String username = currentAccount.getAccountName();
            mUserState.setUsername(username);
            mTraktClient.setAuthentication(username, currentAccount.getPassword());
            mDbHelper.getUserProfile(username, new UserProfileDbLoadCallback());
        } else {
            mUserState.setUsername(null);
            mTraktClient.setAuthentication(null, null);

            final PhilmUserProfile currentUserProfile = mUserState.getUserProfile();
            if (currentUserProfile != null) {
                mUserState.setUserProfile(null);
                mDbHelper.delete(currentUserProfile);
            }
            // TODO: Also nuke rest of state
        }

        mLogger.d(LOG_TAG, "onAccountChanged: " + mUserState.getUsername());
    }

    @Override
    public boolean handleIntent(String intentAction) {
        final Display display = getDisplay();
        if (display != null && Display.ACTION_LOGIN.equals(intentAction)) {
            display.showLogin();
            return true;
        }
        return super.handleIntent(intentAction);
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
        for (UserUi ui : getUis()) {
            ui.showLoadingProgress(true);
        }
        mExecutor.execute(new CheckUserCredentialsRunnable(username, Sha1.encode(password)));
    }

    private void fetchUserProfile(String username) {
        mExecutor.execute(new FetchUserProfileRunnable(username));
    }

    @Override
    protected UserUiCallbacks createUiCallbacks(final UserUi ui) {
        return new UserUiCallbacks() {
            @Override
            public void onTitleChanged(String newTitle) {
                updateDisplayTitle(newTitle);
            }

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

    private class FetchUserProfileRunnable extends NetworkCallRunnable<AccountService.Settings> {
        private final String mUsername;

        FetchUserProfileRunnable(String username) {
            mUsername = Preconditions.checkNotNull(username, "username cannot be null");
        }

        @Override
        public AccountService.Settings doBackgroundCall() throws RetrofitError {
            return mTraktClient.accountService().settings();
        }

        @Override
        public void onSuccess(AccountService.Settings result) {
            PhilmUserProfile newProfile = new PhilmUserProfile(result);
            mUserState.setUserProfile(newProfile);
            mDbHelper.put(newProfile);
        }

        @Override
        public void onError(RetrofitError re) {
            // TODO Ignore
        }
    }

    private class CheckUserCredentialsRunnable extends NetworkCallRunnable<String> {
        private final String mUsername, mPassword;

        CheckUserCredentialsRunnable(String username, String password) {
            mUsername = Preconditions.checkNotNull(username, "username cannot be null");
            mPassword = Preconditions.checkNotNull(password, "password cannot be null");
        }

        @Override
        public String doBackgroundCall() {
            mTraktClient.setAuthentication(mUsername, mPassword);
            return mTraktClient.accountService().test().status;
        }

        @Override
        public void onSuccess(String result) {
            if (!"success".equals(result)) {
                for (UserUi ui : getUis()) {
                    ui.showError(Error.BAD_AUTH);
                }
                return;
            }

            List<PhilmAccount> accounts = mPhilmAccountManager.getAccounts();
            PhilmAccount account = null;
            for (int i = 0, count = accounts.size(); i < count; i++) {
                if (mUsername.equals(accounts.get(i).getAccountName())) {
                    account = accounts.get(i);
                    break;
                }
            }

            if (account == null) {
                account = new PhilmAccount(mUsername, mPassword);
                account.setAuthToken(mPassword, Constants.TRAKT_AUTHTOKEN_PASSWORD_TYPE);
                mPhilmAccountManager.addAccount(account);
            } else {
                mPhilmAccountManager.updatePassword(account);
            }

            mUserState.setCurrentAccount(account);

            if (mControllerCallbacks != null) {
                mControllerCallbacks.onAddAccountCompleted(mUsername, mPassword,
                        Constants.TRAKT_ACCOUNT_TYPE);
            }
        }

        @Override
        public void onError(RetrofitError re) {
            for (UserUi ui : getUis()) {
                ui.showError(Error.BAD_AUTH);
            }
        }

        @Override
        public void onFinished() {
            for (UserUi ui : getUis()) {
                ui.showLoadingProgress(false);
            }
        }
    }

    private class UserProfileDbLoadCallback implements AsyncDatabaseHelper.Callback<PhilmUserProfile> {
        @Override
        public void onFinished(PhilmUserProfile result) {
            mUserState.setUserProfile(result);
            if (result == null || TimeUtils.isPastThreshold(result.getLastFetched(),
                    Constants.STALE_USER_PROFILE_THRESHOLD)) {
                fetchUserProfile(mUserState.getUsername());
            }
        }
    }
}
