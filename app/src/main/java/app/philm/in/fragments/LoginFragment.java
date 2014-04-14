package app.philm.in.fragments;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Patterns;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import app.philm.in.PhilmApplication;
import app.philm.in.R;
import app.philm.in.lib.controllers.UserController;

public class LoginFragment extends Fragment implements UserController.UserUi, View.OnClickListener,
        TextView.OnEditorActionListener, RadioGroup.OnCheckedChangeListener {

    private static final String KEY_NEW_ACCOUNT = "new_account";

    private UserController.UserUiCallbacks mCallbacks;

    private EditText mUsername;
    private EditText mPassword;
    private Button mLoginButton;

    private RadioGroup mTypeRadioGroup;
    private RadioButton mLoginRadioButton, mCreateRadioButton;
    private AutoCompleteTextView mEmailAutoComplete;

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

        mTypeRadioGroup = (RadioGroup) view.findViewById(R.id.rg_type);
        mTypeRadioGroup.setOnCheckedChangeListener(this);

        mEmailAutoComplete = (AutoCompleteTextView) view.findViewById(R.id.actv_email);
        mEmailAutoComplete.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                mEmailAutoComplete.showDropDown();
            }
        });

        mLoginRadioButton = (RadioButton) view.findViewById(R.id.rb_login);
        mCreateRadioButton = (RadioButton) view.findViewById(R.id.rb_create);

        mLoginRadioButton.setChecked(true);

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
            case BAD_CREATE:
                mUsername.setError(getString(R.string.create_user_authorization_error));
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

            switch (mTypeRadioGroup.getCheckedRadioButtonId()) {
                case R.id.rb_login:
                    mCallbacks.login(username, password);
                    break;
                case R.id.rb_create:
                    final String email = mEmailAutoComplete.getText().toString().trim();
                    if (!mCallbacks.isEmailValid(email)) {
                        mEmailAutoComplete.setError(getString(R.string.login_email_invalid));
                        return;
                    }
                    mEmailAutoComplete.setError(null);

                    mCallbacks.createUser(username, password, email);
                    break;
            }
        }
    }

    UserController getController() {
        return PhilmApplication.from(getActivity()).getMainController().getUserController();
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        switch (checkedId) {
            case R.id.rb_login:
                mLoginButton.setText(R.string.account_login);
                mEmailAutoComplete.setVisibility(View.GONE);
                break;
            case R.id.rb_create:
                mLoginButton.setText(R.string.account_register);
                mEmailAutoComplete.setVisibility(View.VISIBLE);

                if (mEmailAutoComplete.getAdapter() == null) {
                    final Set<String> emailSet = new HashSet<>();
                    for (Account account : AccountManager.get(getActivity()).getAccounts()) {
                        if (Patterns.EMAIL_ADDRESS.matcher(account.name).matches()) {
                            emailSet.add(account.name);
                        }
                    }
                    List<String> emails = new ArrayList<>(emailSet);
                    mEmailAutoComplete.setAdapter(new ArrayAdapter<>(getActivity(),
                            android.R.layout.simple_spinner_dropdown_item, emails));
                }
                break;
        }
    }
}
