package hcmute.com.fonosclone;

import android.content.Intent;
import android.os.Bundle;

public class SettingsActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        applySystemBarPadding(R.id.main);

        findViewById(R.id.btnSettingsBack).setOnClickListener(v -> finish());
        findViewById(R.id.tvSettingsLogout).setOnClickListener(v -> {
            new SessionManager(this).logout();
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });
    }
}
