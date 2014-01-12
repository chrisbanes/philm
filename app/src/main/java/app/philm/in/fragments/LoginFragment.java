package app.philm.in.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import app.philm.in.PhilmApplication;
import app.philm.in.R;
import app.philm.in.controllers.UserController;

public class LoginFragment extends Fragment implements UserController.UserUi, View.OnClickListener {

    private static final String KEY_NEW_ACCOUNT = "new_account";

    private UserController.UserUiCallbacks mCallbacks;

    private EditText mUsername;

    private EditText mPassword;

    private Button mLoginButton;

    private TextView mErrorTextView;

    private ProgressBar mProgressBar;

    public static LoginFragment create() {
        LoginFragment fragment = new LoginFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login, container, false);

        mUsername = (EditText) view.findViewById(R.id.edit_login);
        mPassword = (EditText) view.findViewById(R.id.edit_password);

        mLoginButton = (Button) view.findViewById(R.id.btn_submit);
        mLoginButton.setOnClickListener(this);

        mErrorTextView = (TextView) view.findViewById(R.id.textview_error_message);
        mProgressBar = (ProgressBar) view.findViewById(R.id.progressbar);

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
        mProgressBar.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    @Override
    public void showError(UserController.Error error) {
        switch (error) {
            case BAD_AUTH:
                mErrorTextView.setText(R.string.login_authorization_error);
                mErrorTextView.setVisibility(View.VISIBLE);
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
                mErrorTextView.setVisibility(View.GONE);
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
    }

    UserController getController() {
        return PhilmApplication.from(getActivity()).getMainController().getUserController();
    }

}
