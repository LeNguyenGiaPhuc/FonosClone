package hcmute.com.fonosclone;
import android.os.Bundle;

public class ChallengesActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_challenges);

        applySystemBarPadding(R.id.main);
        setupBottomNavigation(NAV_CHALLENGES);
    }
}
