package hcmute.com.fonosclone.worker;


import hcmute.com.fonosclone.data.local.AppDatabase;
import hcmute.com.fonosclone.data.local.FonosDao;
import hcmute.com.fonosclone.data.model.Book;
import hcmute.com.fonosclone.data.model.ChallengeCompletion;
import hcmute.com.fonosclone.data.model.DownloadedContent;
import hcmute.com.fonosclone.data.model.ListeningHistory;
import hcmute.com.fonosclone.data.model.ListeningProgress;
import hcmute.com.fonosclone.data.model.UserPoints;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class FirebaseSyncWorker extends Worker {
    public static final String UNIQUE_WORK_NAME = "firebase_user_sync";

    public FirebaseSyncWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            return Result.success();
        }

        try {
            FonosDao dao = AppDatabase.getInstance(getApplicationContext()).fonosDao();
            FirebaseFirestore firestore = FirebaseFirestore.getInstance();
            String uid = user.getUid();

            WriteBatch batch = firestore.batch();
            int writeCount = 0;

            writeCount += addFavoriteWrites(batch, firestore, uid, dao.getFavoriteBooks(uid));
            writeCount += addListeningProgressWrites(batch, firestore, uid, dao);
            writeCount += addDownloadWrites(batch, firestore, uid, dao);
            writeCount += addListeningHistoryWrites(batch, firestore, uid, dao);
            writeCount += addChallengeWrites(batch, firestore, uid, dao);

            if (writeCount > 0) {
                Tasks.await(batch.commit());
            }
            return Result.success();
        } catch (Exception e) {
            e.printStackTrace();
            return Result.retry();
        }
    }

    private int addFavoriteWrites(WriteBatch batch, FirebaseFirestore firestore, String uid, List<Book> favorites) {
        int writes = 0;
        for (Book book : favorites) {
            DocumentReference ref = firestore.collection("users")
                    .document(uid)
                    .collection("favorites")
                    .document("book_" + book.id);

            Map<String, Object> data = new HashMap<>();
            data.put("bookId", book.id);
            data.put("title", book.title);
            data.put("author", book.author);
            data.put("type", book.type);
            data.put("coverImage", book.coverImage);
            data.put("audioResName", book.audioResName);
            putIfNotBlank(data, "audioUrl", book.audioUrl);
            putIfNotBlank(data, "audioStoragePath", book.audioStoragePath);
            data.put("updatedAt", FieldValue.serverTimestamp());
            batch.set(ref, data);
            writes++;
        }
        return writes;
    }

    private int addListeningProgressWrites(WriteBatch batch, FirebaseFirestore firestore, String uid, FonosDao dao) {
        int writes = 0;
        List<ListeningProgress> progressList = dao.getAllListeningProgress(uid);
        for (ListeningProgress progress : progressList) {
            Book book = dao.getBookById(progress.bookId);
            DocumentReference ref = firestore.collection("users")
                    .document(uid)
                    .collection("listening_progress")
                    .document("book_" + progress.bookId);

            Map<String, Object> data = new HashMap<>();
            data.put("bookId", progress.bookId);
            data.put("title", book != null ? book.title : "");
            data.put("author", book != null ? book.author : "");
            data.put("coverImage", book != null ? book.coverImage : "");
            data.put("positionMs", progress.positionMs);
            data.put("durationMs", progress.durationMs);
            data.put("updatedAtMillis", progress.updatedAt);
            data.put("updatedAt", FieldValue.serverTimestamp());
            batch.set(ref, data);
            writes++;
        }
        return writes;
    }

    private int addDownloadWrites(WriteBatch batch, FirebaseFirestore firestore, String uid, FonosDao dao) {
        int writes = 0;
        List<DownloadedContent> downloads = dao.getDownloadedContents(uid);
        for (DownloadedContent download : downloads) {
            Book book = dao.getBookById(download.bookId);
            DocumentReference ref = firestore.collection("users")
                    .document(uid)
                    .collection("downloads")
                    .document("book_" + download.bookId);

            Map<String, Object> data = new HashMap<>();
            data.put("bookId", download.bookId);
            data.put("title", book != null ? book.title : "");
            data.put("author", book != null ? book.author : "");
            data.put("coverImage", book != null ? book.coverImage : "");
            data.put("audioResName", book != null ? book.audioResName : "");
            putIfNotBlank(data, "audioUrl", book != null ? book.audioUrl : "");
            putIfNotBlank(data, "audioStoragePath", book != null ? book.audioStoragePath : "");
            putIfNotBlank(data, "remoteAudioSource", download.remoteAudioSource);
            data.put("hasLocalFile", download.localAudioPath != null && !download.localAudioPath.trim().isEmpty());
            data.put("downloadedAt", download.downloadedAt);
            data.put("updatedAt", FieldValue.serverTimestamp());
            batch.set(ref, data);
            writes++;
        }
        return writes;
    }

    private int addListeningHistoryWrites(WriteBatch batch, FirebaseFirestore firestore, String uid, FonosDao dao) {
        int writes = 0;
        List<ListeningHistory> historyList = dao.getListeningHistoryForUser(uid);
        for (ListeningHistory history : historyList) {
            Book book = dao.getBookById(history.bookId);
            DocumentReference ref = firestore.collection("users")
                    .document(uid)
                    .collection("listening_history")
                    .document("history_" + history.listenedAt + "_" + history.bookId);

            Map<String, Object> data = new HashMap<>();
            data.put("bookId", history.bookId);
            data.put("title", book != null ? book.title : "");
            data.put("author", book != null ? book.author : "");
            data.put("listenedSeconds", history.listenedSeconds);
            data.put("listenedAt", history.listenedAt);
            data.put("updatedAt", FieldValue.serverTimestamp());
            batch.set(ref, data);
            writes++;
        }
        return writes;
    }

    private int addChallengeWrites(WriteBatch batch, FirebaseFirestore firestore, String uid, FonosDao dao) {
        int writes = 0;
        for (ChallengeCompletion completion : dao.getChallengeCompletions(uid)) {
            DocumentReference ref = firestore.collection("users")
                    .document(uid)
                    .collection("challenge_progress")
                    .document(completion.missionId + "_" + completion.periodKey);

            Map<String, Object> data = new HashMap<>();
            data.put("missionId", completion.missionId);
            data.put("periodKey", completion.periodKey);
            data.put("pointsAwarded", completion.pointsAwarded);
            data.put("completedAt", completion.completedAt);
            data.put("updatedAt", FieldValue.serverTimestamp());
            batch.set(ref, data);
            writes++;
        }

        UserPoints points = dao.getUserPoints(uid);
        if (points != null) {
            DocumentReference ref = firestore.collection("users")
                    .document(uid)
                    .collection("challenge_progress")
                    .document("points_summary");

            Map<String, Object> data = new HashMap<>();
            data.put("totalPoints", points.totalPoints);
            data.put("updatedAt", FieldValue.serverTimestamp());
            batch.set(ref, data);
            writes++;
        }

        return writes;
    }

    private static void putIfNotBlank(Map<String, Object> data, String key, String value) {
        if (value != null && !value.trim().isEmpty()) {
            data.put(key, value);
        }
    }
}
