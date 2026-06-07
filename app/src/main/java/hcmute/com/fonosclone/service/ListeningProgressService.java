package hcmute.com.fonosclone.service;

import android.content.Context;

import hcmute.com.fonosclone.auth.UserIdentity;
import hcmute.com.fonosclone.challenge.ChallengeEngine;
import hcmute.com.fonosclone.data.local.AppDatabase;
import hcmute.com.fonosclone.data.local.FonosDao;
import hcmute.com.fonosclone.data.model.ListeningHistory;
import hcmute.com.fonosclone.data.model.ListeningProgress;
import hcmute.com.fonosclone.data.repository.FonosRepository;
import hcmute.com.fonosclone.sync.SyncScheduler;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ListeningProgressService {
    private static final int PROGRESS_SAVE_INTERVAL_MS = 2_000;
    private static final int HISTORY_SAVE_INTERVAL_MS = 5_000;
    private static final long SYNC_INTERVAL_MS = 30_000L;

    private final Context appContext;
    private final FonosDao dao;
    private final FonosRepository repository;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private String userId = "guest";
    private int bookId;
    private String title = "";
    private String author = "";
    private String coverImage = "";
    private int lastProgressPositionMs = -1;
    private int lastHistoryAnchorMs = -1;
    private long lastSyncAtMillis;
    private boolean sessionActive;

    public ListeningProgressService(Context context) {
        appContext = context.getApplicationContext();
        dao = AppDatabase.getInstance(appContext).fonosDao();
        repository = new FonosRepository(appContext);
    }

    public synchronized void startSession(
            int bookId,
            String title,
            String author,
            String coverImage,
            int startPositionMs
    ) {
        this.userId = UserIdentity.getCurrentUserId(appContext);
        this.bookId = bookId;
        this.title = valueOrEmpty(title);
        this.author = valueOrEmpty(author);
        this.coverImage = valueOrEmpty(coverImage);
        this.lastProgressPositionMs = Math.max(0, startPositionMs);
        this.lastHistoryAnchorMs = Math.max(0, startPositionMs);
        this.sessionActive = bookId > 0;
    }

    public void recordPlaybackState(int positionMs, int durationMs, boolean isPlaying) {
        PersistRequest request;
        synchronized (this) {
            if (!isValidSession(durationMs)) {
                return;
            }

            int historyDeltaSeconds = isPlaying
                    ? takeHistoryDeltaSecondsLocked(positionMs, durationMs, false)
                    : 0;
            int savedPositionMs = normalizeProgressPosition(positionMs, durationMs);
            boolean shouldSaveProgress = !isPlaying
                    || lastProgressPositionMs < 0
                    || Math.abs(savedPositionMs - lastProgressPositionMs) >= PROGRESS_SAVE_INTERVAL_MS
                    || savedPositionMs == 0;

            if (!shouldSaveProgress && historyDeltaSeconds <= 0) {
                return;
            }

            if (shouldSaveProgress) {
                lastProgressPositionMs = savedPositionMs;
            }

            request = new PersistRequest(
                    userId,
                    bookId,
                    title,
                    author,
                    coverImage,
                    savedPositionMs,
                    Math.max(0, durationMs),
                    historyDeltaSeconds,
                    shouldSaveProgress,
                    shouldScheduleSyncLocked(historyDeltaSeconds > 0)
            );
        }

        persistAsync(request);
    }

    public void flushSession(int positionMs, int durationMs, boolean resetPosition) {
        PersistRequest request;
        synchronized (this) {
            if (!isValidSession(durationMs)) {
                return;
            }

            int historyDeltaSeconds = takeHistoryDeltaSecondsLocked(positionMs, durationMs, true);
            int savedPositionMs = resetPosition ? 0 : normalizeProgressPosition(positionMs, durationMs);
            lastProgressPositionMs = savedPositionMs;

            request = new PersistRequest(
                    userId,
                    bookId,
                    title,
                    author,
                    coverImage,
                    savedPositionMs,
                    Math.max(0, durationMs),
                    historyDeltaSeconds,
                    true,
                    true
            );

            if (resetPosition) {
                lastHistoryAnchorMs = 0;
            }
        }

        persistAsync(request);
    }

    public synchronized void clearSession() {
        sessionActive = false;
        bookId = 0;
        lastProgressPositionMs = -1;
        lastHistoryAnchorMs = -1;
    }

    public void shutdown() {
        executor.shutdown();
    }

    private boolean isValidSession(int durationMs) {
        return sessionActive && bookId > 0 && durationMs > 0;
    }

    private int takeHistoryDeltaSecondsLocked(int positionMs, int durationMs, boolean force) {
        int safePositionMs = normalizeHistoryPosition(positionMs, durationMs);
        if (lastHistoryAnchorMs < 0 || safePositionMs < lastHistoryAnchorMs) {
            lastHistoryAnchorMs = safePositionMs;
            return 0;
        }

        int deltaMs = safePositionMs - lastHistoryAnchorMs;
        if (!force && deltaMs < HISTORY_SAVE_INTERVAL_MS) {
            return 0;
        }

        int deltaSeconds = deltaMs / 1000;
        if (deltaSeconds <= 0) {
            return 0;
        }

        lastHistoryAnchorMs += deltaSeconds * 1000;
        return deltaSeconds;
    }

    private boolean shouldScheduleSyncLocked(boolean hasHistoryDelta) {
        if (!hasHistoryDelta) {
            return false;
        }

        long now = System.currentTimeMillis();
        if (now - lastSyncAtMillis < SYNC_INTERVAL_MS) {
            return false;
        }

        lastSyncAtMillis = now;
        return true;
    }

    private void persistAsync(PersistRequest request) {
        executor.execute(() -> {
            long now = System.currentTimeMillis();

            if (request.historyDeltaSeconds > 0) {
                dao.insertListeningHistory(new ListeningHistory(
                        request.userId,
                        request.bookId,
                        request.historyDeltaSeconds,
                        now
                ));
                ChallengeEngine.evaluate(dao, request.userId);
            }

            if (request.saveProgress) {
                dao.upsertListeningProgress(new ListeningProgress(
                        request.userId,
                        request.bookId,
                        request.positionMs,
                        request.durationMs,
                        now
                ));
            }

            if (request.syncNow) {
                repository.saveListeningProgressForCurrentUser(
                        request.bookId,
                        request.title,
                        request.author,
                        request.coverImage,
                        request.positionMs,
                        request.durationMs
                );
                SyncScheduler.enqueueUserSync(appContext);
            }
        });
    }

    private int normalizeProgressPosition(int positionMs, int durationMs) {
        int safePositionMs = Math.max(0, positionMs);
        int safeDurationMs = Math.max(0, durationMs);
        if (safeDurationMs > 0 && safePositionMs >= safeDurationMs - 1000) {
            return 0;
        }
        return safePositionMs;
    }

    private int normalizeHistoryPosition(int positionMs, int durationMs) {
        int safePositionMs = Math.max(0, positionMs);
        int safeDurationMs = Math.max(0, durationMs);
        if (safeDurationMs > 0 && safePositionMs >= safeDurationMs - 1000) {
            return safeDurationMs;
        }
        return safePositionMs;
    }

    private static String valueOrEmpty(String value) {
        return value == null ? "" : value;
    }

    private static final class PersistRequest {
        final String userId;
        final int bookId;
        final String title;
        final String author;
        final String coverImage;
        final int positionMs;
        final int durationMs;
        final int historyDeltaSeconds;
        final boolean saveProgress;
        final boolean syncNow;

        PersistRequest(
                String userId,
                int bookId,
                String title,
                String author,
                String coverImage,
                int positionMs,
                int durationMs,
                int historyDeltaSeconds,
                boolean saveProgress,
                boolean syncNow
        ) {
            this.userId = userId;
            this.bookId = bookId;
            this.title = title;
            this.author = author;
            this.coverImage = coverImage;
            this.positionMs = positionMs;
            this.durationMs = durationMs;
            this.historyDeltaSeconds = historyDeltaSeconds;
            this.saveProgress = saveProgress;
            this.syncNow = syncNow;
        }
    }
}
