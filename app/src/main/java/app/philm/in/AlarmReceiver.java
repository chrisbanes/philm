package app.philm.in;

import com.google.common.base.Preconditions;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import javax.inject.Inject;

import app.philm.in.lib.util.AndroidPhilmAlarmManager;

public class AlarmReceiver extends BroadcastReceiver {

    @Inject AndroidPhilmAlarmManager mPhilmAlarmManager;

    @Override
    public void onReceive(Context context, Intent intent) {
        PhilmApplication.from(context).inject(this);
        Preconditions.checkNotNull(mPhilmAlarmManager, "mPhilmAlarmManager cannot be null");

        mPhilmAlarmManager.onAlarmTriggered(intent);
    }
}
