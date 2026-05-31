package hcmute.com.fonosclone;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

import hcmute.com.fonosclone.data.AppDatabase;
import hcmute.com.fonosclone.data.Book;
import hcmute.com.fonosclone.data.FonosDao;
import hcmute.com.fonosclone.data.PodCourse;

public final class FonosRepository {

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
     * Đồng bộ danh sách Sách từ Firestore đám mây về Room SQLite cục bộ
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
                            Boolean isFavoriteObj = doc.getBoolean("isFavorite");
                            boolean isFavorite = isFavoriteObj != null ? isFavoriteObj : false;
                            String category = doc.getString("category");

                            if (title != null && type != null) {
                                Book book = new Book(title, author, type, coverImage, audioResName, isFavorite, category);
                                if (id > 0) {
                                    book.id = id;
                                }
                                books.add(book);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    // Lưu danh sách sách vào Room ở Thread phụ
                    new Thread(() -> {
                        try {
                            if (!books.isEmpty()) {
                                dao.deleteAllBooks();
                                dao.insertBooks(books);
                            }
                            // Báo thành công về UI Thread
                            mainHandler.post(callback::onSuccess);
                        } catch (Exception e) {
                            mainHandler.post(() -> callback.onFailure(e));
                        }
                    }).start();
                })
                .addOnFailureListener(e -> callback.onFailure(e));
    }

    /**
     * Đồng bộ danh sách Khóa học PodCourse từ Firestore đám mây về Room SQLite cục bộ
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

                    // Lưu danh sách khóa học vào Room ở Thread phụ
                    new Thread(() -> {
                        try {
                            if (!courses.isEmpty()) {
                                dao.deleteAllPodCourses();
                                dao.insertPodCourses(courses);
                            }
                            // Báo thành công về UI Thread
                            mainHandler.post(callback::onSuccess);
                        } catch (Exception e) {
                            mainHandler.post(() -> callback.onFailure(e));
                        }
                    }).start();
                })
                .addOnFailureListener(e -> callback.onFailure(e));
    }
}
