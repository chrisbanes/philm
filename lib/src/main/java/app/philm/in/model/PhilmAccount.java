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

package app.philm.in.model;

public class PhilmAccount {

    private final String mAccountName;
    private final String mPassword;

    private String mAuthToken;
    private String mAuthTokenType;

    public PhilmAccount(String accountName, String password) {
        mAccountName = accountName;
        mPassword = password;
    }

    public String getAccountName() {
        return mAccountName;
    }

    public String getPassword() {
        return mPassword;
    }

    public void setAuthToken(String authToken, String type) {
        mAuthToken = authToken;
        mAuthTokenType = type;
    }

    public String getAuthToken() {
        return mAuthToken;
    }

    public String getAuthTokenType() {
        return mAuthTokenType;
    }
}
