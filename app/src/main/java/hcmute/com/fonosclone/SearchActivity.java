package hcmute.com.fonosclone;
import android.os.Bundle;

public class SearchActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        applySystemBarPadding(R.id.main);
        setupBottomNavigation(NAV_EXPLORE);
        setupUserMenu();
    }
}
