package hcmute.com.fonosclone.ui.activity;


import hcmute.com.fonosclone.auth.UserIdentity;
import hcmute.com.fonosclone.data.local.AppDatabase;
import hcmute.com.fonosclone.data.model.DownloadedContent;
import hcmute.com.fonosclone.data.model.ListeningProgress;
import hcmute.com.fonosclone.R;
import hcmute.com.fonosclone.service.AudioPlayerService;
import hcmute.com.fonosclone.service.ContentDownloadService;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.graphics.Color;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.Locale;


public class PlayerActivity extends BaseActivity {

    public static final String EXTRA_BOOK_ID = "extra_book_id";

    private String title;
    private String author;
    private String audioResName;
    private String audioUrl;
    private String audioStoragePath;
    private String coverImage;
    private int bookId;
    private boolean isPlaying;
    private int currentPositionMs;
    private int currentDurationMs;
    private SeekBar progressBar;
    private TextView timeView;
    private Button playPauseButton;
    private Button downloadButton;
    private boolean hasDownloadedAudio;
    private boolean isDownloadStateLoaded;

    private final BroadcastReceiver progressReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (!AudioPlayerService.ACTION_PROGRESS.equals(intent.getAction())) return;

            int progressBookId = intent.getIntExtra(AudioPlayerService.EXTRA_BOOK_ID, 0);
            if (progressBookId != bookId) return;

