package com.example.serviceCarpet;

import android.content.Context;
import android.database.sqlite.SQLiteCantOpenDatabaseException;
import android.database.sqlite.SQLiteDatabase;
import android.telephony.CellInfoGsm;
import android.telephony.CellSignalStrengthGsm;
import android.telephony.SignalStrength;

import com.example.kernel;

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
        try {
            sqBase = SQLiteDatabase.openOrCreateDatabase("/mnt/sdcard/cache.db3", null);
        } catch (SQLiteCantOpenDatabaseException e) {
            File tmp = new File("/mnt/sdcard/cache.db3");
            if (!tmp.exists()) {
                try {
                    tmp.createNewFile();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }
        sqBase.execSQL("CREATE TABLE IF NOT EXISTS logRecord (\n" +
                "id integer PRIMARY KEY,\n" +
                "userID varchar(50) NOT NULL,\n" +
                "longitude varchar(100) NOT NULL,\n" +
                "latitude varchar(100) NOT NULL,\n" +
                "level varchar(100) NOT NULL,\n" +
                "power varchar(100) NOT NULL,\n" +
                "typeNet varchar(10) NOT NULL,\n" +
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

    public void saveCache() {
        String operatorName = kernel.operatorName;

        if ((kernel.level != -1) || (kernel.dbm != Integer.MIN_VALUE))
        sqBase.execSQL("insert into logRecord (userID, longitude, latitude, level, power, typeNet, operator, phoneType, date) values " +
                "('" + kernel.userID + "', '10', '15', '" + kernel.level + "', '" +
                kernel.dbm + "', '" + getNetworkType(kernel.netType) + "', '" +
                operatorName + "', '" + kernel.phoneType + "', '" + (System.currentTimeMillis() / 1000L) + "')");
    }
}
