package com.sileneer.hydrogenbrowser;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

public class HomepageActivity extends AppCompatActivity {

    private ImageView back;
    private Button confirm;
    private EditText url;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_homepage);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }

        back = findViewById(R.id.title_back);
        confirm = findViewById(R.id.confirm);
        url = findViewById(R.id.home_page_url);

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String input = url.getText().toString();
                MainActivity.homepageUrl = input;
                Toast.makeText(HomepageActivity.this,"Homepage edited successfully", Toast.LENGTH_LONG).show();
                MainActivity.hideKeyboard(HomepageActivity.this);
            }
        });
    }

    protected static void actionStart (Context context){
        Intent intent = new Intent(context, HomepageActivity.class);
        context.startActivity(intent);
    }
}