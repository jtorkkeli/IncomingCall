package com.labaduu.incomingcall;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

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

        public void onCallStateChanged(int state, String incomingNumber) {

            if (state == TelephonyManager.CALL_STATE_RINGING) {
                Log.d("MyPhoneListener", state + "   incoming no:" + incomingNumber);

                // Phone is ringing
                String msg = "My Toast. Incoming Number : " + incomingNumber;
                int duration = Toast.LENGTH_LONG;
                Toast toast = Toast.makeText(pcontext, msg, duration);
                toast.show();
                // prepare intent which is triggered if the
                // notification is selected

                //Some Vars
                int NOTIFICATION_ID = 1; //this can be any int

                //Building the Notification
                NotificationCompat.Builder builder = new NotificationCompat.Builder(pcontext);
                // builder.setSmallIcon(R.drawable.ic_stat_notification);
                builder.setContentTitle("BasicNotifications Sample");
                builder.setContentText("Time to learn about notifications!");

                NotificationManager notificationManager = (NotificationManager) pcontext.getSystemService(
                        pcontext.NOTIFICATION_SERVICE);
                notificationManager.notify(NOTIFICATION_ID, builder.build());
            }
        }
    }
}
