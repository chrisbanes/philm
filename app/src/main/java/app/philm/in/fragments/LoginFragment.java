package app.philm.in.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import app.philm.in.PhilmApplication;
import app.philm.in.R;
import app.philm.in.controllers.UserController;

public class LoginFragment extends Fragment implements UserController.UserUi, View.OnClickListener {

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
        mPassword = (EditText) view.findViewById(R.id.edit_password);

        mLoginButton = (Button) view.findViewById(R.id.btn_login);
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
    public void setCallbacks(UserController.UserUiCallbacks callbacks) {
        mCallbacks = callbacks;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_login:
                if (mCallbacks != null) {
                    String username = mUsername.getText().toString().trim();
                    String password = mPassword.getText().toString().trim();
                    if (mCallbacks.isUsernameValid(username) &&
                            mCallbacks.isPasswordValid(password)) {
                        mCallbacks.login(username, password);
                    }
                }
        }
    }

    UserController getController() {
        return PhilmApplication.from(getActivity()).getMainController().getUserController();
    }

}
