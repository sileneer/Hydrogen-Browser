package com.sileneer.hydrogenbrowser;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
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

    private final String[] menus = {"Search Engine", "Homepage", "Advanced", "About", "Open Source"};
    private final String[] searchEngines = {"Google", "Baidu", "Bing", "DuckDuckGo"};

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
                switch (position) {
                    case 0:
                        showSearchEngines();
                        break;
                    case 1:
                        HomepageActivity.actionStart(SettingsActivity.this);
                        break;
                    case 3:
                        AboutActivity.actionStart(SettingsActivity.this);
                        break;
                    case 4:
                        showOpenSource();
                        break;
                }
            }
        });
    }

    private void showSearchEngines() {
        AlertDialog.Builder ad = new AlertDialog.Builder(SettingsActivity.this);
        ad.setTitle("Please select your search engine:");

        ad.setSingleChoiceItems(searchEngines, MainActivity.searchEnginesIndex, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                MainActivity.editor_searchEngines.putInt("search engines", which);
                MainActivity.editor_searchEngines.commit();
            }
        });
        ad.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                MainActivity.searchEnginesIndex = MainActivity.sharedPref_searchEngines.getInt("search engines", 0);
            }
        });

        ad.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        ad.show();
    }

    private void showOpenSource() {
        AlertDialog.Builder ad = new AlertDialog.Builder(SettingsActivity.this);
        ad.setTitle("Open Source");
        ad.setMessage("You will be redirected github.com. Are you sure to continue?");
        ad.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int i) {
                MainActivity.actionStart(SettingsActivity.this);
                MainActivity.loadOpenSource();
            }
        });
        ad.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int i) {
            }
        });
        AlertDialog alert = ad.show();
    }

    protected static void actionStart(Context context) {
        Intent intent = new Intent(context, SettingsActivity.class);
        context.startActivity(intent);
    }
}