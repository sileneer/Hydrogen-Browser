package com.sileneer.hydrogenbrowser.settings;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.sileneer.hydrogenbrowser.common.base.BaseActivity;
import com.sileneer.hydrogenbrowser.MainActivity;
import com.sileneer.hydrogenbrowser.R;
import com.sileneer.hydrogenbrowser.common.utils.TitleLayout;

public class HomepageSettingActivity extends BaseActivity {

    private Button confirm;
    private EditText url;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_homepage);

        TitleLayout.setTitleText("Homepage");

        confirm = findViewById(R.id.confirm);
        url = findViewById(R.id.home_page_url);

        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String input = url.getText().toString();
                MainActivity.editor_homepage.putString("homepage", input);
                MainActivity.editor_homepage.commit();
                MainActivity.homepageUrl = MainActivity.sharedPref_homepage.getString("homepage", "www.google.com");
                Toast.makeText(HomepageSettingActivity.this, "Homepage edited successfully", Toast.LENGTH_LONG).show();
                MainActivity.hideKeyboard(HomepageSettingActivity.this);
            }
        });
    }

    public static void actionStart(Context context) {
        Intent intent = new Intent(context, HomepageSettingActivity.class);
        context.startActivity(intent);
    }
}