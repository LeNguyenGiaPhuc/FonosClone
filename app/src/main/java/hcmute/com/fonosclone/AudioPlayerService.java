package hcmute.com.fonosclone;

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

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

public class AudioPlayerService extends Service {

    public static final String ACTION_PLAY = "hcmute.com.fonosclone.action.PLAY";
    public static final String ACTION_PAUSE = "hcmute.com.fonosclone.action.PAUSE";
    public static final String ACTION_STOP = "hcmute.com.fonosclone.action.STOP";
    public static final String ACTION_PROGRESS = "hcmute.com.fonosclone.action.PROGRESS";
    public static final String EXTRA_TITLE = "extra_title";
    public static final String EXTRA_AUTHOR = "extra_author";
    public static final String EXTRA_AUDIO_RES = "extra_audio_res";
    public static final String EXTRA_POSITION_MS = "extra_position_ms";
    public static final String EXTRA_DURATION_MS = "extra_duration_ms";
    public static final String EXTRA_IS_PLAYING = "extra_is_playing";

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
    private String currentTitle = "";
    private String currentAuthor = "";

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
            currentAudioResName = audioResName;
            play(audioResName);
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

    private void play(String audioResName) {
        int audioResId = 0;
        if (audioResName != null && !audioResName.isEmpty()) {
            audioResId = getResources().getIdentifier(audioResName, "raw", getPackageName());
        }
        if (audioResId == 0 && currentAudioResId != 0) {
            audioResId = currentAudioResId;
        }
        if (audioResId == 0) {
            audioResId = R.raw.demo_audio;
            currentAudioResName = "demo_audio";
        }

        if (mediaPlayer == null || currentAudioResId != audioResId) {
            releasePlayer();
            currentAudioResId = audioResId;
            mediaPlayer = MediaPlayer.create(this, audioResId);
            mediaPlayer.setOnCompletionListener(mp -> {
                sendProgressUpdate();
                stopForeground(false);
            });
        }

        if (!mediaPlayer.isPlaying()) {
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

        intent.putExtra(EXTRA_POSITION_MS, position);
        intent.putExtra(EXTRA_DURATION_MS, duration);
        intent.putExtra(EXTRA_IS_PLAYING, isPlaying);
        sendBroadcast(intent);
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

        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_media_play)
                .setContentTitle(title)
                .setContentText(author)
                .setContentIntent(contentIntent)
                .setOngoing(isPlaying)
                .addAction(
                        isPlaying ? android.R.drawable.ic_media_pause : android.R.drawable.ic_media_play,
                        isPlaying ? getString(R.string.pause) : getString(R.string.play),
                        togglePendingIntent
                )
                .addAction(android.R.drawable.ic_menu_close_clear_cancel, getString(R.string.stop), stopPendingIntent)
                .build();
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
        releasePlayer();
        super.onDestroy();
    }
}
