package com.example.erathmix;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class MusicService extends Service {

    @Override
    public IBinder onBind(Intent intent) {
        return null; // Not a bound service
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Your playback logic here
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Cleanup if needed
    }
}
