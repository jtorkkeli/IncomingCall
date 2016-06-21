package com.labaduu.incomingcall;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NotificationCompat;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

import static android.provider.Settings.Global.getString;

public class MyReceiver extends BroadcastReceiver {

    Context pcontext;

    public MyReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            pcontext = context;

            // TELEPHONY MANAGER class object to register one listner
            TelephonyManager tmgr = (TelephonyManager) context
                    .getSystemService(Context.TELEPHONY_SERVICE);

            //Create Listner
            MyPhoneStateListener PhoneListener = new MyPhoneStateListener();

            // Register listener for LISTEN_CALL_STATE
            tmgr.listen(PhoneListener, PhoneStateListener.LISTEN_CALL_STATE);

        } catch (Exception e) {
            Log.e("Phone Receive Error", " " + e);
        }
    }

    private class MyPhoneStateListener extends PhoneStateListener {
        private static final int NET_CONNECT_TIMEOUT_MILLIS = 15000; // 15 seconds
        private static final int NET_READ_TIMEOUT_MILLIS = 10000; // 10 seconds
        private static final String NOTIFICATION_SERVICE = "notification";

        public void onCallStateChanged(int state, String incomingNumber) {

            if (state == TelephonyManager.CALL_STATE_RINGING) {
                Log.d("MyPhoneListener", state + "   incoming no:" + incomingNumber);

                // Phone is ringing
/*                String msg = pcontext.getString(R.string.toast_message) + incomingNumber;
                int duration = Toast.LENGTH_LONG;
                Toast toast = Toast.makeText(pcontext, msg, duration);
                toast.show();
*/
                String urli = new String("http://office.labaduu.net/cgi-bin/call.pl?number=" + incomingNumber);
                AsyncTask<String, Void, String> resp = new DownloadTask().execute(urli);
            }
        }

        // Given a URL, establishes an HttpUrlConnection and retrieves
        // the web page content as a InputStream, which it returns as
        // a string.
        private String downloadUrl(String myurl) throws IOException {
            InputStream is = null;
            // Only display the first 500 characters of the retrieved
            // web page content.
            int len = 500;

            try {
                URL url = new URL(myurl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(10000 /* milliseconds */);
                conn.setConnectTimeout(15000 /* milliseconds */);
                conn.setRequestMethod("GET");
                conn.setDoInput(true);
                // Starts the query
                conn.connect();
                int response = conn.getResponseCode();
                Log.d("DEBUG_TAG", "The response is: " + response);
                is = conn.getInputStream();

                // Convert the InputStream into a string
                String contentAsString = readIt(is, len);
                return contentAsString;

                // Makes sure that the InputStream is closed after the app is
                // finished using it.
            } finally {
                if (is != null) {
                    is.close();
                }
            }
        }

        // Reads an InputStream and converts it to a String.
        public String readIt(InputStream stream, int len) throws IOException, UnsupportedEncodingException {
            Reader reader = null;
            reader = new InputStreamReader(stream, "UTF-8");
            char[] buffer = new char[len];
            reader.read(buffer);
            return new String(buffer);
        }

        /**
         * Implementation of AsyncTask, to fetch the data in the background away from
         * the UI thread.
         */
        private class DownloadTask extends AsyncTask<String, Void, String> {

            @Override
            protected String doInBackground(String... urls) {
                try {
                    return downloadUrl(urls[0]);
                } catch (IOException e) {
                    return pcontext.getString(R.string.connection_error);
                }
            }

            /**
             * Uses the logging framework to display the output of the fetch
             * operation in the log fragment.
             */
            @Override
            protected void onPostExecute(String result) {
                Log.i("TAG", result);
                int duration = Toast.LENGTH_LONG;
                Toast toast = Toast.makeText(pcontext, result, duration);
                toast.show();

                /*********** Create notification ***********/

                PendingIntent pi = PendingIntent.getActivity(pcontext, 0, new Intent(pcontext, ShowNotificationDetailActivity.class), 0);
                Resources r = pcontext.getResources();
                Notification notification = new NotificationCompat.Builder(pcontext)
                        .setTicker(r.getString(R.string.notification_title))
                        .setSmallIcon(android.R.drawable.ic_menu_report_image)
                        .setContentTitle(r.getString(R.string.notification_title))
                        .setContentText(result)
                        .setContentIntent(pi)
                        .setAutoCancel(true)
                        .setPriority(NotificationCompat.PRIORITY_MAX)
                        .build();

                NotificationManager notificationManager = (NotificationManager) pcontext.getSystemService(NOTIFICATION_SERVICE);
                notificationManager.notify(0, notification);
            }
        }
    }
}
