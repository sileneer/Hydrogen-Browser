package com.sileneer.hydrogenbrowser.common.base;

import android.os.Bundle;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.gyf.immersionbar.ImmersionBar;
import com.sileneer.hydrogenbrowser.common.utils.ActivityCollector;

public class BaseActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityCollector.addActivity(this);

        ImmersionBar.with(this).statusBarDarkFont(true);
        ImmersionBar.with(this).autoDarkModeEnable(true);
        ImmersionBar.with(this).transparentBar().fullScreen(false);
        ImmersionBar.with(this).keyboardEnable(true);
        ImmersionBar.with(this).fitsSystemWindows(true).init();

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ActivityCollector.removeActivity(this);
    }
}
