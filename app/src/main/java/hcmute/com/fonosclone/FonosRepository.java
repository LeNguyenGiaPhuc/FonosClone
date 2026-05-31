package hcmute.com.fonosclone;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import hcmute.com.fonosclone.data.AppDatabase;
import hcmute.com.fonosclone.data.Book;
import hcmute.com.fonosclone.data.FonosDao;
import hcmute.com.fonosclone.data.ListeningProgress;
import hcmute.com.fonosclone.data.PodCourse;

public final class FonosRepository {
    private static final String TAG = "FonosRepository";

    private final FonosDao dao;
    private final FirebaseFirestore firestore;
    private final Handler mainHandler;

    public interface SyncCallback {
        void onSuccess();
        void onFailure(Exception e);
    }

    public FonosRepository(Context context) {
        this.dao = AppDatabase.getInstance(context).fonosDao();
        this.firestore = FirebaseFirestore.getInstance();
        this.mainHandler = new Handler(Looper.getMainLooper());
    }

    /**
     * Äá»“ng bá»™ danh sÃ¡ch SÃ¡ch tá»« Firestore Ä‘Ã¡m mÃ¢y vá» Room SQLite cá»¥c bá»™
     */
    public void syncBooks(final SyncCallback callback) {
        firestore.collection("books")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    final List<Book> books = new ArrayList<>();
                    for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                        try {
                            Long idLong = doc.getLong("id");
                            int id = idLong != null ? idLong.intValue() : 0;
                            String title = doc.getString("title");
                            String author = doc.getString("author");
                            String type = doc.getString("type");
                            String coverImage = doc.getString("coverImage");
                            String audioResName = doc.getString("audioResName");
                            String audioUrl = doc.getString("audioUrl");
                            String audioStoragePath = doc.getString("audioStoragePath");
                            Boolean isFavoriteObj = doc.getBoolean("isFavorite");
                            boolean isFavorite = isFavoriteObj != null ? isFavoriteObj : false;
                            String category = doc.getString("category");

                            if (title != null && type != null) {
                                Book book = new Book(title, author, type, coverImage, audioResName, isFavorite, category, audioUrl, audioStoragePath);
                                if (id > 0) {
                                    book.id = id;
                                }
                                books.add(book);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    // LÆ°u danh sÃ¡ch sÃ¡ch vÃ o Room á»Ÿ Thread phá»¥
                    new Thread(() -> {
                        try {
                            if (!books.isEmpty()) {
                                dao.deleteAllBooks();
                                dao.insertBooks(books);
                            }
                            // BÃ¡o thÃ nh cÃ´ng vá» UI Thread
                            mainHandler.post(callback::onSuccess);
                        } catch (Exception e) {
                            mainHandler.post(() -> callback.onFailure(e));
                        }
                    }).start();
                })
                .addOnFailureListener(e -> callback.onFailure(e));
    }

    /**
     * Äá»“ng bá»™ danh sÃ¡ch KhÃ³a há»c PodCourse tá»« Firestore Ä‘Ã¡m mÃ¢y vá» Room SQLite cá»¥c bá»™
     */
    public void syncPodCourses(final SyncCallback callback) {
        firestore.collection("pod_courses")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    final List<PodCourse> courses = new ArrayList<>();
                    for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                        try {
                            Long idLong = doc.getLong("id");
                            int id = idLong != null ? idLong.intValue() : 0;
                            String title = doc.getString("title");
                            String teacher = doc.getString("teacher");
                            String category = doc.getString("category");
                            String coverColor = doc.getString("coverColor");
                            Double ratingObj = doc.getDouble("rating");
                            double rating = ratingObj != null ? ratingObj : 4.5;

                            if (title != null && teacher != null) {
                                PodCourse course = new PodCourse(title, teacher, category, coverColor, rating);
                                if (id > 0) {
                                    course.id = id;
                                }
                                courses.add(course);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    // LÆ°u danh sÃ¡ch khÃ³a há»c vÃ o Room á»Ÿ Thread phá»¥
                    new Thread(() -> {
                        try {
                            if (!courses.isEmpty()) {
                                dao.deleteAllPodCourses();
                                dao.insertPodCourses(courses);
                            }
                            // BÃ¡o thÃ nh cÃ´ng vá» UI Thread
                            mainHandler.post(callback::onSuccess);
                        } catch (Exception e) {
                            mainHandler.post(() -> callback.onFailure(e));
                        }
                    }).start();
                })
                .addOnFailureListener(e -> callback.onFailure(e));
    }

