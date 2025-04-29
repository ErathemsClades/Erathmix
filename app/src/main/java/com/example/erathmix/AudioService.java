package com.example.erathmix;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class AudioService extends Service {

    private static AudioPlayer audioPlayer;

    public static void setAudioPlayer(AudioPlayer player) {
        audioPlayer = player;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (audioPlayer != null && intent != null && intent.getAction() != null) {
            switch (intent.getAction()) {
                case "RESUME":
                    audioPlayer.resume();
                    break;
                case "PAUSE":
                    audioPlayer.pause();
                    break;
            }
        }
        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null; // Not a bound service
    }
}
