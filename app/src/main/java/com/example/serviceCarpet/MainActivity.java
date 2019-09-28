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
    }

    @Override
    protected void onDestroy() {
        kernel.sqLite.getInstance().close();
        super.onDestroy();
    }

    // check network connection
    public boolean checkNetworkConnection() {
        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        boolean isConnected = false;
        if (networkInfo != null && (isConnected = networkInfo.isConnected())) ;/*{
            // show "Connected" & type of network "WIFI or MOBILE"
            tvIsConnected.setText("Connected "+networkInfo.getTypeName());
            // change background color to red
            tvIsConnected.setBackgroundColor(0xFF7CCC26);


        } else {
            // show "Not Connected"
            tvIsConnected.setText("Not Connected");
            // change background color to green
            tvIsConnected.setBackgroundColor(0xFFFF0000);
        }*/

        return isConnected;
    }

    private String httpPost(String myUrl) throws IOException, JSONException {
        String result = "";

        URL url = new URL(myUrl);

        // 1. create HttpURLConnection
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json; charset=utf-8");

        // 2. build JSON object
        JSONObject jsonObject = buidJsonObject();

        // 3. add JSON content to POST request body
        setPostRequestContent(conn, jsonObject);

        // 4. make POST request to the given URL
        conn.connect();

        // 5. return response message
        return conn.getResponseMessage()+"";

    }

    private class HTTPAsyncTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            // params comes from the execute() call: params[0] is the url.
            try {
                try {
                    return httpPost(urls[0]);
                } catch (JSONException e) {
                    e.printStackTrace();
                    return "Error!";
                }
            } catch (IOException e) {
                return "Unable to retrieve web page. URL may be invalid.";
            }
        }
        // onPostExecute displays the results of the AsyncTask.
        /*@Override
        protected void onPostExecute(String result) {
            tvResult.setText(result);
        }*/
    }

    public void send(View view) {
        Toast.makeText(this, "Clicked", Toast.LENGTH_SHORT).show();
        // perform HTTP POST request
        if(checkNetworkConnection())
            new HTTPAsyncTask().execute(new String[] { "http://4322b9b5.ngrok.io/api/post-track" });
        else
            Toast.makeText(this, "Not Connected!", Toast.LENGTH_SHORT).show();

    }

    private JSONObject buidJsonObject() throws JSONException {

        JSONObject jsonObject = null;
        kernel.sqLite.getInstance().beginTransaction();
        Cursor c = kernel.sqLite.getInstance().query("logRecord", null, "", null, "", "", "id", "1");
        long id = 0;
        if (c.getCount() > 0) {
            c.moveToNext();
            jsonObject = new JSONObject();
            for (int i = 0; i < c.getColumnCount(); i++) {
                String name = c.getColumnName(i);
                System.out.println(name);
                if (name.equals("id")) {
                    id = c.getLong(i);
                } else {
                    jsonObject.accumulate(name, c.getString(i));
                }
            }
        }
        kernel.sqLite.getInstance().setTransactionSuccessful();
        kernel.sqLite.getInstance().beginTransaction();
        try {
            kernel.sqLite.getInstance().execSQL("delete from logRecord where id = " + id);
            kernel.sqLite.getInstance().setTransactionSuccessful();
        } catch (Exception e) {
            kernel.sqLite.getInstance().endTransaction();
        }

        return jsonObject;
    }

    private void setPostRequestContent(HttpURLConnection conn, JSONObject jsonObject) throws IOException {

        OutputStream os = conn.getOutputStream();
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
        writer.write(jsonObject.toString());
        //Log.i(MainActivity.class.toString(), jsonObject.toString());
        writer.flush();
        writer.close();
        os.close();
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
