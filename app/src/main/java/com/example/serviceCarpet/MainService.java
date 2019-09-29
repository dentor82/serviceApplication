package com.example.serviceCarpet;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.widget.Toast;

import com.example.kernel;

public class MainService extends Service {
    Thread workThread = null;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (workThread == null) {
            workThread = new Thread(run);
            workThread.start();
        }
        return Service.START_STICKY;
    }

    final Runnable run = new Runnable() {
        @Override
        public void run() {
            try {
                while (true) {
                    // Check if GPS enabled
                    if(kernel.gps.canGetLocation()) {
                        kernel.latitude = kernel.gps.getLatitude();
                        kernel.longitude = kernel.gps.getLongitude();
                    }
                    kernel.sqLite.saveCache();
                    //Toast.makeText(kernel.main, kernel.operatorName, Toast.LENGTH_SHORT).show();
                    Thread.sleep(20000);
                }
            } catch (InterruptedException iex) {
            }

            workThread = null;
        }
    };
}
