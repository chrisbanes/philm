package app.philm.in.controllers;

import com.google.common.base.Preconditions;

import android.text.TextUtils;

import app.philm.in.state.UserState;

public class UserController extends BaseUiController<UserController.UserUi,
        UserController.UserUiCallbacks> {

    public interface UserUi extends BaseUiController.Ui<UserUiCallbacks> {
    }

    public interface UserUiCallbacks {
        void login(String username, String password);
    }

    private final UserState mUserState;

    public UserController(UserState userState) {
        super();
        mUserState = Preconditions.checkNotNull(userState, "userState cannot be null");
    }

    @Override
    protected void onInited() {
        super.onInited();
        mUserState.registerForEvents(this);
    }

    private boolean validCredentials() {
        return !TextUtils.isEmpty(mUserState.getUsername())
                && !TextUtils.isEmpty(mUserState.getHashedPassword());
    }

    @Override
    protected void onSuspended() {
        super.onSuspended();
        mUserState.unregisterForEvents(this);
    }

    void doLogin(String username, String password) {
        // TODO: Hash password
    }

    @Override
    protected UserUiCallbacks createUiCallbacks() {
        return new UserUiCallbacks() {
            @Override
            public void login(String username, String password) {
                doLogin(username, password);
            }
        };
    }

}
