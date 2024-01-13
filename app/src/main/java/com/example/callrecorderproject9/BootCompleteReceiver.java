package com.example.callrecorderproject9;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.TelephonyManager;
import android.util.Log;

public class BootCompleteReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if (action != null) {
            if (action.equals(Intent.ACTION_NEW_OUTGOING_CALL)) {
                // Handle NEW_OUTGOING_CALL
                String phoneNumber = intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER);
                Log.d("BootCompleteReceiver", "Outgoing call to: " + phoneNumber);

                // Add your logic for handling outgoing calls here
                // For example, you can start a service or perform some action
                Intent serviceIntent = new Intent(context, CallRecorder.class);
                serviceIntent.setAction(Intent.ACTION_NEW_OUTGOING_CALL);
                serviceIntent.putExtra("OUTGOING_CALL_NUMBER", phoneNumber);
                context.startService(serviceIntent);
            } else if (action.equals(Intent.ACTION_BOOT_COMPLETED) || action.equals(TelephonyManager.ACTION_PHONE_STATE_CHANGED)) {
                // Handle BOOT_COMPLETED or PHONE_STATE_CHANGED
                Intent serviceIntent = new Intent(context, CallRecorder.class);
                context.startService(serviceIntent);
            }
        }
    }
}
