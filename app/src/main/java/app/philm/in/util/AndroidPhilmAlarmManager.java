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

package app.philm.in.util;

import com.google.common.base.Preconditions;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;

import javax.inject.Inject;
import javax.inject.Singleton;

import app.philm.in.qualifiers.ApplicationContext;

@Singleton
public class AndroidPhilmAlarmManager implements PhilmAlarmManager {

    private static final int REQUEST_CODE_INVALID = -1;
    private static final int REQUEST_CODE_CHECKIN_RATE_PROMPT = 0;

    private static final String KEY_PARAM_REQUEST = "request_type";
    private static final String KEY_PARAM_MOVIE_ID = "movie_id";

    private final Context mContext;
    private final AlarmManager mAlarmManager;

    @Inject
    public AndroidPhilmAlarmManager(
            @ApplicationContext Context context,
            AlarmManager alarmManager) {
        mContext = Preconditions.checkNotNull(context, "context cannot be null");
        mAlarmManager = Preconditions.checkNotNull(alarmManager, "alarmManager cannot be null");
    }

    @Override
    public void scheduleCheckinRatePrompt(String movieId, long delay) {
        mAlarmManager.set(
                AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime() + delay,
                buildPendingIntent(REQUEST_CODE_CHECKIN_RATE_PROMPT, movieId));
    }

    public void onAlarmTriggered(Intent intent) {
        switch (intent.getIntExtra(KEY_PARAM_REQUEST, REQUEST_CODE_INVALID)) {
            case REQUEST_CODE_CHECKIN_RATE_PROMPT:
                // TODO
                break;
        }
    }

    private Intent buildIntent(int requestType, String movieId) {
        Intent intent = new Intent();
        intent.putExtra(KEY_PARAM_REQUEST, requestType);
        intent.putExtra(KEY_PARAM_MOVIE_ID, movieId);
        return intent;
    }

    private PendingIntent buildPendingIntent(int requestType, String movieId) {
        return PendingIntent.getBroadcast(
                mContext,
                requestType,
                buildIntent(requestType, movieId),
                0);
    }
}