            currentPositionMs = intent.getIntExtra(AudioPlayerService.EXTRA_POSITION_MS, 0);
            currentDurationMs = intent.getIntExtra(AudioPlayerService.EXTRA_DURATION_MS, 0);
            isPlaying = intent.getBooleanExtra(AudioPlayerService.EXTRA_IS_PLAYING, false);
            updateProgressUi();
        }
    };

    private final BroadcastReceiver downloadReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (!ContentDownloadService.ACTION_DOWNLOAD_PROGRESS.equals(intent.getAction())) return;
            int downloadedBookId = intent.getIntExtra(ContentDownloadService.EXTRA_BOOK_ID, 0);
            if (downloadedBookId != bookId) return;

            String status = intent.getStringExtra(ContentDownloadService.EXTRA_STATUS);
            int progress = intent.getIntExtra(ContentDownloadService.EXTRA_PROGRESS, 0);
            if (ContentDownloadService.STATUS_COMPLETED.equals(status)) {
                String localAudioPath = intent.getStringExtra(ContentDownloadService.EXTRA_LOCAL_AUDIO_PATH);
                if (localAudioPath != null && !localAudioPath.trim().isEmpty()) {
                    File localAudioFile = new File(localAudioPath);
                    if (localAudioFile.exists()) {
                        audioResName = localAudioFile.getAbsolutePath();
                        audioUrl = "";
                        audioStoragePath = "";
                        hasDownloadedAudio = true;
                        isDownloadStateLoaded = true;
                    }
                }
                downloadButton.setEnabled(false);
                downloadButton.setText(R.string.downloaded);
            } else if (ContentDownloadService.STATUS_RUNNING.equals(status)) {
                downloadButton.setText(getString(R.string.download_in_progress) + " " + progress + "%");
            } else if (ContentDownloadService.STATUS_CANCELLED.equals(status)) {
                downloadButton.setEnabled(true);
                downloadButton.setText(R.string.download);
            } else if (ContentDownloadService.STATUS_FAILED.equals(status)) {
                String errorMessage = intent.getStringExtra(ContentDownloadService.EXTRA_ERROR_MESSAGE);
                downloadButton.setEnabled(true);
                downloadButton.setText(R.string.download);
                Toast.makeText(
                        PlayerActivity.this,
                        errorMessage == null || errorMessage.trim().isEmpty()
                                ? getString(R.string.download_failed)
                                : errorMessage,
                        Toast.LENGTH_LONG
                ).show();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        applySystemBarPadding(R.id.main);

        title = getIntent().getStringExtra(AudioPlayerService.EXTRA_TITLE);
        author = getIntent().getStringExtra(AudioPlayerService.EXTRA_AUTHOR);
        audioResName = getIntent().getStringExtra(AudioPlayerService.EXTRA_AUDIO_RES);
        audioUrl = getIntent().getStringExtra(AudioPlayerService.EXTRA_AUDIO_URL);
        audioStoragePath = getIntent().getStringExtra(AudioPlayerService.EXTRA_AUDIO_STORAGE_PATH);
        bookId = getIntent().getIntExtra(EXTRA_BOOK_ID, 0);
        currentPositionMs = getIntent().getIntExtra(AudioPlayerService.EXTRA_START_POSITION_MS, 0);
        coverImage = getIntent().getStringExtra("cover_image");
        String coverColor = getIntent().getStringExtra("cover_color");
        String coverEmoji = getIntent().getStringExtra("cover_emoji");

        TextView titleView = findViewById(R.id.tvPlayerTitle);
        TextView authorView = findViewById(R.id.tvPlayerAuthor);
        ImageView coverView = findViewById(R.id.ivPlayerCover);
        com.google.android.material.card.MaterialCardView coverCard = findViewById(R.id.cvPlayerCoverCard);
        TextView emojiView = findViewById(R.id.tvPlayerEmoji);
        playPauseButton = findViewById(R.id.btnPlayPause);
        Button stopButton = findViewById(R.id.btnStop);
        downloadButton = findViewById(R.id.btnDownload);
        progressBar = findViewById(R.id.sbPlayerProgress);
        timeView = findViewById(R.id.tvPlayerTime);

        requestNotificationPermissionIfNeeded();
        findViewById(R.id.btnPlayerBack).setOnClickListener(v -> closePlayer());

        titleView.setText(title);
        authorView.setText(author);

        if (coverColor != null && !coverColor.isEmpty()) {
            // It's a PodCourse/Podcast: Show color and emoji
            coverView.setVisibility(View.GONE);
            emojiView.setVisibility(View.VISIBLE);
            emojiView.setText(coverEmoji != null ? coverEmoji : "ðŸŽ“");
            coverCard.setCardBackgroundColor(Color.parseColor(coverColor));
        } else {
            // It's a standard Book: Show image cover and reset background to white
            coverView.setVisibility(View.VISIBLE);
            emojiView.setVisibility(View.GONE);
            coverCard.setCardBackgroundColor(Color.WHITE);
            if (coverImage != null && !coverImage.isEmpty()) {
                int coverResId = getResources().getIdentifier(coverImage, "drawable", getPackageName());
                if (coverResId != 0) {
                    coverView.setImageResource(coverResId);
                } else {
                    coverView.setImageResource(android.R.drawable.ic_menu_gallery);
                }
            } else {
                coverView.setImageResource(android.R.drawable.ic_menu_gallery);
            }
        }

        playPauseButton.setOnClickListener(v -> {
            if (isPlaying) {
                pauseAudio();
                playPauseButton.setText(R.string.play);
                isPlaying = false;
            } else {
                playAudio();
            }
        });

        stopButton.setOnClickListener(v -> {
            stopAudio();
            currentPositionMs = 0;
            updateProgressUi();
            playPauseButton.setText(R.string.play);
            isPlaying = false;
        });

        updateDownloadAvailability();
        downloadButton.setOnClickListener(v -> {
            if (bookId <= 0) return;
            if (hasDownloadedAudio) {
                downloadButton.setEnabled(false);
                downloadButton.setText(R.string.downloaded);
                return;
            }
            if (!isNetworkAvailable()) {
                Toast.makeText(this, R.string.download_no_network, Toast.LENGTH_LONG).show();
                return;
            }
            downloadButton.setText(getString(R.string.download_in_progress) + " 0%");
            ContentDownloadService.startDownload(this, bookId, title, audioResName, audioUrl, audioStoragePath);
            Toast.makeText(this, R.string.download_started, Toast.LENGTH_SHORT).show();
        });

        loadSavedProgress();
        loadDownloadState();
        updateProgressUi();
    }

    @Override
    protected void onStart() {
        super.onStart();
        IntentFilter filter = new IntentFilter(AudioPlayerService.ACTION_PROGRESS);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(progressReceiver, filter, Context.RECEIVER_NOT_EXPORTED);
        } else {
            registerReceiver(progressReceiver, filter);
        }

        IntentFilter downloadFilter = new IntentFilter(ContentDownloadService.ACTION_DOWNLOAD_PROGRESS);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(downloadReceiver, downloadFilter, Context.RECEIVER_NOT_EXPORTED);
        } else {
            registerReceiver(downloadReceiver, downloadFilter);
        }
    }

    @Override
    protected void onStop() {
        unregisterReceiver(progressReceiver);
        unregisterReceiver(downloadReceiver);
        super.onStop();
    }

    private void playAudio() {
        if (hasDownloadedAudio && isLocalAudioFileAvailable()) {
            startAudioService();
            return;
        }

        if (!isDownloadStateLoaded) {
            new Thread(() -> {
                boolean downloaded = loadDownloadedAudioIfAvailable();
                runOnUiThread(() -> {
                    if (downloaded) {
                        startAudioService();
                    } else if (isNetworkAvailable()) {
                        startAudioService();
                    } else {
                        Toast.makeText(PlayerActivity.this, R.string.play_no_network, Toast.LENGTH_LONG).show();
                        updateProgressUi();
                    }
                });
            }).start();
            return;
        }

        if (!isNetworkAvailable()) {
            Toast.makeText(this, R.string.play_no_network, Toast.LENGTH_LONG).show();
            updateProgressUi();
            return;
        }

        startAudioService();
    }

    private void startAudioService() {
        Intent intent = new Intent(this, AudioPlayerService.class);
        intent.setAction(AudioPlayerService.ACTION_PLAY);
        intent.putExtra(AudioPlayerService.EXTRA_TITLE, title);
        intent.putExtra(AudioPlayerService.EXTRA_AUTHOR, author);
        intent.putExtra(AudioPlayerService.EXTRA_AUDIO_RES, audioResName);
        intent.putExtra(AudioPlayerService.EXTRA_AUDIO_URL, audioUrl);
        intent.putExtra(AudioPlayerService.EXTRA_AUDIO_STORAGE_PATH, audioStoragePath);
        intent.putExtra(AudioPlayerService.EXTRA_COVER_IMAGE, coverImage);
        intent.putExtra(AudioPlayerService.EXTRA_BOOK_ID, bookId);
        intent.putExtra(AudioPlayerService.EXTRA_START_POSITION_MS, currentPositionMs);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent);
        } else {
            startService(intent);
        }
        isPlaying = true;
        playPauseButton.setText(R.string.pause);
    }

    private void requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, 67);
        }
    }

    private void pauseAudio() {
        Intent intent = new Intent(this, AudioPlayerService.class);
        intent.setAction(AudioPlayerService.ACTION_PAUSE);
        startService(intent);
    }

    private void stopAudio() {
        Intent intent = new Intent(this, AudioPlayerService.class);
        intent.setAction(AudioPlayerService.ACTION_STOP);
        startService(intent);
    }

    private void closePlayer() {
        finish();
    }

    @Override
    public void onBackPressed() {
        closePlayer();
    }

    private void updateProgressUi() {
        if (currentDurationMs > 0) {
            progressBar.setMax(currentDurationMs);
            progressBar.setProgress(Math.min(currentPositionMs, currentDurationMs));
        } else {
            progressBar.setMax(1000);
            progressBar.setProgress(0);
        }

        timeView.setText(getString(
                R.string.player_time_format,
                formatTime(currentPositionMs),
                formatTime(currentDurationMs)
        ));
        playPauseButton.setText(isPlaying ? R.string.pause : R.string.play);
    }

    private String formatTime(int millis) {
        int totalSeconds = Math.max(0, millis / 1000);
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        return String.format(Locale.US, "%02d:%02d", minutes, seconds);
    }

    private void loadSavedProgress() {
        if (bookId <= 0) return;

        new Thread(() -> {
            ListeningProgress progress = AppDatabase
                    .getInstance(getApplicationContext())
                    .fonosDao()
                    .getListeningProgress(UserIdentity.getCurrentUserId(getApplicationContext()), bookId);

            if (progress == null || progress.positionMs <= 0) return;

            runOnUiThread(() -> {
                currentPositionMs = progress.positionMs;
                currentDurationMs = progress.durationMs;
                updateProgressUi();
            });
        }).start();
    }

    private void loadDownloadState() {
        if (bookId <= 0) return;

        new Thread(() -> {
            boolean downloaded = loadDownloadedAudioIfAvailable();

            runOnUiThread(() -> {
                if (downloaded) {
                    downloadButton.setEnabled(false);
                    downloadButton.setText(R.string.downloaded);
                } else {
                    updateDownloadAvailability();
                }
            });
        }).start();
    }

    private void updateDownloadAvailability() {
        if (downloadButton == null) return;

        boolean downloadable = bookId > 0;
        downloadButton.setEnabled(downloadable);
        downloadButton.setText(downloadable ? R.string.download : R.string.download_unavailable);
    }

    private boolean loadDownloadedAudioIfAvailable() {
        DownloadedContent downloadedContent = AppDatabase
                .getInstance(getApplicationContext())
                .fonosDao()
                .getDownloadedContent(UserIdentity.getCurrentUserId(getApplicationContext()), bookId);

        isDownloadStateLoaded = true;
        if (downloadedContent == null
                || downloadedContent.localAudioPath == null
                || downloadedContent.localAudioPath.trim().isEmpty()) {
            hasDownloadedAudio = false;
            return false;
        }

        File localAudioFile = new File(downloadedContent.localAudioPath);
        if (!localAudioFile.exists() || localAudioFile.length() == 0) {
            hasDownloadedAudio = false;
            return false;
        }

        audioResName = localAudioFile.getAbsolutePath();
        audioUrl = "";
        audioStoragePath = "";
        hasDownloadedAudio = true;
        return true;
    }

    private boolean isLocalAudioFileAvailable() {
        if (audioResName == null || audioResName.trim().isEmpty()) {
            return false;
        }
        File localAudioFile = new File(audioResName);
        return localAudioFile.exists() && localAudioFile.length() > 0;
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager manager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (manager == null) {
            return false;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            android.net.Network network = manager.getActiveNetwork();
            if (network == null) {
                return false;
            }
            NetworkCapabilities capabilities = manager.getNetworkCapabilities(network);
            return capabilities != null
                    && capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                    && capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED);
        }

        NetworkInfo activeNetwork = manager.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnected();
    }
}
