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

import java.util.Locale;

import hcmute.com.fonosclone.data.AppDatabase;
import hcmute.com.fonosclone.data.ListeningHistory;

public class PlayerActivity extends BaseActivity {

    public static final String EXTRA_BOOK_ID = "extra_book_id";

    private String title;
    private String author;
    private String audioResName;
    private int bookId;
    private boolean isPlaying;
    private int currentPositionMs;
    private int currentDurationMs;
    private int lastSavedPositionMs;
    private SeekBar progressBar;
    private TextView timeView;
    private Button playPauseButton;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        applySystemBarPadding(R.id.main);

        title = getIntent().getStringExtra(AudioPlayerService.EXTRA_TITLE);
        author = getIntent().getStringExtra(AudioPlayerService.EXTRA_AUTHOR);
        audioResName = getIntent().getStringExtra(AudioPlayerService.EXTRA_AUDIO_RES);
        bookId = getIntent().getIntExtra(EXTRA_BOOK_ID, 0);
        String coverImage = getIntent().getStringExtra("cover_image");
        String coverColor = getIntent().getStringExtra("cover_color");
        String coverEmoji = getIntent().getStringExtra("cover_emoji");

        TextView titleView = findViewById(R.id.tvPlayerTitle);
        TextView authorView = findViewById(R.id.tvPlayerAuthor);
        ImageView coverView = findViewById(R.id.ivPlayerCover);
        com.google.android.material.card.MaterialCardView coverCard = findViewById(R.id.cvPlayerCoverCard);
        TextView emojiView = findViewById(R.id.tvPlayerEmoji);
        playPauseButton = findViewById(R.id.btnPlayPause);
        Button stopButton = findViewById(R.id.btnStop);
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
            emojiView.setText(coverEmoji != null ? coverEmoji : "🎓");
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
            stopAudio();
            currentPositionMs = 0;
            lastSavedPositionMs = 0;
            updateProgressUi();
            playPauseButton.setText(R.string.play);
            isPlaying = false;
        });
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
    }

    @Override
    protected void onStop() {
        unregisterReceiver(progressReceiver);
        super.onStop();
    }

    private void playAudio() {
        Intent intent = new Intent(this, AudioPlayerService.class);
        intent.setAction(AudioPlayerService.ACTION_PLAY);
        intent.putExtra(AudioPlayerService.EXTRA_TITLE, title);
        intent.putExtra(AudioPlayerService.EXTRA_AUTHOR, author);
        intent.putExtra(AudioPlayerService.EXTRA_AUDIO_RES, audioResName);

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
}
