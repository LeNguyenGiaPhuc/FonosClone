package hcmute.com.fonosclone.service;


import hcmute.com.fonosclone.auth.UserIdentity;
import hcmute.com.fonosclone.data.local.AppDatabase;
import hcmute.com.fonosclone.data.local.FonosDao;
import hcmute.com.fonosclone.data.model.DownloadedContent;
import hcmute.com.fonosclone.R;
import hcmute.com.fonosclone.ui.activity.MainActivity;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.google.android.gms.tasks.Tasks;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.Map;


public class ContentDownloadService extends Service {

    public static final String ACTION_START_DOWNLOAD = "hcmute.com.fonosclone.action.START_DOWNLOAD";
    public static final String ACTION_CANCEL_DOWNLOAD = "hcmute.com.fonosclone.action.CANCEL_DOWNLOAD";
    public static final String ACTION_DOWNLOAD_PROGRESS = "hcmute.com.fonosclone.action.DOWNLOAD_PROGRESS";

    public static final String EXTRA_CONTENT_ID = "extra_content_id";
    public static final String EXTRA_BOOK_ID = "extra_book_id";
    public static final String EXTRA_TITLE = "extra_title";
    public static final String EXTRA_AUDIO_RES = "extra_audio_res";
    public static final String EXTRA_AUDIO_URL = "extra_audio_url";
    public static final String EXTRA_AUDIO_STORAGE_PATH = "extra_audio_storage_path";
    public static final String EXTRA_LOCAL_AUDIO_PATH = "extra_local_audio_path";
    public static final String EXTRA_REMOTE_AUDIO_SOURCE = "extra_remote_audio_source";
    public static final String EXTRA_PROGRESS = "extra_progress";
    public static final String EXTRA_STATUS = "extra_status";
    public static final String EXTRA_ERROR_MESSAGE = "extra_error_message";

    public static final String STATUS_RUNNING = "running";
    public static final String STATUS_COMPLETED = "completed";
    public static final String STATUS_CANCELLED = "cancelled";
    public static final String STATUS_FAILED = "failed";

    private static final String TAG = "ContentDownloadService";
    private static final String CHANNEL_ID = "content_download_queue";
    private static final int NOTIFICATION_ID = 68;
    private static final int PROGRESS_MAX = 100;
    private static final int HTTP_TIMEOUT_MS = 15000;
    private static final String STATUS_QUEUED = "queued";

    private final Object lock = new Object();
    private final LinkedHashMap<Integer, DownloadItem> downloads = new LinkedHashMap<>();
    private volatile boolean isCancelled;
    private boolean workerRunning;

    public static void startDownload(Context context, String contentId, String title) {
        Intent intent = new Intent(context, ContentDownloadService.class);
        intent.setAction(ACTION_START_DOWNLOAD);
        intent.putExtra(EXTRA_CONTENT_ID, contentId);
        intent.putExtra(EXTRA_TITLE, title);
        start(context, intent);
    }

    public static void startDownload(Context context, int bookId, String title) {
        startDownload(context, bookId, title, "", "", "");
    }

    public static void startDownload(
            Context context,
            int bookId,
            String title,
            String audioResName,
            String audioUrl,
            String audioStoragePath
    ) {
        Intent intent = new Intent(context, ContentDownloadService.class);
        intent.setAction(ACTION_START_DOWNLOAD);
        intent.putExtra(EXTRA_BOOK_ID, bookId);
        intent.putExtra(EXTRA_CONTENT_ID, "book_" + bookId);
        intent.putExtra(EXTRA_TITLE, title);
        intent.putExtra(EXTRA_AUDIO_RES, audioResName);
        intent.putExtra(EXTRA_AUDIO_URL, audioUrl);
        intent.putExtra(EXTRA_AUDIO_STORAGE_PATH, audioStoragePath);
        start(context, intent);
    }

    public static void cancelDownload(Context context) {
        Intent intent = new Intent(context, ContentDownloadService.class);
        intent.setAction(ACTION_CANCEL_DOWNLOAD);
        context.startService(intent);
    }

