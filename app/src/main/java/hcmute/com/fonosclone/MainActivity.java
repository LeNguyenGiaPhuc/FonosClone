package hcmute.com.fonosclone;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import hcmute.com.fonosclone.data.AppDatabase;
import hcmute.com.fonosclone.data.Book;
import hcmute.com.fonosclone.data.FonosDao;
import hcmute.com.fonosclone.data.ListeningProgress;
import hcmute.com.fonosclone.data.PodCourse;
import hcmute.com.fonosclone.data.SeedData;

public class MainActivity extends BaseActivity {
    private final List<Book> currentAudiobooks = new ArrayList<>();
    private Book continueBook;
    private int continuePositionMs;
    private AppDatabase appDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        applySystemBarPadding(R.id.main);
        setupBottomNavigation(NAV_BOOKS);
        setupTabs(TAB_AUDIOBOOKS);
        setupUserMenu();

        AppDatabase db = AppDatabase.getInstance(this);
        appDatabase = db;
        FonosRepository repository = new FonosRepository(this);

        // 1. Tự động kiểm tra và đồng bộ ngược dữ liệu lên Firestore có chẩn đoán lỗi bằng Toast
        seedFirestoreIfNecessary();

        // 2. Đọc nhanh từ Room cục bộ ngay lập tức để UI mượt mà (0ms lag)
        loadLocalBooks(db);