    public void syncFavoritesForCurrentUser(final SyncCallback callback) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            mainHandler.post(callback::onSuccess);
            return;
        }

        firestore.collection("users")
                .document(user.getUid())
                .collection("favorites")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    final List<Integer> favoriteBookIds = new ArrayList<>();
                    for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                        Long bookIdLong = doc.getLong("bookId");
                        if (bookIdLong != null) {
                            favoriteBookIds.add(bookIdLong.intValue());
                            continue;
                        }

                        try {
                            String documentId = doc.getId().replace("book_", "");
                            favoriteBookIds.add(Integer.parseInt(documentId));
                        } catch (NumberFormatException ignored) {
                        }
                    }

                    new Thread(() -> {
                        try {
                            dao.clearBookFavorites();
                            for (Integer bookId : favoriteBookIds) {
                                dao.setFavorite(bookId, true);
                            }
                            mainHandler.post(callback::onSuccess);
                        } catch (Exception e) {
                            mainHandler.post(() -> callback.onFailure(e));
                        }
                    }).start();
                })
                .addOnFailureListener(e -> callback.onFailure(e));
    }

    public void setFavoriteForCurrentUser(Book book, boolean isFavorite) {
        setFavoriteForCurrentUser(book, isFavorite, null);
    }

    public void setFavoriteForCurrentUser(Book book, boolean isFavorite, final SyncCallback callback) {
        new Thread(() -> {
            try {
                dao.setFavorite(book.id, isFavorite);
                if (callback != null) {
                    mainHandler.post(callback::onSuccess);
                }
            } catch (Exception e) {
                if (callback != null) {
                    mainHandler.post(() -> callback.onFailure(e));
                }
            }
        }).start();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            return;
        }

        if (isFavorite) {
            Map<String, Object> favoriteData = new HashMap<>();
            favoriteData.put("bookId", book.id);
            favoriteData.put("title", book.title);
            favoriteData.put("author", book.author);
            favoriteData.put("type", book.type);
            favoriteData.put("coverImage", book.coverImage);
            favoriteData.put("audioResName", book.audioResName);
            putIfNotBlank(favoriteData, "audioUrl", book.audioUrl);
            putIfNotBlank(favoriteData, "audioStoragePath", book.audioStoragePath);
            favoriteData.put("updatedAt", FieldValue.serverTimestamp());

            firestore.collection("users")
                    .document(user.getUid())
                    .collection("favorites")
                    .document("book_" + book.id)
                    .set(favoriteData)
                    .addOnFailureListener(e -> Log.e(TAG, "Failed to save favorite to Firestore", e));
        } else {
            firestore.collection("users")
                    .document(user.getUid())
                    .collection("favorites")
                    .document("book_" + book.id)
                    .delete()
                    .addOnFailureListener(e -> Log.e(TAG, "Failed to delete favorite from Firestore", e));
        }
    }

    public void syncListeningProgressForCurrentUser(final SyncCallback callback) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            mainHandler.post(callback::onSuccess);
            return;
        }

        firestore.collection("users")
                .document(user.getUid())
                .collection("listening_progress")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    final List<ListeningProgress> cloudProgress = new ArrayList<>();
                    for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                        Long bookIdLong = doc.getLong("bookId");
                        Long positionMsLong = doc.getLong("positionMs");
                        Long durationMsLong = doc.getLong("durationMs");
                        Long updatedAtMillisLong = doc.getLong("updatedAtMillis");

                        if (bookIdLong == null) {
                            try {
                                String documentId = doc.getId().replace("book_", "");
                                bookIdLong = Long.parseLong(documentId);
                            } catch (NumberFormatException ignored) {
                            }
                        }

                        if (bookIdLong == null || positionMsLong == null || durationMsLong == null) {
                            continue;
                        }

                        long updatedAtMillis = updatedAtMillisLong != null
                                ? updatedAtMillisLong
                                : System.currentTimeMillis();
                        cloudProgress.add(new ListeningProgress(
                                bookIdLong.intValue(),
                                positionMsLong.intValue(),
                                durationMsLong.intValue(),
                                updatedAtMillis
                        ));
                    }

                    new Thread(() -> {
                        try {
                            for (ListeningProgress progress : cloudProgress) {
                                ListeningProgress localProgress = dao.getListeningProgress(progress.bookId);
                                if (localProgress == null || progress.updatedAt >= localProgress.updatedAt) {
                                    dao.upsertListeningProgress(progress);
                                }
                            }
                            mainHandler.post(callback::onSuccess);
                        } catch (Exception e) {
                            mainHandler.post(() -> callback.onFailure(e));
                        }
                    }).start();
                })
                .addOnFailureListener(e -> callback.onFailure(e));
    }

    public void saveListeningProgressForCurrentUser(
            int bookId,
            String title,
            String author,
            String coverImage,
            int positionMs,
            int durationMs
    ) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null || bookId <= 0 || durationMs <= 0) {
            return;
        }

        long updatedAtMillis = System.currentTimeMillis();
        Map<String, Object> progressData = new HashMap<>();
        progressData.put("bookId", bookId);
        progressData.put("title", title);
        progressData.put("author", author);
        progressData.put("coverImage", coverImage);
        progressData.put("positionMs", Math.max(0, positionMs));
        progressData.put("durationMs", Math.max(0, durationMs));
        progressData.put("updatedAtMillis", updatedAtMillis);
        progressData.put("updatedAt", FieldValue.serverTimestamp());

        firestore.collection("users")
                .document(user.getUid())
                .collection("listening_progress")
                .document("book_" + bookId)
                .set(progressData)
                .addOnFailureListener(e -> Log.e(TAG, "Failed to save listening progress to Firestore", e));
    }
    private static void putIfNotBlank(Map<String, Object> data, String key, String value) {
        if (value != null && !value.trim().isEmpty()) {
            data.put(key, value);
        }
    }
}
