package com.sileneer.hydrogenbrowser;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class AboutActivity extends BaseActivity {

    private ImageView back;
    private TextView version;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);


        back = findViewById(R.id.title_back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        version = findViewById(R.id.app_version);
        version.setText("Version: "+BuildConfig.VERSION_NAME);
    }

    protected static void actionStart (Context context){
        Intent intent = new Intent(context, AboutActivity.class);
        context.startActivity(intent);
    }
}