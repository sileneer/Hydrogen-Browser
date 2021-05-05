package com.sileneer.hydrogenbrowser;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;

public class SettingsActivity extends AppCompatActivity {

    private ImageView back;
    private ListView listView;

    private String[] menus = {"Search Engine", "Homepage", "Advanced", "About", "Open Source"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        
        back = findViewById(R.id.title_back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(SettingsActivity.this, android.R.layout.simple_list_item_1, menus);
        listView = findViewById(R.id.list_view);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch(position){
                    case 1:
                        HomepageActivity.actionStart(SettingsActivity.this);
                        break;
                    case 3:
                        AboutActivity.actionStart(SettingsActivity.this);
                        break;
                    case 4:

                }
            }
        });
    }

    protected static void actionStart (Context context){
        Intent intent = new Intent(context, SettingsActivity.class);
        context.startActivity(intent);
    }
}