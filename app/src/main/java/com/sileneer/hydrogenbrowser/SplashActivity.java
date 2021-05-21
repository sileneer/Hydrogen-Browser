package com.sileneer.hydrogenbrowser;

import android.os.Bundle;
import android.os.Handler;

import com.sileneer.hydrogenbrowser.common.base.BaseActivity;

public class SplashActivity extends BaseActivity {

    private static final int SPLASH_DISPLAY_LENGTH = 500;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                MainActivity.actionStart(SplashActivity.this);
                finish();
            }
        }, SPLASH_DISPLAY_LENGTH);
    }
}