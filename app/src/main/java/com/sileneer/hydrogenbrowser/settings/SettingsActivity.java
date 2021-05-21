package com.sileneer.hydrogenbrowser.settings;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.sileneer.hydrogenbrowser.common.base.BaseActivity;
import com.sileneer.hydrogenbrowser.R;
import com.sileneer.hydrogenbrowser.common.utils.TitleLayout;

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

    public static void actionStart(Context context) {
        Intent intent = new Intent(context, SettingsActivity.class);
        context.startActivity(intent);
    }
}