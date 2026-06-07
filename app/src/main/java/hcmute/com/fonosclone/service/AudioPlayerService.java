package hcmute.com.fonosclone.service;


import hcmute.com.fonosclone.auth.UserIdentity;
import hcmute.com.fonosclone.data.local.AppDatabase;
import hcmute.com.fonosclone.data.local.FonosDao;
import hcmute.com.fonosclone.data.model.ListeningProgress;
import hcmute.com.fonosclone.R;
import hcmute.com.fonosclone.ui.activity.MainActivity;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;


public class    AudioPlayerService extends Service {

    public static final String ACTION_PLAY = "hcmute.com.fonosclone.action.PLAY";
    public static final String ACTION_PAUSE = "hcmute.com.fonosclone.action.PAUSE";
    public static final String ACTION_STOP = "hcmute.com.fonosclone.action.STOP";
    public static final String ACTION_PROGRESS = "hcmute.com.fonosclone.action.PROGRESS";
    public static final String EXTRA_TITLE = "extra_title";
    public static final String EXTRA_AUTHOR = "extra_author";
    public static final String EXTRA_AUDIO_RES = "extra_audio_res";
    public static final String EXTRA_AUDIO_URL = "extra_audio_url";
    public static final String EXTRA_AUDIO_STORAGE_PATH = "extra_audio_storage_path";
    public static final String EXTRA_BOOK_ID = "extra_book_id";
    public static final String EXTRA_START_POSITION_MS = "extra_start_position_ms";
    public static final String EXTRA_POSITION_MS = "extra_position_ms";
    public static final String EXTRA_DURATION_MS = "extra_duration_ms";
    public static final String EXTRA_IS_PLAYING = "extra_is_playing";

    private static final String TAG = "AudioPlayerService";
    private static final String CHANNEL_ID = "audio_playback";
    private static final int NOTIFICATION_ID = 67;