    private static void start(Context context, Intent intent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent);
        } else {
            context.startService(intent);
        }
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
            enqueueDownload(intent);
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

    private void enqueueDownload(Intent intent) {
        DownloadItem item = DownloadItem.fromIntent(intent, getString(R.string.download_default_title));
        if (item.bookId <= 0) {
            item.bookId = item.contentId.hashCode();
        }

        synchronized (lock) {
            DownloadItem existing = downloads.get(item.bookId);
            if (existing != null
                    && !STATUS_COMPLETED.equals(existing.status)
                    && !STATUS_FAILED.equals(existing.status)
                    && !STATUS_CANCELLED.equals(existing.status)) {
                sendProgressBroadcast(existing);
                updateNotification(false);
                return;
            }

            downloads.put(item.bookId, item);
            isCancelled = false;
            sendProgressBroadcast(item);
            startForeground(NOTIFICATION_ID, buildNotification(false));

            if (!workerRunning) {
                workerRunning = true;
                new Thread(this::processQueue).start();
            }
        }
    }

    private void processQueue() {
        while (!isCancelled) {
            DownloadItem item = nextQueuedItem();
            if (item == null) {
                break;
            }

            try {
                item.status = STATUS_RUNNING;
                item.progress = 0;
                sendProgressBroadcast(item);
                updateNotification(false);

                if (!isNetworkAvailable()) {
                    throw new IllegalStateException(getString(R.string.download_no_network));
                }

                String remoteSource = firstNonBlank(item.audioUrl, item.audioStoragePath);
                if (isBlank(remoteSource)) {
                    copyLocalRawAudioToDownloadFile(item, isBlank(item.audioResName) ? "demo_audio" : item.audioResName);
                } else if (remoteSource.startsWith("http://") || remoteSource.startsWith("https://")) {
                    downloadFromHttpUrl(item, remoteSource);
                } else {
                    downloadFromFirebaseStorage(item, remoteSource);
                }

                if (isCancelled) {
                    markCancelled(item);
                    break;
                }

                completeDownload(item);
            } catch (Exception e) {
                failDownload(item, e);
            }
        }

        boolean cancelled = isCancelled;
        synchronized (lock) {
            if (!cancelled) {
                for (DownloadItem item : downloads.values()) {
                    if (STATUS_QUEUED.equals(item.status)) {
                        new Thread(this::processQueue).start();
                        return;
                    }
                }
            }
            workerRunning = false;
        }

        Notification finalNotification = buildNotification(true);
        if (cancelled) {
            stopForeground(true);
        } else {
            detachForegroundNotification();
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.notify(NOTIFICATION_ID, finalNotification);
            }
        }
        stopSelf();
    }

    private DownloadItem nextQueuedItem() {
        synchronized (lock) {
            for (DownloadItem item : downloads.values()) {
                if (STATUS_QUEUED.equals(item.status)) {
                    return item;
                }
            }
        }
        return null;
    }

    private void copyLocalRawAudioToDownloadFile(DownloadItem item, String audioResName) throws Exception {
        File destination = getDownloadFile(item.bookId);
        String resolvedAudioResName = audioResName;
        int audioResId = getResources().getIdentifier(resolvedAudioResName, "raw", getPackageName());
        if (audioResId == 0) {
            audioResId = R.raw.demo_audio;
            resolvedAudioResName = "demo_audio";
        }

        publishProgress(item, 10);
        try (InputStream input = getResources().openRawResource(audioResId);
             FileOutputStream output = new FileOutputStream(destination)) {
            byte[] buffer = new byte[8192];
            int totalBytes = Math.max(1, input.available());
            int downloadedBytes = 0;
            int read;
            while (!isCancelled && (read = input.read(buffer)) != -1) {
                output.write(buffer, 0, read);
                downloadedBytes += read;
                int localProgress = 10 + (int) Math.min(80, (downloadedBytes * 80L) / totalBytes);
                publishProgress(item, localProgress);
                Thread.sleep(15);
            }
        }

        if (isCancelled) {
            destination.delete();
            return;
        }
        if (!destination.exists() || destination.length() == 0) {
            throw new IllegalStateException("Copied audio file is empty");
        }

        item.localAudioPath = destination.getAbsolutePath();
        item.remoteAudioSource = resolvedAudioResName;
        publishProgress(item, 90);
    }

    private void downloadFromFirebaseStorage(DownloadItem item, String storagePath) throws Exception {
        StorageReference reference = storagePath.startsWith("gs://")
                ? FirebaseStorage.getInstance().getReferenceFromUrl(storagePath)
                : FirebaseStorage.getInstance().getReference().child(storagePath);
        File destination = getDownloadFile(item.bookId);
        Tasks.await(reference.getFile(destination)
                .addOnProgressListener(snapshot -> {
                    long total = snapshot.getTotalByteCount();
                    if (total > 0) {
                        int nextProgress = (int) Math.min(PROGRESS_MAX, (snapshot.getBytesTransferred() * 100) / total);
                        publishProgress(item, nextProgress);
                    }
                }));
        if (!destination.exists() || destination.length() == 0) {
            throw new IllegalStateException("Downloaded file is empty");
        }
        item.localAudioPath = destination.getAbsolutePath();
        item.remoteAudioSource = storagePath;
    }

    private void downloadFromHttpUrl(DownloadItem item, String audioUrl) throws Exception {
        File destination = getDownloadFile(item.bookId);
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) new URL(audioUrl).openConnection();
            connection.setConnectTimeout(HTTP_TIMEOUT_MS);
            connection.setReadTimeout(HTTP_TIMEOUT_MS);
            connection.connect();
            int responseCode = connection.getResponseCode();
            if (responseCode < HttpURLConnection.HTTP_OK || responseCode >= HttpURLConnection.HTTP_MULT_CHOICE) {
                throw new IllegalStateException("HTTP " + responseCode);
            }
            int length = connection.getContentLength();
            try (InputStream input = connection.getInputStream();
                 FileOutputStream output = new FileOutputStream(destination)) {
                byte[] buffer = new byte[8192];
                long downloaded = 0;
                int read;
                while (!isCancelled && (read = input.read(buffer)) != -1) {
                    output.write(buffer, 0, read);
                    downloaded += read;
                    if (length > 0) {
                        publishProgress(item, (int) Math.min(PROGRESS_MAX, (downloaded * 100) / length));
                    }
                }
            }
            if (isCancelled) {
                destination.delete();
                return;
            }
            if (!destination.exists() || destination.length() == 0) {
                throw new IllegalStateException("Downloaded file is empty");
            }
            item.localAudioPath = destination.getAbsolutePath();
            item.remoteAudioSource = audioUrl;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private void publishProgress(DownloadItem item, int nextProgress) {
        item.progress = Math.max(item.progress, Math.min(PROGRESS_MAX, nextProgress));
        sendProgressBroadcast(item);
        updateNotification(false);
    }

    private File getDownloadFile(int bookId) {
        File directory = new File(getFilesDir(), "downloads");
        if (!directory.exists()) {
            directory.mkdirs();
        }
        return new File(directory, "book_" + bookId + ".mp3");
    }

    private void completeDownload(DownloadItem item) {
        item.progress = PROGRESS_MAX;
        item.status = STATUS_COMPLETED;
        markContentDownloaded(item);
        sendProgressBroadcast(item);
        updateNotification(false);
    }

    private void failDownload(DownloadItem item, Exception e) {
        Log.e(TAG, "Download failed", e);
        item.errorMessage = isBlank(e.getMessage()) ? getString(R.string.download_failed) : e.getMessage();
        item.status = STATUS_FAILED;
        item.progress = PROGRESS_MAX;
        sendProgressBroadcast(item);
        updateNotification(false);
    }

    private void markCancelled(DownloadItem item) {
        item.status = STATUS_CANCELLED;
        item.errorMessage = getString(R.string.cancel);
        sendProgressBroadcast(item);
    }

    private void cancelDownloadFlow() {
        isCancelled = true;
        synchronized (lock) {
            for (DownloadItem item : downloads.values()) {
                if (!STATUS_COMPLETED.equals(item.status) && !STATUS_FAILED.equals(item.status)) {
                    item.status = STATUS_CANCELLED;
                    item.errorMessage = getString(R.string.cancel);
                    sendProgressBroadcast(item);
                }
            }
        }
        stopForeground(true);
        stopSelf();
    }

    private void updateNotification(boolean finished) {
        NotificationManager manager = getSystemService(NotificationManager.class);
        if (manager != null) {
            manager.notify(NOTIFICATION_ID, buildNotification(finished));
        }
    }

    private Notification buildNotification(boolean finished) {
        Intent openAppIntent = new Intent(this, MainActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(
                this,
                0,
                openAppIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        int total = Math.max(1, downloads.size());
        int completed = countStatus(STATUS_COMPLETED);
        int failed = countStatus(STATUS_FAILED);
        int cancelled = countStatus(STATUS_CANCELLED);
        int finishedCount = completed + failed + cancelled;
        int aggregateProgress = calculateAggregateProgress();
        DownloadItem activeItem = findActiveItem();

        String title = finished
                ? getString(R.string.download_completed)
                : getString(R.string.download_in_progress) + " " + finishedCount + "/" + total;
        String text = activeItem != null
                ? activeItem.title
                : getString(R.string.download_queue_summary, completed, failed);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.stat_sys_download)
                .setContentTitle(title)
                .setContentText(text)
                .setContentIntent(contentIntent)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setOnlyAlertOnce(false)
                .setOngoing(!finished)
                .setAutoCancel(false)
                .setProgress(PROGRESS_MAX, aggregateProgress, false);

        NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle()
                .setBigContentTitle(title)
                .setSummaryText(getString(R.string.download_queue_progress, finishedCount, total));
        for (DownloadItem item : downloads.values()) {
            inboxStyle.addLine(formatNotificationLine(item));
        }
        builder.setStyle(inboxStyle);

        if (!finished) {
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

    private String formatNotificationLine(DownloadItem item) {
        if (STATUS_COMPLETED.equals(item.status)) {
            return item.title + " - " + getString(R.string.download_status_done);
        }
        if (STATUS_FAILED.equals(item.status)) {
            return item.title + " - " + getString(R.string.download_status_failed);
        }
        if (STATUS_CANCELLED.equals(item.status)) {
            return item.title + " - " + getString(R.string.cancel);
        }
        if (STATUS_QUEUED.equals(item.status)) {
            return item.title + " - " + getString(R.string.download_status_waiting);
        }
        return item.title + " - " + item.progress + "%";
    }

    private int calculateAggregateProgress() {
        synchronized (lock) {
            if (downloads.isEmpty()) {
                return 0;
            }
            int totalProgress = 0;
            for (DownloadItem item : downloads.values()) {
                totalProgress += Math.max(0, Math.min(PROGRESS_MAX, item.progress));
            }
            return totalProgress / downloads.size();
        }
    }

    private int countStatus(String status) {
        synchronized (lock) {
            int count = 0;
            for (DownloadItem item : downloads.values()) {
                if (status.equals(item.status)) {
                    count++;
                }
            }
            return count;
        }
    }

    private DownloadItem findActiveItem() {
        synchronized (lock) {
            for (DownloadItem item : downloads.values()) {
                if (STATUS_RUNNING.equals(item.status)) {
                    return item;
                }
            }
        }
        return null;
    }

    private void sendProgressBroadcast(DownloadItem item) {
        Intent intent = new Intent(ACTION_DOWNLOAD_PROGRESS);
        intent.setPackage(getPackageName());
        intent.putExtra(EXTRA_CONTENT_ID, item.contentId);
        intent.putExtra(EXTRA_BOOK_ID, item.bookId);
        intent.putExtra(EXTRA_TITLE, item.title);
        intent.putExtra(EXTRA_PROGRESS, item.progress);
        intent.putExtra(EXTRA_STATUS, STATUS_QUEUED.equals(item.status) ? STATUS_RUNNING : item.status);
        intent.putExtra(EXTRA_LOCAL_AUDIO_PATH, item.localAudioPath);
        intent.putExtra(EXTRA_REMOTE_AUDIO_SOURCE, item.remoteAudioSource);
        intent.putExtra(EXTRA_ERROR_MESSAGE, item.errorMessage);
        sendBroadcast(intent);
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    getString(R.string.download_channel_name),
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            channel.setDescription(getString(R.string.download_channel_name));
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    private void detachForegroundNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            stopForeground(STOP_FOREGROUND_DETACH);
        } else {
            stopForeground(false);
        }
    }

    private void markContentDownloaded(DownloadItem item) {
        if (item.bookId <= 0) {
            return;
        }

        AppDatabase
                .getInstance(getApplicationContext())
                .fonosDao()
                .upsertDownloadedContent(new DownloadedContent(
                        UserIdentity.getCurrentUserId(getApplicationContext()),
                        item.bookId,
                        System.currentTimeMillis(),
                        item.localAudioPath,
                        item.remoteAudioSource
                ));
    }

    private static String firstNonBlank(String first, String second) {
        return !isBlank(first) ? first : (!isBlank(second) ? second : "");
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
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

    private static final class DownloadItem {
        int bookId;
        String contentId;
        String title;
        String audioResName;
        String audioUrl;
        String audioStoragePath;
        String localAudioPath = "";
        String remoteAudioSource = "";
        String errorMessage = "";
        String status = STATUS_QUEUED;
        int progress;

        static DownloadItem fromIntent(Intent intent, String fallbackTitle) {
            DownloadItem item = new DownloadItem();
            item.bookId = intent.getIntExtra(EXTRA_BOOK_ID, 0);
            item.contentId = valueOrFallback(intent.getStringExtra(EXTRA_CONTENT_ID), "book_" + item.bookId);
            item.title = valueOrFallback(intent.getStringExtra(EXTRA_TITLE), fallbackTitle);
            item.audioResName = valueOrFallback(intent.getStringExtra(EXTRA_AUDIO_RES), "");
            item.audioUrl = valueOrFallback(intent.getStringExtra(EXTRA_AUDIO_URL), "");
            item.audioStoragePath = valueOrFallback(intent.getStringExtra(EXTRA_AUDIO_STORAGE_PATH), "");
            return item;
        }

        private static String valueOrFallback(String value, String fallback) {
            return value == null || value.trim().isEmpty() ? fallback : value;
        }
    }
}
