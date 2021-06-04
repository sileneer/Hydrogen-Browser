package com.sileneer.hydrogenbrowser.settings;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.ViewGroup;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.sileneer.hydrogenbrowser.MainActivity;
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
        for (String settings_item_name: settings_items) {
            settingsList.add(new Settings(settings_item_name));
        }
    }

    protected static void showSearchEngines(ViewGroup parent) {
        AlertDialog.Builder ad = new AlertDialog.Builder(parent.getContext());
        ad.setTitle("Please select your search engine:");

        ad.setSingleChoiceItems(MainActivity.searchEngines, MainActivity.searchEnginesIndex, new DialogInterface.OnClickListener() {

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
                MainActivity.changeAddressBarHint(MainActivity.searchEnginesIndex);
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

    protected static void showOpenSource(ViewGroup parent) {
        AlertDialog.Builder ad = new AlertDialog.Builder(parent.getContext());
        ad.setTitle("Open Source");
        ad.setMessage("You will be redirected github.com. Are you sure to continue?");
        ad.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int i) {
                MainActivity.actionStart(parent.getContext());
                MainActivity.loadOpenSource();
            }
        });
        ad.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int i) {
            }
        });
        ad.show();
    }

    public static void actionStart(Context context) {
        Intent intent = new Intent(context, SettingsActivity.class);
        context.startActivity(intent);
    }
}