    private MediaPlayer mediaPlayer;
    private final Handler progressHandler = new Handler(Looper.getMainLooper());
    private final Runnable progressRunnable = new Runnable() {
        @Override
        public void run() {
            sendProgressUpdate();
            if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                progressHandler.postDelayed(this, 500);
            }
        }
    };
    private int currentAudioResId;
    private String currentAudioResName = "";
    private String currentAudioUrl = "";
    private String currentAudioStoragePath = "";
    private String currentTitle = "";
    private String currentAuthor = "";
    private int currentBookId;
    private int pendingStartPositionMs;
    private int lastPersistedPositionMs;
    private int lastNotificationPositionMs = -1000;

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null || intent.getAction() == null) {
            return START_NOT_STICKY;
        }

        String action = intent.getAction();
        if (ACTION_PLAY.equals(action)) {
            currentTitle = intent.getStringExtra(EXTRA_TITLE);
            currentAuthor = intent.getStringExtra(EXTRA_AUTHOR);
            String audioResName = intent.getStringExtra(EXTRA_AUDIO_RES);
            String audioUrl = intent.getStringExtra(EXTRA_AUDIO_URL);
            String audioStoragePath = intent.getStringExtra(EXTRA_AUDIO_STORAGE_PATH);
            currentBookId = intent.getIntExtra(EXTRA_BOOK_ID, currentBookId);
            pendingStartPositionMs = intent.getIntExtra(EXTRA_START_POSITION_MS, 0);
            currentAudioResName = isBlank(audioResName) ? "demo_audio" : audioResName;
            currentAudioStoragePath = audioStoragePath == null ? "" : audioStoragePath;

            startForeground(NOTIFICATION_ID, buildNotification(false));
            playBestAvailableSource(audioUrl, audioStoragePath, currentAudioResName);
        } else if (ACTION_PAUSE.equals(action)) {
            pause();
        } else if (ACTION_STOP.equals(action)) {
            stopPlayback();
            stopSelf();
        }

        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void playBestAvailableSource(String audioUrl, String audioStoragePath, String fallbackAudioResName) {
        if (!isBlank(audioUrl)) {
            play(audioUrl);
            return;
        }
        if (!isBlank(audioStoragePath)) {
            playFromFirebaseStorage(audioStoragePath, fallbackAudioResName);
            return;
        }
        play(fallbackAudioResName);
    }

    private void playFromFirebaseStorage(String storagePath, String fallbackAudioResName) {
        try {
            StorageReference reference = storagePath.startsWith("gs://")
                    ? FirebaseStorage.getInstance().getReferenceFromUrl(storagePath)
                    : FirebaseStorage.getInstance().getReference().child(storagePath);
            reference.getDownloadUrl()
                    .addOnSuccessListener(uri -> play(uri.toString()))
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Failed to resolve Firebase Storage audio. Falling back to raw resource.", e);
                        play(fallbackAudioResName);
                    });
        } catch (Exception e) {
            Log.e(TAG, "Invalid Firebase Storage audio path. Falling back to raw resource.", e);
            play(fallbackAudioResName);
        }
    }

    private void play(String audioSource) {
        if (isBlank(audioSource)) {
            audioSource = "demo_audio";
        }

        boolean isUrl = audioSource.startsWith("http://") || audioSource.startsWith("https://");
        boolean isFile = audioSource.startsWith("file://") || audioSource.startsWith("/");

        if (isUrl || isFile) {
            String dataSource = audioSource.startsWith("file://") ? audioSource.substring("file://".length()) : audioSource;
            if (mediaPlayer == null || !currentAudioUrl.equals(dataSource)) {
                releasePlayer();
                currentAudioUrl = dataSource;
                currentAudioResName = audioSource;
                try {
                    mediaPlayer = new MediaPlayer();
                    mediaPlayer.setDataSource(dataSource);
                    mediaPlayer.setOnPreparedListener(mp -> {
                        if (mediaPlayer != null) {
                            seekToPendingStartPosition();
                            mediaPlayer.start();
                            progressHandler.removeCallbacks(progressRunnable);
                            progressHandler.post(progressRunnable);
                            startForeground(NOTIFICATION_ID, buildNotification(true));
                        }
                    });
                    mediaPlayer.setOnCompletionListener(mp -> {
                        sendProgressUpdate();
                        stopForeground(false);
                    });
                    mediaPlayer.prepareAsync();
                } catch (Exception e) {
                    Log.e(TAG, "Unable to play audio source. Falling back to demo_audio.", e);
                    playLocalRaw("demo_audio");
                }
            } else {
                resumeExistingPlayer();
            }
            return;
        }

        playLocalRaw(audioSource);
    }

    private void playLocalRaw(String audioResName) {
        int audioResId = getResources().getIdentifier(audioResName, "raw", getPackageName());
        if (audioResId == 0) {
            audioResId = R.raw.demo_audio;
            currentAudioResName = "demo_audio";
        }

        if (mediaPlayer == null || currentAudioResId != audioResId || !currentAudioUrl.isEmpty()) {
            releasePlayer();
            currentAudioResId = audioResId;
            currentAudioUrl = "";
            mediaPlayer = MediaPlayer.create(this, audioResId);
            mediaPlayer.setOnCompletionListener(mp -> {
                sendProgressUpdate();
                persistProgress(0, mediaPlayer != null ? mediaPlayer.getDuration() : 0, false);
                stopForeground(false);
            });
        }

        seekToPendingStartPosition();
        resumeExistingPlayer();
    }

    private void resumeExistingPlayer() {
        if (mediaPlayer != null && !mediaPlayer.isPlaying()) {
            mediaPlayer.start();
        }
        progressHandler.removeCallbacks(progressRunnable);
        progressHandler.post(progressRunnable);
        startForeground(NOTIFICATION_ID, buildNotification(true));
    }

    private void pause() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
        }
        progressHandler.removeCallbacks(progressRunnable);
        sendProgressUpdate();
        startForeground(NOTIFICATION_ID, buildNotification(false));
    }

    private void stopPlayback() {
        progressHandler.removeCallbacks(progressRunnable);
        sendProgressUpdate();
        releasePlayer();
        stopForeground(true);
    }

    private void releasePlayer() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
            currentAudioResId = 0;
            currentAudioUrl = "";
        }
    }

    private void sendProgressUpdate() {
        Intent intent = new Intent(ACTION_PROGRESS);
        intent.setPackage(getPackageName());

        int position = 0;
        int duration = 0;
        boolean isPlaying = false;

        if (mediaPlayer != null) {
            position = mediaPlayer.getCurrentPosition();
            duration = mediaPlayer.getDuration();
            isPlaying = mediaPlayer.isPlaying();
        }

        persistProgress(position, duration, isPlaying);

        intent.putExtra(EXTRA_BOOK_ID, currentBookId);
        intent.putExtra(EXTRA_POSITION_MS, position);
        intent.putExtra(EXTRA_DURATION_MS, duration);
        intent.putExtra(EXTRA_IS_PLAYING, isPlaying);
        sendBroadcast(intent);

        updatePlaybackNotification(position, duration, isPlaying);
    }

    private Notification buildNotification(boolean isPlaying) {
        Intent openPlayerIntent = new Intent(this, MainActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(
                this,
                0,
                openPlayerIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        Intent toggleIntent = new Intent(this, AudioPlayerService.class);
        toggleIntent.setAction(isPlaying ? ACTION_PAUSE : ACTION_PLAY);
        toggleIntent.putExtra(EXTRA_TITLE, currentTitle);
        toggleIntent.putExtra(EXTRA_AUTHOR, currentAuthor);
        toggleIntent.putExtra(EXTRA_AUDIO_RES, currentAudioResName);
        toggleIntent.putExtra(EXTRA_AUDIO_URL, currentAudioUrl);
        toggleIntent.putExtra(EXTRA_AUDIO_STORAGE_PATH, currentAudioStoragePath);
        toggleIntent.putExtra(EXTRA_BOOK_ID, currentBookId);
        PendingIntent togglePendingIntent = PendingIntent.getService(
                this,
                1,
                toggleIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        Intent stopIntent = new Intent(this, AudioPlayerService.class);
        stopIntent.setAction(ACTION_STOP);
        PendingIntent stopPendingIntent = PendingIntent.getService(
                this,
                2,
                stopIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        String title = currentTitle == null || currentTitle.isEmpty()
                ? getString(R.string.audio_notification_title)
                : currentTitle;
        String author = currentAuthor == null || currentAuthor.isEmpty()
                ? getString(R.string.audio_notification_subtitle)
                : currentAuthor;

        int position = 0;
        int duration = 0;
        if (mediaPlayer != null) {
            position = mediaPlayer.getCurrentPosition();
            duration = mediaPlayer.getDuration();
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_media_play)
                .setContentTitle(title)
                .setContentText(author)
                .setContentIntent(contentIntent)
                .setCategory(NotificationCompat.CATEGORY_TRANSPORT)
                .setOnlyAlertOnce(true)
                .setShowWhen(false)
                .setOngoing(isPlaying)
                .addAction(
                        isPlaying ? android.R.drawable.ic_media_pause : android.R.drawable.ic_media_play,
                        isPlaying ? getString(R.string.pause) : getString(R.string.play),
                        togglePendingIntent
                )
                .addAction(android.R.drawable.ic_menu_close_clear_cancel, getString(R.string.stop), stopPendingIntent);

        if (duration > 0) {
            builder.setProgress(duration, Math.min(position, duration), false)
                    .setSubText(formatTime(position) + " / " + formatTime(duration));
        }

        return builder.build();
    }

    private void updatePlaybackNotification(int position, int duration, boolean isPlaying) {
        if (duration <= 0 || mediaPlayer == null) {
            return;
        }
        if (isPlaying && Math.abs(position - lastNotificationPositionMs) < 1000) {
            return;
        }

        lastNotificationPositionMs = position;
        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (manager != null) {
            manager.notify(NOTIFICATION_ID, buildNotification(isPlaying));
        }
    }

    private String formatTime(int millis) {
        int totalSeconds = Math.max(0, millis / 1000);
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        return String.format(java.util.Locale.US, "%02d:%02d", minutes, seconds);
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    getString(R.string.audio_channel_name),
                    NotificationManager.IMPORTANCE_LOW
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    @Override
    public void onDestroy() {
        progressHandler.removeCallbacks(progressRunnable);
        sendProgressUpdate();
        releasePlayer();
        super.onDestroy();
    }

    private void seekToPendingStartPosition() {
        if (mediaPlayer == null || pendingStartPositionMs <= 0) {
            return;
        }

        int duration = mediaPlayer.getDuration();
        if (duration > 0 && pendingStartPositionMs < duration - 1000) {
            mediaPlayer.seekTo(pendingStartPositionMs);
            lastPersistedPositionMs = pendingStartPositionMs;
        }
        pendingStartPositionMs = 0;
    }

    private void persistProgress(int positionMs, int durationMs, boolean isPlaying) {
        if (currentBookId <= 0 || durationMs <= 0) {
            return;
        }

        if (positionMs >= durationMs - 1000) {
            positionMs = 0;
        }

        if (isPlaying && Math.abs(positionMs - lastPersistedPositionMs) < 2000) {
            return;
        }

        lastPersistedPositionMs = positionMs;
        int finalPositionMs = Math.max(0, positionMs);
        int finalDurationMs = Math.max(0, durationMs);
        new Thread(() -> AppDatabase
                .getInstance(getApplicationContext())
                .fonosDao()
                .upsertListeningProgress(new ListeningProgress(
                        UserIdentity.getCurrentUserId(getApplicationContext()),
                        currentBookId,
                        finalPositionMs,
                        finalDurationMs,
                        System.currentTimeMillis()
                ))
        ).start();
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
