package com.example.serviceCarpet;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteCantOpenDatabaseException;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.CellInfoGsm;
import android.telephony.CellSignalStrengthGsm;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.Toast;

import com.example.kernel;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    private Logger log = LoggerFactory.getLogger("MainActivity");
    private static final String LTE_SIGNAL_STRENGTH = "getLteSignalStrength";

    public String userID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //send(null);

        kernel.telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        kernel.operatorName = kernel.telephonyManager.getNetworkOperatorName();
        kernel.operatorCode = kernel.telephonyManager.getNetworkOperator();
        kernel.phoneType = android.os.Build.MODEL;
        PhoneStateListener listener = new PhoneStateListener() {
            public void onSignalStrengthsChanged(SignalStrength ss) {
                super.onSignalStrengthsChanged(ss);
                kernel.netType = kernel.telephonyManager.getNetworkType();
                if (kernel.netType == 13) {
                    try {
                        kernel.dbm  = getLTEsignalStrength(ss);
                    } catch (InvocationTargetException e) {
                        e.printStackTrace();
                    } catch (IllegalAccessException e2) {
                        e2.printStackTrace();
                    }
                } else {
                    kernel.dbm = ss.getGsmSignalStrength();
                }
                if (Build.VERSION.SDK_INT >= 23)
                    kernel.level = ss.getLevel();
            }
        };
        kernel.telephonyManager.listen(listener, 256);
        MainAlarmReceiver.setupAlarm(this);
        SendAlarmReceiver.setupAlarm(this);
    }

    @Override
    protected void onDestroy() {
        kernel.sqLite.getInstance().close();
        super.onDestroy();
    }

    public int getLTEsignalStrength(SignalStrength signalStrength) throws InvocationTargetException, IllegalAccessException {
        Method[] methods;
        for (Method mthd : SignalStrength.class.getMethods()) {
            if (mthd.getName().equals(LTE_SIGNAL_STRENGTH)) {
                return ((Integer) mthd.invoke(signalStrength, new Object[0])).intValue();
            }
        }
        return -1;
    }
}
