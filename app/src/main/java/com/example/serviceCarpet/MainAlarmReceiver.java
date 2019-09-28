package com.example.serviceCarpet;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.example.kernel;

public class MainAlarmReceiver extends BroadcastReceiver {
    static final long intervalMs = 6000; // Интервал в миллисекундах

    @Override
    public void onReceive(Context context, Intent intent) {
        context.startService(new Intent(context, MainService.class));
        setupAlarm(context);
    }

    public static final void setupAlarm(Context context) {
        kernel.sqLite = new DB_Helper(context);
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent i = new Intent(context, MainAlarmReceiver.class);
        PendingIntent pi = PendingIntent.getBroadcast(context, 0, i, 0);
        if (am != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + intervalMs, pi);
            } else {
                am.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + intervalMs, pi);
            }
        }
    }
}
