package hcmute.com.fonosclone;
import android.os.Bundle;

public class PodcourseActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_podcourse);

        applySystemBarPadding(R.id.main);
        setupBottomNavigation(NAV_PODCOURSE);
    }
}
