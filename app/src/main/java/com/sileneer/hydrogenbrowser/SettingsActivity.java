package com.sileneer.hydrogenbrowser;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class SettingsActivity extends BaseActivity {

    private List<Settings> settingsList = new ArrayList<>();

    private final String[] settings_items = {"Search Engine", "Homepage", "Advanced", "About", "Open Source"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        TitleLayout.setTitleText("Settings");

        initSettings();
        RecyclerView recyclerview = findViewById(R.id.recycler_view);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerview.setLayoutManager(layoutManager);
        SettingsAdapter adapter = new SettingsAdapter(settingsList);
        recyclerview.setAdapter(adapter);
    }

    private void initSettings() {
        for (int i = 0; i < settings_items.length; i++) {
            settingsList.add(new Settings(settings_items[i]));
        }
    }


    protected static void actionStart(Context context) {
        Intent intent = new Intent(context, SettingsActivity.class);
        context.startActivity(intent);
    }
}