        // 3. Chạy ngầm đồng bộ từ Cloud Firestore về máy
        repository.syncBooks(new FonosRepository.SyncCallback() {
            @Override
            public void onSuccess() {
                // Đồng bộ xong -> Nạp lại giao diện với sách mới nhất từ mây
                repository.syncFavoritesForCurrentUser(new FonosRepository.SyncCallback() {
                    @Override
                    public void onSuccess() {
                        repository.syncListeningProgressForCurrentUser(new FonosRepository.SyncCallback() {
                            @Override
                            public void onSuccess() {
                                loadLocalBooks(db);
                            }

                            @Override
                            public void onFailure(Exception e) {
                                e.printStackTrace();
                                loadLocalBooks(db);
                            }
                        });
                    }

                    @Override
                    public void onFailure(Exception e) {
                        e.printStackTrace();
                        loadLocalBooks(db);
                    }
                });
            }

            @Override
            public void onFailure(Exception e) {
                // Thất bại (như mất mạng) -> Giữ nguyên dữ liệu cục bộ, không gây crash ứng dụng
                e.printStackTrace();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (appDatabase == null) return;

        View rootView = findViewById(R.id.main);
        if (rootView != null) {
            rootView.postDelayed(() -> loadLocalBooks(appDatabase), 250);
        } else {
            loadLocalBooks(appDatabase);
        }
    }

    private void loadLocalBooks(AppDatabase db) {
        new Thread(() -> {
            FonosDao dao = db.fonosDao();
            if (dao.countBooks() == 0) {
                SeedData.insertSampleData(dao);
            }

            List<Book> audiobooks = dao.getBooksByType("AUDIOBOOK");
            ListeningProgress latestProgress = dao.getLatestListeningProgress();
            Book latestBook = latestProgress != null ? dao.getBookById(latestProgress.bookId) : null;
            if (latestBook == null && !audiobooks.isEmpty()) {
                latestBook = audiobooks.get(0);
            }
            final Book finalLatestBook = latestBook;
            final ListeningProgress finalLatestProgress = latestProgress;

            runOnUiThread(() -> {
                currentAudiobooks.clear();
                currentAudiobooks.addAll(audiobooks);
                bindContinueListening(finalLatestBook, finalLatestProgress);

                if (audiobooks.size() > 0) {
                    bindBook(audiobooks.get(0), R.id.ivGridCover1, R.id.tvTitle1, R.id.tvAuthor1);
                }

                if (audiobooks.size() > 1) {
                    bindCover(audiobooks.get(1), R.id.ivCover2);
                    bindBook(audiobooks.get(1), R.id.ivGridCover2, R.id.tvTitle2, R.id.tvAuthor2);
                }

                if (audiobooks.size() > 2) {
                    bindCover(audiobooks.get(2), R.id.ivCover3);
                    bindBook(audiobooks.get(2), R.id.ivGridCover3, R.id.tvTitle3, R.id.tvAuthor3);
                }

                setupBookClick(R.id.ivGridCover1, 0);
                setupBookClick(R.id.ivCover2, 1);
                setupBookClick(R.id.ivGridCover2, 1);
                setupBookClick(R.id.ivCover3, 2);
                setupBookClick(R.id.ivGridCover3, 2);
            });
        }).start();
    }

    /**
     * Tự động đẩy dữ liệu mẫu lên Firestore (bao gồm Category giống mydio.vn)
     */
    private void seedFirestoreIfNecessary() {
        SharedPreferences prefs = getSharedPreferences("fonos_sync", MODE_PRIVATE);
        if (prefs.getBoolean("firestore_seeded_v4", false)) {
            Log.d("FonosClone", "Firestore already seeded v4. Skipping.");
            return;
        }

        FirebaseFirestore firestore = FirebaseFirestore.getInstance();

        // 1. Tạo danh sách sách mẫu kèm Category tương ứng như mydio.vn
        List<Book> sampleBooks = new ArrayList<>();
        sampleBooks.add(new Book("Hậu Hồng Lâu Mộng", "Tào Tuyết Cần", "AUDIOBOOK", "hau_hong_lau_mong", "demo_audio", true, "Văn học & Truyện"));
        sampleBooks.add(new Book("Tại sao người thông minh dễ tổn thương", "Đặng cập nhật", "AUDIOBOOK", "tai_sao_nguoi_thong_minh_de_ton_thuong", "demo_audio", false, "Tâm lý học"));
        sampleBooks.add(new Book("Bông hồng nhung", "Nhiều tác giả", "AUDIOBOOK", "bong_hong_nhung", "demo_audio", false, "Văn học & Truyện"));
        sampleBooks.add(new Book("Những người khốn khổ", "Victor Hugo", "AUDIOBOOK", "nhung_nguoi_khon_kho", "demo_audio", true, "Văn học & Truyện"));
        sampleBooks.add(new Book("Lá bài chủ", "Đặng cập nhật", "AUDIOBOOK", "la_bai_chu", "demo_audio", false, "Văn học & Truyện"));
        sampleBooks.add(new Book("Bên kia bức tường", "Nguyễn Nhật Ánh", "AUDIOBOOK", "ben_kia_buc_tuong", "demo_audio", false, "Thiếu nhi"));
        sampleBooks.add(new Book("Mùi hương", "Patrick Suskind", "AUDIOBOOK", "mui_huong", "demo_audio", false, "Văn học & Truyện"));
        
        sampleBooks.add(new Book("Ra quyết định thông minh", "Đặng cập nhật", "EBOOK", "ra_quyet_dinh_thong_minh", "demo_audio", false, "Kinh doanh"));
        sampleBooks.add(new Book("Khéo ăn nói được thiên hạ", "Trác Nhã", "EBOOK", "kheo_an_noi_duok_thien_ha", "demo_audio", false, "Kỹ năng sống"));
        sampleBooks.add(new Book("Xây dựng đội nhóm hiệu suất cao", "Đặng cập nhật", "EBOOK", "xay_dung_doi_nhom_hieu_suat_cao", "demo_audio", true, "Kinh doanh"));
        
        sampleBooks.add(new Book("Nghĩ giàu và làm giàu", "Napoleon Hill", "SUMMARY", "nghi_giau_va_lam_giau", "demo_audio", false, "Kinh doanh"));
        sampleBooks.add(new Book("Luyện trí nhớ", "Đặng cập nhật", "SUMMARY", "luyen_tri_nho", "demo_audio", false, "Kỹ năng sống"));

        // 2. Tạo danh sách khóa học mẫu
        List<PodCourse> sampleCourses = new ArrayList<>();
        sampleCourses.add(new PodCourse("AI for Beginners", "Nam Nguyễn", "Technology", "#1E8080", 4.8));
        sampleCourses.add(new PodCourse("Management for First-Time Leaders", "Vũ Đức Trí", "Management", "#7A5540", 4.7));
        sampleCourses.add(new PodCourse("Personal Finance Thinking", "Trần Việt Quân", "Finance", "#1A3A5C", 4.7));
        sampleCourses.add(new PodCourse("Kỹ năng Thuyết trình Chuyên nghiệp", "Nhiều tác giả", "Soft Skills", "#5A3E91", 4.6));

        int totalWrites = sampleBooks.size() + sampleCourses.size();
        final int[] completedWrites = {0};
        final boolean[] hasShownError = {false};

        Log.d("FonosClone", "Starting Firestore auto-seeding v4. Total writes: " + totalWrites);

        // Gửi thông báo bắt đầu đồng bộ
        Toast.makeText(this, "Đang đồng bộ dữ liệu kèm Category kiểu mydio.vn...", Toast.LENGTH_SHORT).show();

        // Ghi sách
        int bookId = 1;
        for (Book book : sampleBooks) {
            book.id = bookId;
            Map<String, Object> map = new HashMap<>();
            map.put("id", book.id);
            map.put("title", book.title);
            map.put("author", book.author);
            map.put("type", book.type);
            map.put("coverImage", book.coverImage);
            map.put("audioResName", book.audioResName);
            map.put("isFavorite", book.isFavorite);
            map.put("category", book.category); // Thêm cột Category vào Firestore!

            firestore.collection("books").document("book_" + book.id).set(map)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        completedWrites[0]++;
                        checkSeedingFinished(completedWrites[0], totalWrites, prefs);
                    } else {
                        if (!hasShownError[0]) {
                            hasShownError[0] = true;
                            String error = task.getException() != null ? task.getException().getMessage() : "Permission Denied hoặc lỗi mạng";
                            Log.e("FonosClone", "Seeding failed: " + error);
                            Toast.makeText(MainActivity.this, "Đồng bộ thất bại: " + error, Toast.LENGTH_LONG).show();
                        }
                    }
                });
            bookId++;
        }

