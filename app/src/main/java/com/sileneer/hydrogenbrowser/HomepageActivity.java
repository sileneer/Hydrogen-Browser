package com.sileneer.hydrogenbrowser;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class HomepageActivity extends BaseActivity {

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
                Toast.makeText(HomepageActivity.this, "Homepage edited successfully", Toast.LENGTH_LONG).show();
                MainActivity.hideKeyboard(HomepageActivity.this);
            }
        });
    }

    protected static void actionStart(Context context) {
        Intent intent = new Intent(context, HomepageActivity.class);
        context.startActivity(intent);
    }
}