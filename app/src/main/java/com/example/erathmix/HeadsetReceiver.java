package com.example.erathmix;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.Toast;

public class HeadsetReceiver extends BroadcastReceiver {

    public HeadsetReceiver() {
        // Required default constructor
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        SharedPreferences prefs = context.getSharedPreferences("ErathmixPrefs", Context.MODE_PRIVATE);
        boolean autoStart = prefs.getBoolean("auto_start", true);
        boolean autoPause = prefs.getBoolean("auto_pause", true);

        String action = intent.getAction();

        if (Intent.ACTION_HEADSET_PLUG.equals(action)) {
            int state = intent.getIntExtra("state", -1);
            if (state == 1 && autoStart) {
                startServiceAction(context, "RESUME");
                Toast.makeText(context, "🎧 Wired Headset Connected", Toast.LENGTH_SHORT).show();
            } else if (state == 0 && autoPause) {
                startServiceAction(context, "PAUSE");
                Toast.makeText(context, "🔌 Wired Headset Disconnected", Toast.LENGTH_SHORT).show();
            }
        }

        if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(action)) {
            if (autoStart) {
                startServiceAction(context, "RESUME");
                Toast.makeText(context, "🔊 Bluetooth Connected", Toast.LENGTH_SHORT).show();
            }
        } else if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)) {
            if (autoPause) {
                startServiceAction(context, "PAUSE");
                Toast.makeText(context, "🔇 Bluetooth Disconnected", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void startServiceAction(Context context, String action) {
        Intent serviceIntent = new Intent(context, AudioService.class); // Replace with your actual service class
        serviceIntent.setAction(action);
        context.startService(serviceIntent);
    }
}
