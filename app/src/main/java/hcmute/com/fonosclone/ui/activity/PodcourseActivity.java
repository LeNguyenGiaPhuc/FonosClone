package hcmute.com.fonosclone.ui.activity;


import hcmute.com.fonosclone.data.local.AppDatabase;
import hcmute.com.fonosclone.data.local.FonosDao;
import hcmute.com.fonosclone.data.model.PodCourse;
import hcmute.com.fonosclone.data.repository.FonosRepository;
import hcmute.com.fonosclone.data.seed.SeedData;
import hcmute.com.fonosclone.R;
import hcmute.com.fonosclone.ui.renderer.PodCourseListRenderer;

import android.os.Bundle;
import android.widget.LinearLayout;

import java.util.List;


public class PodcourseActivity extends BaseActivity {

    private FonosRepository repository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_podcourse);

        applySystemBarPadding(R.id.main);
        setupBottomNavigation(NAV_PODCOURSE);
        setupUserMenu();

        repository = new FonosRepository(this);

        // 1. Nạp nhanh từ SQLite cục bộ ngay khi mở màn hình (0ms lag)
        loadLocalPodCourses();

        // 2. Chạy ngầm đồng bộ danh sách Khóa học từ Cloud Firestore về máy
        syncCloudPodCourses();
    }

    private void loadLocalPodCourses() {
        LinearLayout horizontalContainer = findViewById(R.id.horizontalCoursesContainer);
        LinearLayout verticalContainer = findViewById(R.id.featuredCoursesContainer);

        new Thread(() -> {
            FonosDao dao = AppDatabase.getInstance(getApplicationContext()).fonosDao();
            if (dao.getPodCourses().isEmpty()) {
                SeedData.insertSampleData(dao);
            }
            
            List<PodCourse> courses = dao.getPodCourses();

            runOnUiThread(() -> {
                PodCourseListRenderer.renderHorizontal(this, horizontalContainer, courses);
                PodCourseListRenderer.renderVertical(this, verticalContainer, courses);
            });
        }).start();
    }

    private void syncCloudPodCourses() {
        repository.syncPodCourses(new FonosRepository.SyncCallback() {
            @Override
            public void onSuccess() {
                // Đồng bộ thành công -> Vẽ lại giao diện với dữ liệu đám mây mới nhất
                loadLocalPodCourses();
            }

            @Override
            public void onFailure(Exception e) {
                // Thất bại (như mất mạng) -> Giữ nguyên trạng thái hiển thị offline cục bộ
                e.printStackTrace();
            }
        });
    }
}
