package hcmute.com.fonosclone;
import android.os.Bundle;

public class LibraryActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_library);

        applySystemBarPadding(R.id.main);
        setupBottomNavigation(NAV_LIBRARY);
        setupUserMenu();
    }
}
