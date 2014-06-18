/*
 * Copyright 2014 Chris Banes
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package app.philm.in.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

import app.philm.in.PhilmApplication;
import app.philm.in.R;
import app.philm.in.controllers.UserController;

public class CredentialsChangedFragment extends DialogFragment
        implements DialogInterface.OnClickListener, UserController.UserUi {

    private UserController.UserUiCallbacks mCallbacks;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.trakt_wrong_credentials_title);
        builder.setMessage(R.string.trakt_wrong_credentials_text);
        builder.setPositiveButton(R.string.account_login, this);
        builder.setNegativeButton(android.R.string.cancel, this);
        return builder.create();
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
    public void onClick(DialogInterface dialogInterface, final int button) {
        switch (button) {
            case DialogInterface.BUTTON_POSITIVE:
                if (mCallbacks != null) {
                    mCallbacks.requestReLogin();
                }
                break;
        }
    }

    private UserController getController() {
        return PhilmApplication.from(getActivity()).getMainController().getUserController();
    }

    @Override
    public void showLoadingProgress(boolean visible) {
        // NO-OP
    }

    @Override
    public void showError(UserController.Error error) {
        // NO-OP
    }

    @Override
    public void setCallbacks(UserController.UserUiCallbacks callbacks) {
        mCallbacks = callbacks;
    }

    @Override
    public boolean isModal() {
        return true;
    }
}
