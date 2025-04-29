package com.example.erathmix;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class AudioPlayer {

    private final Context context;
    private MediaPlayer mediaPlayer;
    private MediaPlayer overlayPlayer;
    private final List<File> musicUris = new ArrayList<>();
    private final List<Integer> adResIds = new ArrayList<>();
    private final List<Integer> djResIds = new ArrayList<>();
    private final Handler handler = new Handler();

    private int songsPlayed = 0;
    private int sinceLastAd = 0;
    private int sinceLastDj = 0;
    private int songsSinceLastOverlay = 0;
    private boolean isOverlayPlaying = false;
    private Integer lastInsertRes = null;

    private String currentMode = "radio"; // default mode

    private final TrackUpdateListener trackUpdateListener;

    private File currentMusicFolder = null; // 🔥 New: store the last selected folder

    public void loadMusic() {
        musicUris.clear();

        if (currentMusicFolder != null && currentMusicFolder.isDirectory()) {
            File[] files = currentMusicFolder.listFiles();
            if (files != null) {
                for (File file : files) {
                    String name = file.getName().toLowerCase();
                    if (name.endsWith(".mp3") || name.endsWith(".wav") || name.endsWith(".m4a")) {
                        musicUris.add(file);
                    }
                }
            }
        }

        if (!musicUris.isEmpty()) {
            Collections.shuffle(musicUris);
        }
    }

    public interface TrackUpdateListener {
        void onTrackChanged(String trackName, String mode);
    }

    public AudioPlayer(Context context, TrackUpdateListener listener) {
        this.context = context;
        this.trackUpdateListener = listener;
        loadInbuiltAudio();
    }

    public void setMusicFolder(File musicFolder) {
        currentMusicFolder = musicFolder;
        loadMusic(); // 🔥 Automatically load songs from folder
    }

    // 🔥 NEW: Reload music files from the last selected folder


    public void setMode(String mode) {
        if (mode.equalsIgnoreCase("music") || mode.equalsIgnoreCase("radio")) {
            currentMode = mode.toLowerCase();
        }
    }

    private void loadInbuiltAudio() {
        adResIds.clear();
        djResIds.clear();

        for (int i = 1; i <= 97; i++) {
            String name = String.format("advert_%02d", i);
            int resId = context.getResources().getIdentifier(name, "raw", context.getPackageName());
            if (resId != 0) adResIds.add(resId);
        }

        for (int i = 1; i <= 41; i++) {
            String name = String.format("dj_%02d", i);
            int resId = context.getResources().getIdentifier(name, "raw", context.getPackageName());
            if (resId != 0) djResIds.add(resId);
        }
    }

    public void playNext() {
        releaseMediaPlayer();

        File nextFile = getNextTrack();
        if (nextFile == null) return;

        Uri uri = Uri.fromFile(nextFile);
        mediaPlayer = MediaPlayer.create(context, uri);

        if (mediaPlayer == null) return;

        mediaPlayer.setOnCompletionListener(mp -> playNext());
        mediaPlayer.start();

        if (trackUpdateListener != null) {
            String name = nextFile.getName().replaceFirst("[.][^.]+$", "");
            trackUpdateListener.onTrackChanged(name, currentMode.equals("radio") ? "Music" : "Music Only");
        }

        songsPlayed++;
        sinceLastAd++;
        sinceLastDj++;
        songsSinceLastOverlay++;
    }

    private File getNextTrack() {
        boolean isRadio = currentMode.equals("radio");

        if (isRadio) {
            boolean playAd = (sinceLastAd >= getRandom(5, 7));
            boolean playDj = (sinceLastDj >= getRandom(3, 4));
            boolean allowInsert = songsPlayed >= 2 && lastInsertRes == null && !isOverlayPlaying;

            if (allowInsert) {
                if (playAd && !adResIds.isEmpty()) {
                    sinceLastAd = 0;
                    lastInsertRes = getRandomElement(adResIds);
                    playResource(lastInsertRes, "[Ad Insert]", "Ad Insert");
                    return null;
                } else if (playDj && !djResIds.isEmpty()) {
                    sinceLastDj = 0;
                    lastInsertRes = getRandomElement(djResIds);
                    playResource(lastInsertRes, "[DJ Insert]", "DJ Insert");
                    return null;
                }
            }
        }

        lastInsertRes = null;

        if (!musicUris.isEmpty()) {
            File song = musicUris.remove(0);
            musicUris.add(song); // loop playlist

            if (isRadio && songsSinceLastOverlay >= getRandom(20, 25) && !djResIds.isEmpty()) {
                songsSinceLastOverlay = 0;
                int overlayRes = getRandomElement(djResIds);
                playWithOverlay(song, overlayRes);
                return null;
            }

            return song;
        }

        return null;
    }

    private void playWithOverlay(File songFile, int djResId) {
        releaseMediaPlayer();

        mediaPlayer = MediaPlayer.create(context, Uri.fromFile(songFile));
        if (mediaPlayer == null) return;

        mediaPlayer.setVolume(0.4f, 0.4f);
        mediaPlayer.start();

        if (trackUpdateListener != null) {
            String name = songFile.getName().replaceFirst("[.][^.]+$", "");
            trackUpdateListener.onTrackChanged(name, "Overlay DJ");
        }

        isOverlayPlaying = true;

        overlayPlayer = MediaPlayer.create(context, djResId);
        handler.postDelayed(() -> {
            if (overlayPlayer != null) overlayPlayer.start();
        }, 5000); // Delay for realism

        mediaPlayer.setOnCompletionListener(mp -> {
            releaseOverlayPlayer();
            isOverlayPlaying = false;
            playNext();
        });
    }

    private void playResource(int resId, String label, String mode) {
        releaseMediaPlayer();

        mediaPlayer = MediaPlayer.create(context, resId);
        if (mediaPlayer == null) return;

        mediaPlayer.setOnCompletionListener(mp -> {
            lastInsertRes = null;
            playNext();
        });
        mediaPlayer.start();

        if (trackUpdateListener != null) {
            trackUpdateListener.onTrackChanged(label, mode);
        }
    }

    private void releaseMediaPlayer() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    private void releaseOverlayPlayer() {
        if (overlayPlayer != null) {
            overlayPlayer.release();
            overlayPlayer = null;
        }
    }

    private int getRandom(int min, int max) {
        return new Random().nextInt((max - min) + 1) + min;
    }

    private int getRandomElement(List<Integer> list) {
        return list.get(new Random().nextInt(list.size()));
    }

    public void pause() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
        }
    }

    public void resume() {
        if (mediaPlayer != null) {
            mediaPlayer.start();
        } else {
            playNext(); // Start fresh
        }
    }
}
