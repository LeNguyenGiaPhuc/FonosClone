package hcmute.com.fonosclone;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
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

import hcmute.com.fonosclone.data.AppDatabase;
import hcmute.com.fonosclone.data.DownloadedContent;
import hcmute.com.fonosclone.data.ListeningHistory;
import hcmute.com.fonosclone.data.ListeningProgress;

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
    private int lastSavedPositionMs;
    private SeekBar progressBar;
    private TextView timeView;
    private Button playPauseButton;
    private Button downloadButton;
    private FonosRepository repository;

    private final BroadcastReceiver progressReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (!AudioPlayerService.ACTION_PROGRESS.equals(intent.getAction())) return;

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
                    }
                }
                downloadButton.setEnabled(false);
                downloadButton.setText(R.string.downloaded);
            } else if (ContentDownloadService.STATUS_RUNNING.equals(status)) {
                downloadButton.setText(getString(R.string.download_in_progress) + " " + progress + "%");
            } else if (ContentDownloadService.STATUS_CANCELLED.equals(status)) {
                downloadButton.setEnabled(true);
                downloadButton.setText(R.string.download);
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
        lastSavedPositionMs = currentPositionMs;
        coverImage = getIntent().getStringExtra("cover_image");
        String coverColor = getIntent().getStringExtra("cover_color");
        String coverEmoji = getIntent().getStringExtra("cover_emoji");
        repository = new FonosRepository(this);

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
                saveListeningProgress();
                savePlaybackSnapshot(false);
                pauseAudio();
                playPauseButton.setText(R.string.play);
            } else {
                playAudio();
                playPauseButton.setText(R.string.pause);
            }
            isPlaying = !isPlaying;
        });

        stopButton.setOnClickListener(v -> {
            saveListeningProgress();
            savePlaybackSnapshot(true);
            stopAudio();
            currentPositionMs = 0;
            lastSavedPositionMs = 0;
            updateProgressUi();
            playPauseButton.setText(R.string.play);
            isPlaying = false;
        });

        downloadButton.setOnClickListener(v -> {
            if (bookId <= 0) return;
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
        Intent intent = new Intent(this, AudioPlayerService.class);
        intent.setAction(AudioPlayerService.ACTION_PLAY);
        intent.putExtra(AudioPlayerService.EXTRA_TITLE, title);
        intent.putExtra(AudioPlayerService.EXTRA_AUTHOR, author);
        intent.putExtra(AudioPlayerService.EXTRA_AUDIO_RES, audioResName);
        intent.putExtra(AudioPlayerService.EXTRA_BOOK_ID, bookId);
        intent.putExtra(AudioPlayerService.EXTRA_START_POSITION_MS, currentPositionMs);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent);
        } else {
            startService(intent);
        }
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
        saveListeningProgress();
        savePlaybackSnapshot(false);
        stopAudio();
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

    private void saveListeningProgress() {
        if (bookId <= 0 || currentPositionMs <= lastSavedPositionMs) return;

        int deltaSeconds = (currentPositionMs - lastSavedPositionMs) / 1000;
        if (deltaSeconds <= 0) return;

        lastSavedPositionMs += deltaSeconds * 1000;
        new Thread(() -> AppDatabase
                .getInstance(getApplicationContext())
                .fonosDao()
                .insertListeningHistory(new ListeningHistory(bookId, deltaSeconds))
        ).start();
    }

    private void savePlaybackSnapshot(boolean resetPosition) {
        if (bookId <= 0) return;

        int savedPositionMs = resetPosition ? 0 : Math.max(0, currentPositionMs);
        int savedDurationMs = Math.max(0, currentDurationMs);
        repository.saveListeningProgressForCurrentUser(
                bookId,
                title,
                author,
                coverImage,
                savedPositionMs,
                savedDurationMs
        );
        new Thread(() -> AppDatabase
                .getInstance(getApplicationContext())
                .fonosDao()
                .upsertListeningProgress(new ListeningProgress(
                        bookId,
                        savedPositionMs,
                        savedDurationMs,
                        System.currentTimeMillis()
                ))
        ).start();
        SyncScheduler.enqueueUserSync(this);
    }

    private void loadSavedProgress() {
        if (bookId <= 0) return;

        new Thread(() -> {
            ListeningProgress progress = AppDatabase
                    .getInstance(getApplicationContext())
                    .fonosDao()
                    .getListeningProgress(bookId);

            if (progress == null || progress.positionMs <= 0) return;

            runOnUiThread(() -> {
                currentPositionMs = progress.positionMs;
                currentDurationMs = progress.durationMs;
                lastSavedPositionMs = progress.positionMs;
                updateProgressUi();
            });
        }).start();
    }

    private void loadDownloadState() {
        if (bookId <= 0) return;

        new Thread(() -> {
            DownloadedContent downloadedContent = AppDatabase
                    .getInstance(getApplicationContext())
                    .fonosDao()
                    .getDownloadedContent(bookId);

            if (downloadedContent == null) return;

            if (downloadedContent.localAudioPath != null && !downloadedContent.localAudioPath.trim().isEmpty()) {
                File localAudioFile = new File(downloadedContent.localAudioPath);
                if (localAudioFile.exists()) {
                    audioResName = localAudioFile.getAbsolutePath();
                    audioUrl = "";
                    audioStoragePath = "";
                }
            }

            runOnUiThread(() -> {
                downloadButton.setEnabled(false);
                downloadButton.setText(R.string.downloaded);
            });
        }).start();
    }
}