        // Ghi khóa học
        int courseId = 1;
        for (PodCourse course : sampleCourses) {
            course.id = courseId;
            Map<String, Object> map = new HashMap<>();
            map.put("id", course.id);
            map.put("title", course.title);
            map.put("teacher", course.teacher);
            map.put("category", course.category);
            map.put("coverColor", course.coverColor);
            map.put("rating", course.rating);

            firestore.collection("pod_courses").document("course_" + course.id).set(map)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        completedWrites[0]++;
                        checkSeedingFinished(completedWrites[0], totalWrites, prefs);
                    } else {
                        if (!hasShownError[0]) {
                            hasShownError[0] = true;
                            String error = task.getException() != null ? task.getException().getMessage() : "Permission Denied hoặc lỗi mạng";
                            Log.e("FonosClone", "Seeding failed: " + error);
                            Toast.makeText(MainActivity.this, "Đồng bộ thất bại: " + error, Toast.LENGTH_LONG).show();
                        }
                    }
                });
            courseId++;
        }
    }

    private void checkSeedingFinished(int completed, int total, SharedPreferences prefs) {
        Log.d("FonosClone", "Write progress: " + completed + "/" + total);
        if (completed == total) {
            Log.d("FonosClone", "All records successfully seeded to Firestore!");
            Toast.makeText(this, "Đồng bộ CSDL category kiểu mydio.vn thành công!", Toast.LENGTH_LONG).show();
            prefs.edit().putBoolean("firestore_seeded_v4", true).apply();
            
            // Tải lại sách sau khi đồng bộ thành công
            AppDatabase db = AppDatabase.getInstance(this);
            loadLocalBooks(db);
        }
    }

    private void setupBookClick(int viewId, int bookIndex) {
        findViewById(viewId).setOnClickListener(v -> {
            if (currentAudiobooks.size() <= bookIndex) return;
            openPlayer(currentAudiobooks.get(bookIndex));
        });
    }

    private void openPlayer(Book book) {
        openPlayer(book, 0);
    }

    private void openPlayer(Book book, int startPositionMs) {
        Intent intent = new Intent(this, PlayerActivity.class);
        intent.putExtra(AudioPlayerService.EXTRA_TITLE, book.title);
        intent.putExtra(AudioPlayerService.EXTRA_AUTHOR, book.author);
        intent.putExtra(AudioPlayerService.EXTRA_AUDIO_RES, book.audioResName);
        intent.putExtra(PlayerActivity.EXTRA_BOOK_ID, book.id);
        intent.putExtra("cover_image", book.coverImage);
        intent.putExtra(AudioPlayerService.EXTRA_START_POSITION_MS, startPositionMs);
        startActivity(intent);
    }

    private void bindContinueListening(Book book, ListeningProgress progress) {
        if (book == null) return;

        continueBook = book;
        continuePositionMs = progress != null ? progress.positionMs : 0;

        bindCover(book, R.id.ivCover1);

        TextView labelView = findViewById(R.id.tvContinueLabel);
        TextView titleView = findViewById(R.id.tvContinueTitle);
        TextView metaView = findViewById(R.id.tvContinueMeta);
        TextView timeView = findViewById(R.id.tvContinueTime);

        if (labelView != null) {
            labelView.setText(progress != null && progress.positionMs > 0
                    ? R.string.home_continue
                    : R.string.home_today_pick);
        }
        if (titleView != null) {
            titleView.setText(book.title);
        }
        if (metaView != null) {
            String category = book.category != null ? book.category : book.type;
            metaView.setText(book.author + " · " + category);
        }
        if (timeView != null) {
            if (progress != null && progress.durationMs > 0) {
                timeView.setText(formatPlayerTime(progress.positionMs) + " / " + formatPlayerTime(progress.durationMs));
            } else {
                timeView.setText(R.string.home_minutes);
            }
        }

        View card = findViewById(R.id.continueListeningCard);
        View play = findViewById(R.id.btnContinuePlay);
        View cover = findViewById(R.id.ivCover1);
        View.OnClickListener listener = v -> openPlayer(continueBook, continuePositionMs);
        if (card != null) card.setOnClickListener(listener);
        if (play != null) play.setOnClickListener(listener);
        if (cover != null) cover.setOnClickListener(listener);
    }

    private String formatPlayerTime(int millis) {
        int totalSeconds = Math.max(0, millis / 1000);
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        return String.format(Locale.US, "%02d:%02d", minutes, seconds);
    }

    private void bindBook(Book book, int coverViewId, int titleViewId, int authorViewId) {
        bindCover(book, coverViewId);

        TextView titleView = findViewById(titleViewId);
        TextView authorView = findViewById(authorViewId);

        titleView.setText(book.title);
        authorView.setText(book.author);
    }

    private void bindCover(Book book, int coverViewId) {
        ImageView coverView = findViewById(coverViewId);
        int imageResId = getResources().getIdentifier(
                book.coverImage,
                "drawable",
                getPackageName()
        );

        if (imageResId != 0) {
            coverView.setImageResource(imageResId);
        }
    }
}
