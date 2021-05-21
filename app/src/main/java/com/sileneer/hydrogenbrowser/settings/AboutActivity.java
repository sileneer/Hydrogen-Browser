package com.sileneer.hydrogenbrowser.settings;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import com.sileneer.hydrogenbrowser.common.base.BaseActivity;
import com.sileneer.hydrogenbrowser.BuildConfig;
import com.sileneer.hydrogenbrowser.R;
import com.sileneer.hydrogenbrowser.common.utils.TitleLayout;

public class AboutActivity extends BaseActivity {

    private TextView version;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        TitleLayout.setTitleText("About");

        version = findViewById(R.id.app_version);
        version.setText("Version: "+ BuildConfig.VERSION_NAME);
    }

    public static void actionStart(Context context){
        Intent intent = new Intent(context, AboutActivity.class);
        context.startActivity(intent);
    }
}