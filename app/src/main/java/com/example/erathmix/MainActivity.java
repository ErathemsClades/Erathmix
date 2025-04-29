package com.example.erathmix;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "ErathmixPrefs";
    private static final String KEY_FOLDER_URI = "music_folder_uri";
    private static final String KEY_PLAYBACK_MODE = "playback_mode";
    private static final String KEY_AUTO_START = "autoStart";
    private static final String KEY_AUTO_PAUSE = "autoPause";

    private enum PlaybackMode { RADIO, MUSIC }

    private PlaybackMode currentMode = PlaybackMode.RADIO;

    private AudioPlayer audioPlayer;
    private Uri musicFolder;

    private TextView txtNowPlaying, txtMode;
    private Button btnPlayPause, btnToggleMode, btnSelectFolder, btnNext;
    private ImageButton settingsButton, playlistMenuButton;

    private boolean isPlaying = false;
    private SharedPreferences prefs;
    private ActivityResultLauncher<Intent> folderPickerLauncher;

    private final BroadcastReceiver headsetReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            boolean autoStart = prefs.getBoolean(KEY_AUTO_START, true);
            boolean autoPause = prefs.getBoolean(KEY_AUTO_PAUSE, true);

            switch (intent.getAction()) {
                case Intent.ACTION_HEADSET_PLUG:
                    int state = intent.getIntExtra("state", -1);
                    if (state == 1 && autoStart && !isPlaying) {
                        resumePlayback();
                    } else if (state == 0 && autoPause && isPlaying) {
                        pausePlayback();
                    }
                    break;

                case BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED:
                    int connState = intent.getIntExtra(BluetoothAdapter.EXTRA_CONNECTION_STATE, -1);
                    if (connState == BluetoothAdapter.STATE_CONNECTED && autoStart && !isPlaying) {
                        resumePlayback();
                    } else if (connState == BluetoothAdapter.STATE_DISCONNECTED && autoPause && isPlaying) {
                        pausePlayback();
                    }
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        requestStoragePermission();

        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String savedMode = prefs.getString(KEY_PLAYBACK_MODE, "radio");
        currentMode = savedMode.equals("music") ? PlaybackMode.MUSIC : PlaybackMode.RADIO;

        bindViews();
        setupFolderPicker();
        setupAudioPlayer();
        setupListeners();

        loadSavedMusicFolder();
        registerReceiver();
    }

    private void requestStoragePermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
        }
    }

    private void bindViews() {
        txtNowPlaying = findViewById(R.id.txtNowPlaying);
        txtMode = findViewById(R.id.txtMode);
        btnPlayPause = findViewById(R.id.btnPlayPause);
        btnToggleMode = findViewById(R.id.btnToggleMode);
        settingsButton = findViewById(R.id.btnSettings);
        playlistMenuButton = findViewById(R.id.btnPlaylistMenu);
        btnSelectFolder = findViewById(R.id.btnSelectFolder);
        btnNext = findViewById(R.id.btnNext);
    }

    private void setupFolderPicker() {
        folderPickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri treeUri = result.getData().getData();
                        if (treeUri != null) {
                            getContentResolver().takePersistableUriPermission(
                                    treeUri,
                                    Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                            );
                            prefs.edit().putString(KEY_FOLDER_URI, treeUri.toString()).apply();
                            setMusicFolder(treeUri);
                        }
                    }
                });
    }

    private void setupAudioPlayer() {
        audioPlayer = new AudioPlayer(this, (track, mode) -> runOnUiThread(() -> {
            txtNowPlaying.setText("Now Playing: " + track);
            txtMode.setText("Mode: " + mode);
            btnPlayPause.setText("Pause");
            isPlaying = true;
        }));
        audioPlayer.setMode(currentMode == PlaybackMode.RADIO ? "radio" : "music");

        txtMode.setText("Mode: " + (currentMode == PlaybackMode.RADIO ? "Radio" : "Music"));
        btnToggleMode.setText(currentMode == PlaybackMode.RADIO ? "Switch to Music Mode" : "Switch to Radio Mode");
    }

    private void setupListeners() {
        btnPlayPause.setOnClickListener(v -> {
            if (musicFolder != null) {
                if (isPlaying) {
                    pausePlayback();
                } else {
                    resumePlayback();
                }
            } else {
                showToast("Please select a music folder first!");
            }
        });

        btnNext.setOnClickListener(v -> {
            if (musicFolder != null) {
                audioPlayer.playNext();
            } else {
                showToast("Please select a music folder first!");
            }
        });

        btnToggleMode.setOnClickListener(v -> {
            if (currentMode == PlaybackMode.RADIO) {
                switchToMode(PlaybackMode.MUSIC);
            } else {
                switchToMode(PlaybackMode.RADIO);
            }
        });

        playlistMenuButton.setOnClickListener(v -> showPlaylistBottomSheet());

        settingsButton.setOnClickListener(view ->
                startActivity(new Intent(MainActivity.this, SettingsActivity.class)));

        btnSelectFolder.setOnClickListener(v -> pickMusicFolder());
    }

    private void switchToMode(PlaybackMode mode) {
        currentMode = mode;
        audioPlayer.setMode(mode == PlaybackMode.RADIO ? "radio" : "music");
        txtMode.setText("Mode: " + (mode == PlaybackMode.RADIO ? "Radio" : "Music"));
        btnToggleMode.setText(mode == PlaybackMode.RADIO ? "Switch to Music Mode" : "Switch to Radio Mode");
        prefs.edit().putString(KEY_PLAYBACK_MODE, mode == PlaybackMode.RADIO ? "radio" : "music").apply();
    }

    private void resumePlayback() {
        audioPlayer.resume();
        btnPlayPause.setText("Pause");
        isPlaying = true;
    }

    private void pausePlayback() {
        audioPlayer.pause();
        btnPlayPause.setText("Play");
        isPlaying = false;
    }

    private void pickMusicFolder() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION |
                Intent.FLAG_GRANT_WRITE_URI_PERMISSION |
                Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        folderPickerLauncher.launch(intent);
    }

    private void setMusicFolder(Uri treeUri) {
        musicFolder = treeUri;
        List<Uri> musicUris = new ArrayList<>();

        ContentResolver resolver = getContentResolver();
        Uri docUri = DocumentsContract.buildChildDocumentsUriUsingTree(
                treeUri, DocumentsContract.getTreeDocumentId(treeUri)
        );

        try (Cursor cursor = resolver.query(docUri,
                new String[]{DocumentsContract.Document.COLUMN_DOCUMENT_ID,
                        DocumentsContract.Document.COLUMN_DISPLAY_NAME,
                        DocumentsContract.Document.COLUMN_MIME_TYPE},
                null, null, null)) {

            if (cursor != null) {
                while (cursor.moveToNext()) {
                    String docId = cursor.getString(0);
                    String mime = cursor.getString(2);

                    if (mime != null && mime.startsWith("audio/")) {
                        Uri fileUri = DocumentsContract.buildDocumentUriUsingTree(treeUri, docId);
                        musicUris.add(fileUri);
                    }
                }
            }

            if (musicUris.isEmpty()) {
                showToast("No audio files found in selected folder");
            } else {
                audioPlayer.loadMusic();
                showToast("Loaded " + musicUris.size() + " songs!");
            }

        } catch (Exception e) {
            e.printStackTrace();
            showToast("Failed to load music from folder");
        }
    }


    private void loadSavedMusicFolder() {
        String folderUriStr = prefs.getString(KEY_FOLDER_URI, null);
        if (folderUriStr != null) {
            setMusicFolder(Uri.parse(folderUriStr));
        } else {
            pickMusicFolder();
        }
    }

    private void registerReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_HEADSET_PLUG);
        filter.addAction(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED);
        registerReceiver(headsetReceiver, filter);
    }

    private void showPlaylistBottomSheet() {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        View sheetView = getLayoutInflater().inflate(R.layout.playlist_bottom_sheet, null);
        bottomSheetDialog.setContentView(sheetView);

        RecyclerView recyclerView = sheetView.findViewById(R.id.playlistRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Sample playlist names – replace with dynamic list later if needed
        List<String> playlistNames = Arrays.asList("Favorites", "Chill Vibes", "Workout Mix", "Sleep Mode");

        PlaylistAdapter adapter = new PlaylistAdapter(playlistNames, playlistName -> {
            showToast("Clicked: " + playlistName);
            bottomSheetDialog.dismiss();
        });

        recyclerView.setAdapter(adapter);

        bottomSheetDialog.show();
    }


    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(headsetReceiver);
    }
}
