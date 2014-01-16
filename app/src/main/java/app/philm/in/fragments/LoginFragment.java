package app.philm.in.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import app.philm.in.PhilmApplication;
import app.philm.in.R;
import app.philm.in.controllers.UserController;

public class LoginFragment extends Fragment implements UserController.UserUi, View.OnClickListener,
        TextView.OnEditorActionListener {

    private static final String KEY_NEW_ACCOUNT = "new_account";

    private UserController.UserUiCallbacks mCallbacks;

    private EditText mUsername;
    private EditText mPassword;
    private Button mLoginButton;

    public static LoginFragment create() {
        LoginFragment fragment = new LoginFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login, container, false);

        mUsername = (EditText) view.findViewById(R.id.edit_login);
        mUsername.setOnEditorActionListener(this);
        mPassword = (EditText) view.findViewById(R.id.edit_password);
        mPassword.setOnEditorActionListener(this);

        mLoginButton = (Button) view.findViewById(R.id.btn_submit);
        mLoginButton.setOnClickListener(this);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        getController().attachUi(this);
    }

    @Override
    public void onPause() {
        getController().detachUi(this);
        super.onPause();
    }

    @Override
    public String getUiTitle() {
        return getString(R.string.account_login);
    }

    @Override
    public void showLoadingProgress(boolean visible) {
        getActivity().setProgressBarIndeterminateVisibility(visible);
    }

    @Override
    public void showError(UserController.Error error) {
        switch (error) {
            case BAD_AUTH:
                mPassword.setError(getString(R.string.login_authorization_error));
                break;
        }
    }

    @Override
    public boolean isModal() {
        return false;
    }

    @Override
    public void setCallbacks(UserController.UserUiCallbacks callbacks) {
        mCallbacks = callbacks;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_submit:
                submit();
                break;
        }
    }

    @Override
    public boolean onEditorAction(TextView textView, int actionId, KeyEvent event) {
        if (textView == mPassword) {
            switch (actionId) {
                case EditorInfo.IME_ACTION_DONE:
                    submit();
                    return true;
            }
        }
        return false;
    }

    private void submit() {
        mPassword.setError(null);

        if (mCallbacks != null) {
            final String username = mUsername.getText().toString().trim();
            if (!mCallbacks.isUsernameValid(username)) {
                mUsername.setError(getString(R.string.login_username_empty));
                return;
            }
            mUsername.setError(null);

            final String password = mPassword.getText().toString().trim();
            if (!mCallbacks.isPasswordValid(password)) {
                mPassword.setError(getString(R.string.login_password_empty));
                return;
            }
            mPassword.setError(null);

            mCallbacks.login(username, password);
        }
    }

    UserController getController() {
        return PhilmApplication.from(getActivity()).getMainController().getUserController();
    }

}
