package com.sukinsan.cloudftp.activity;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.sukinsan.cloudftp.R;
import com.sukinsan.cloudftp.util.SystemUtils;
import com.sukinsan.cloudftp.util.SystemUtilsImpl;

public class SplashActivity extends AppCompatActivity {

    private final static int RC = 16;
    private SystemUtils systemUtils;
    private View helpView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        (helpView = findViewById(R.id.help_message)).setVisibility(View.GONE);
        systemUtils = new SystemUtilsImpl(this);
    }

    private void moveToHome() {
        startActivity(new Intent(this, HomeActivity.class));
        finish();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (systemUtils.doWeNeedToExplainWhyWeNeedThem(this)) {
            helpView.setVisibility(View.VISIBLE);// never called actually
        } else if (systemUtils.doWeNeedAnyPermissions()) {
            systemUtils.askAllPermissionsWeNeed(this, RC);
        } else {
            moveToHome();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == RC) {
            if (!systemUtils.doWeNeedAnyPermissions()) {
                moveToHome();
            } else {
                helpView.setVisibility(View.VISIBLE);
            }
        }
    }
}
