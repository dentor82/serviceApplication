package com.example;

import android.content.SharedPreferences;
import android.telephony.TelephonyManager;

import com.example.serviceCarpet.DB_Helper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class kernel {
    public static Logger log = LoggerFactory.getLogger("kernel");
    public static volatile DB_Helper sqLite;
    public static volatile TelephonyManager telephonyManager;
    public static volatile int netType = 0;
    public static volatile String operatorName;
    public static volatile String userID;
    public static volatile String phoneType;
    public static volatile int level = -1;
    public static volatile int dbm = Integer.MIN_VALUE;
}
