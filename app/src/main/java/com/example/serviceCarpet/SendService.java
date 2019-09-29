package com.example.serviceCarpet;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.kernel;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

public class SendService extends Service {
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
                    send(null);
                    Thread.sleep(2000);
                }
            } catch (InterruptedException iex) {
            }

            workThread = null;
        }
    };

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
        retJSON jsonObject = buidJsonObject();

        if (jsonObject.json != null) {
            // 3. add JSON content to POST request body
            setPostRequestContent(conn, jsonObject.json);

            // 4. make POST request to the given URL
            conn.connect();
            Log.i("ID", String.valueOf(jsonObject.id));
            kernel.sqLite.delById(jsonObject.id);

            result = conn.getResponseMessage()+"";
        }
        // 5. return response message
        return result;

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
        // perform HTTP POST request
        if(checkNetworkConnection())
            new HTTPAsyncTask().execute(new String[] { "http://192.168.43.136:5600/api/post-track" });
        else
            Toast.makeText(this, "Not Connected!", Toast.LENGTH_SHORT).show();

    }

    private retJSON buidJsonObject() throws JSONException {
        retJSON retValue = null;

        retValue = kernel.sqLite.getObj();

        return retValue;
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
}
