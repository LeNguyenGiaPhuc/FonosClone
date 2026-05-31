package hcmute.com.fonosclone;

import android.os.Bundle;
import android.widget.LinearLayout;

import java.util.List;

import hcmute.com.fonosclone.data.AppDatabase;
import hcmute.com.fonosclone.data.FonosDao;
import hcmute.com.fonosclone.data.PodCourse;
import hcmute.com.fonosclone.data.SeedData;

public class PodcourseActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_podcourse);

        applySystemBarPadding(R.id.main);
        setupBottomNavigation(NAV_PODCOURSE);
        setupUserMenu();

        loadPodCourses();
    }

    private void loadPodCourses() {
        LinearLayout horizontalContainer = findViewById(R.id.horizontalCoursesContainer);
        LinearLayout verticalContainer = findViewById(R.id.featuredCoursesContainer);

        new Thread(() -> {
            FonosDao dao = AppDatabase.getInstance(getApplicationContext()).fonosDao();
            // Seed database if it's empty
            SeedData.insertSampleData(dao);
            
            // Retrieve data
            List<PodCourse> courses = dao.getPodCourses();

            // Render on UI thread
            runOnUiThread(() -> {
                PodCourseListRenderer.renderHorizontal(this, horizontalContainer, courses);
                PodCourseListRenderer.renderVertical(this, verticalContainer, courses);
            });
        }).start();
    }
}
