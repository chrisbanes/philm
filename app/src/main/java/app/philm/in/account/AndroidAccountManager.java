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

package app.philm.in.account;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;

import android.accounts.Account;
import android.accounts.AccountManager;

import java.util.ArrayList;
import java.util.List;

import app.philm.in.Constants;
import app.philm.in.accounts.PhilmAccountManager;
import app.philm.in.model.PhilmAccount;

public class AndroidAccountManager implements PhilmAccountManager {

    private final AccountManager mAccountManager;

    public AndroidAccountManager(AccountManager accountManager) {
        mAccountManager = Preconditions.checkNotNull(accountManager,
                "accountManager cannot be null");
    }

    @Override
    public List<PhilmAccount> getAccounts() {
        final Account[] accounts = mAccountManager.getAccountsByType(Constants.TRAKT_ACCOUNT_TYPE);
        ArrayList<PhilmAccount> philmAccounts = new ArrayList<>(accounts.length);

        for (int i = 0; i < accounts.length ; i++) {
            final Account account = accounts[i];

            String password = mAccountManager.getPassword(account);
            philmAccounts.add(new PhilmAccount(account.name, password));
        }

        return philmAccounts;
    }

    @Override
    public void addAccount(PhilmAccount philmAccount) {
        Account account = new Account(philmAccount.getAccountName(), Constants.TRAKT_ACCOUNT_TYPE);

        mAccountManager.addAccountExplicitly(account, philmAccount.getPassword(), null);

        mAccountManager.setAuthToken(account, philmAccount.getAuthToken(),
                philmAccount.getAuthTokenType());
    }

    @Override
    public void removeAccount(PhilmAccount philmAccount) {
        Account account = new Account(philmAccount.getAccountName(), Constants.TRAKT_ACCOUNT_TYPE);
        mAccountManager.removeAccount(account, null, null);
    }

    @Override
    public void updatePassword(PhilmAccount philmAccount) {
        final Account[] accounts = mAccountManager.getAccountsByType(Constants.TRAKT_ACCOUNT_TYPE);
        for (int i = 0; i < accounts.length ; i++) {
            final Account account = accounts[i];

            if (Objects.equal(account.name, philmAccount.getAccountName())) {
                mAccountManager.setPassword(account, philmAccount.getPassword());
                return;
            }
        }
    }

}
