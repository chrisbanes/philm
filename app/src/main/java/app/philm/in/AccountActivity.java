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

package app.philm.in;

import android.accounts.AccountAuthenticatorResponse;
import android.accounts.AccountManager;
import android.content.Intent;
import android.os.Bundle;

public class AccountActivity extends BasePhilmActivity {

    private AccountAuthenticatorResponse mAccountAuthenticatorResponse = null;
    private Bundle mResultBundle = null;

    /**
     * Set the result that is to be sent as the result of the request that caused this
     * Activity to be launched. If result is null or this method is never called then
     * the request will be canceled.
     * @param result this is returned as the result of the AbstractAccountAuthenticator request
     */
    public final void setAccountAuthenticatorResult(Bundle result) {
        mResultBundle = result;
    }

    /**
     * Sends the result or a Constants.ERROR_CODE_CANCELED error if a result isn't present.
     */
    public void finish() {
        if (mAccountAuthenticatorResponse != null) {
            // send the result bundle back if set, otherwise send an error.
            if (mResultBundle != null) {
                mAccountAuthenticatorResponse.onResult(mResultBundle);
            } else {
                mAccountAuthenticatorResponse.onError(
                        AccountManager.ERROR_CODE_CANCELED, "canceled");
            }
            mAccountAuthenticatorResponse = null;
        }
        super.finish();
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

    /**
     * Retreives the AccountAuthenticatorResponse from either the intent of the icicle, if the
     * icicle is non-zero.
     * @param icicle the save instance data of this Activity, may be null
     */
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        mAccountAuthenticatorResponse = getIntent().getParcelableExtra(
                AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE);

        if (mAccountAuthenticatorResponse != null) {
            mAccountAuthenticatorResponse.onRequestContinued();
        }
    }

    @Override
    protected void handleIntent(Intent intent, Display display) {
        if (!display.hasMainFragment()) {
            display.showLogin();
        }
    }

    @Override
    protected int getContentViewLayoutId() {
        return R.layout.activity_account;
    }
}
