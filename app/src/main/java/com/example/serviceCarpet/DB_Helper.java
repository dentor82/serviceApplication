package com.example.serviceCarpet;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteCantOpenDatabaseException;
import android.database.sqlite.SQLiteDatabase;
import android.telephony.CellInfoGsm;
import android.telephony.CellSignalStrengthGsm;
import android.telephony.SignalStrength;

import com.example.kernel;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class DB_Helper {
    private static final String LTE_SIGNAL_STRENGTH = "getLteSignalStrength";

    private SQLiteDatabase sqBase;
    private Context cur;

    public DB_Helper(Context context) {
        cur = context;
        //String b = "data/cache.db3";
        String b = "/mnt/sdcard/cache.db3";
        try {
            sqBase = SQLiteDatabase.openOrCreateDatabase(b, null);
        } catch (SQLiteCantOpenDatabaseException e) {
            File tmp = new File(b);
            if (!tmp.exists()) {
                try {
                    tmp.createNewFile();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }
        sqBase.execSQL("CREATE TABLE IF NOT EXISTS logRecord (\n" +
                "id integer PRIMARY KEY AUTOINCREMENT,\n" +
                "userID varchar(50) NOT NULL,\n" +
                "longitude varchar(100) NOT NULL,\n" +
                "latitude varchar(100) NOT NULL,\n" +
                "level varchar(100) NOT NULL,\n" +
                "power varchar(100) NOT NULL,\n" +
                "typeNet varchar(10) NOT NULL,\n" +
                "operatorCode varchar(10) NOT NULL,\n" +
                "operator varchar(10) NOT NULL,\n" +
                "phoneType varchar(50) NOT NULL,\n" +
                "date varchar(100) NOT NULL)");
    }

    public SQLiteDatabase getInstance() {
        return sqBase;
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

    public static String getNetworkType(int netType) {
        switch (netType) {
            case 0:
                return "??";
            case 1:
                return "GPRS";
            case 2:
                return "EDGE";
            case 3:
                return "UMTS";
            case 4:
                return "CDMA";
            case 5:
                return "EVDO_0";
            case 6:
                return "EVDO_A";
            case 7:
                return "1xRTT";
            case 8:
                return "HSDPA";
            case 9:
                return "HSUPA";
            case 10:
                return "HSPA";
            case 11:
                return "IDEN";
            case 12:
                return "EVDO_B";
            case 13:
                return "LTE";
            case 14:
                return "ENRPD";
            case 15:
                return "HSPAP";
            default:
                return "???";
        }
    }

    public synchronized retJSON getObj() throws JSONException {
        retJSON retValue = new retJSON();
        kernel.sqLite.getInstance().beginTransaction();
        Cursor c = kernel.sqLite.getInstance().query("logRecord", null, "", null, "", "", "id", "1");
        if (c.getCount() > 0) {
            c.moveToNext();
            retValue.json = new JSONObject();
            for (int i = 0; i < c.getColumnCount(); i++) {
                String name = c.getColumnName(i);
                System.out.println(name);
                if (name.equals("id")) {
                    retValue.id = c.getLong(i);
                } else {
                    retValue.json.accumulate(name, c.getString(i));
                }
            }
        }
        kernel.sqLite.getInstance().setTransactionSuccessful();
        kernel.sqLite.getInstance().endTransaction();
        return retValue;
    }

    public synchronized void delById(long inId) {
        kernel.sqLite.getInstance().beginTransaction();
        try {
            kernel.sqLite.getInstance().execSQL("delete from logRecord where id = " + inId);
            kernel.sqLite.getInstance().setTransactionSuccessful();
        } catch (Exception e) {
        }
        kernel.sqLite.getInstance().endTransaction();
    }

    public synchronized void saveCache() {
        if ((kernel.latitude != Double.MAX_VALUE) && (kernel.longitude != Double.MAX_VALUE)) {
            if ((kernel.level != -1) || (kernel.dbm != Integer.MIN_VALUE))
                sqBase.execSQL("insert into logRecord (userID, longitude, latitude, level, power, typeNet, operatorCode, operator, phoneType, date) values " +
                        "('" + kernel.userID + "', '" + kernel.longitude + "', '" + kernel.latitude + "', '" + kernel.level + "', '" +
                        kernel.dbm + "', '" + getNetworkType(kernel.netType) + "', '" + kernel.operatorName + "', '" +
                        kernel.operatorCode + "', '" + kernel.phoneType + "', '" + (System.currentTimeMillis() / 1000L) + "')");
        }
    }
}
