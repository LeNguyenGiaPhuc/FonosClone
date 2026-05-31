package hcmute.com.fonosclone;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import hcmute.com.fonosclone.data.AppDatabase;
import hcmute.com.fonosclone.data.DownloadedContent;

public class ContentDownloadService extends Service {

    public static final String ACTION_START_DOWNLOAD = "hcmute.com.fonosclone.action.START_DOWNLOAD";
    public static final String ACTION_CANCEL_DOWNLOAD = "hcmute.com.fonosclone.action.CANCEL_DOWNLOAD";
    public static final String ACTION_DOWNLOAD_PROGRESS = "hcmute.com.fonosclone.action.DOWNLOAD_PROGRESS";

    public static final String EXTRA_CONTENT_ID = "extra_content_id";
    public static final String EXTRA_BOOK_ID = "extra_book_id";
    public static final String EXTRA_TITLE = "extra_title";
    public static final String EXTRA_PROGRESS = "extra_progress";
    public static final String EXTRA_STATUS = "extra_status";

    public static final String STATUS_RUNNING = "running";
    public static final String STATUS_COMPLETED = "completed";
    public static final String STATUS_CANCELLED = "cancelled";

    private static final String CHANNEL_ID = "content_download";
    private static final int NOTIFICATION_ID = 68;
    private static final int PROGRESS_MAX = 100;
    private static final int PROGRESS_STEP = 5;
    private static final long PROGRESS_DELAY_MS = 350L;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private String currentContentId = "";
    private String currentTitle = "";
    private int currentBookId;
    private int progress;

    private final Runnable progressRunnable = new Runnable() {
        @Override
        public void run() {
            progress = Math.min(PROGRESS_MAX, progress + PROGRESS_STEP);

            if (progress >= PROGRESS_MAX) {
                markContentDownloaded();
                sendProgressBroadcast(STATUS_COMPLETED);
                showCompletedNotification();
                stopForeground(false);
                stopSelf();
                return;
            }

            sendProgressBroadcast(STATUS_RUNNING);
            updateNotification(progress);
            handler.postDelayed(this, PROGRESS_DELAY_MS);
        }
    };

    public static void startDownload(Context context, String contentId, String title) {
        Intent intent = new Intent(context, ContentDownloadService.class);
        intent.setAction(ACTION_START_DOWNLOAD);
        intent.putExtra(EXTRA_CONTENT_ID, contentId);
        intent.putExtra(EXTRA_TITLE, title);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent);
        } else {
            context.startService(intent);
        }
    }

    public static void startDownload(Context context, int bookId, String title) {
        Intent intent = new Intent(context, ContentDownloadService.class);
        intent.setAction(ACTION_START_DOWNLOAD);
        intent.putExtra(EXTRA_BOOK_ID, bookId);
        intent.putExtra(EXTRA_CONTENT_ID, "book_" + bookId);
        intent.putExtra(EXTRA_TITLE, title);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent);
        } else {
            context.startService(intent);
        }
    }

    public static void cancelDownload(Context context) {
        Intent intent = new Intent(context, ContentDownloadService.class);
        intent.setAction(ACTION_CANCEL_DOWNLOAD);
        context.startService(intent);
    }

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

        if (ACTION_START_DOWNLOAD.equals(intent.getAction())) {
            currentBookId = intent.getIntExtra(EXTRA_BOOK_ID, 0);
            currentContentId = intent.getStringExtra(EXTRA_CONTENT_ID);
            currentTitle = intent.getStringExtra(EXTRA_TITLE);
            if (currentTitle == null || currentTitle.trim().isEmpty()) {
                currentTitle = getString(R.string.download_default_title);
            }
            startDownloadFlow();
        } else if (ACTION_CANCEL_DOWNLOAD.equals(intent.getAction())) {
            cancelDownloadFlow();
        }

        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void startDownloadFlow() {
        handler.removeCallbacks(progressRunnable);
        progress = 0;
        sendProgressBroadcast(STATUS_RUNNING);
        startForeground(NOTIFICATION_ID, buildNotification(progress, false));
        handler.postDelayed(progressRunnable, PROGRESS_DELAY_MS);
    }

    private void cancelDownloadFlow() {
        handler.removeCallbacks(progressRunnable);
        sendProgressBroadcast(STATUS_CANCELLED);
        stopForeground(true);
        stopSelf();
    }

    private void updateNotification(int progressValue) {
        NotificationManager manager = getSystemService(NotificationManager.class);
        if (manager != null) {
            manager.notify(NOTIFICATION_ID, buildNotification(progressValue, false));
        }
    }

    private void showCompletedNotification() {
        NotificationManager manager = getSystemService(NotificationManager.class);
        if (manager != null) {
            manager.notify(NOTIFICATION_ID, buildNotification(PROGRESS_MAX, true));
        }
    }

    private Notification buildNotification(int progressValue, boolean completed) {
        Intent openAppIntent = new Intent(this, MainActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(
                this,
                0,
                openAppIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.stat_sys_download)
                .setContentTitle(completed ? getString(R.string.download_completed) : getString(R.string.download_in_progress))
                .setContentText(currentTitle)
                .setContentIntent(contentIntent)
                .setOnlyAlertOnce(true)
                .setOngoing(!completed)
                .setProgress(PROGRESS_MAX, progressValue, false);

        if (!completed) {
            Intent cancelIntent = new Intent(this, ContentDownloadService.class);
            cancelIntent.setAction(ACTION_CANCEL_DOWNLOAD);
            PendingIntent cancelPendingIntent = PendingIntent.getService(
                    this,
                    1,
                    cancelIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );
            builder.addAction(
                    android.R.drawable.ic_menu_close_clear_cancel,
                    getString(R.string.cancel),
                    cancelPendingIntent
            );
        }

        return builder.build();
    }

    private void sendProgressBroadcast(String status) {
        Intent intent = new Intent(ACTION_DOWNLOAD_PROGRESS);
        intent.setPackage(getPackageName());
        intent.putExtra(EXTRA_CONTENT_ID, currentContentId);
        intent.putExtra(EXTRA_BOOK_ID, currentBookId);
        intent.putExtra(EXTRA_TITLE, currentTitle);
        intent.putExtra(EXTRA_PROGRESS, progress);
        intent.putExtra(EXTRA_STATUS, status);
        sendBroadcast(intent);
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    getString(R.string.download_channel_name),
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
        handler.removeCallbacks(progressRunnable);
        super.onDestroy();
    }

    private void markContentDownloaded() {
        if (currentBookId <= 0) {
            return;
        }

        new Thread(() -> AppDatabase
                .getInstance(getApplicationContext())
                .fonosDao()
                .upsertDownloadedContent(new DownloadedContent(currentBookId, System.currentTimeMillis()))
        ).start();
    }
}
