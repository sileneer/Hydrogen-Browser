package com.sileneer.hydrogenbrowser;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class SettingsAdapter extends RecyclerView.Adapter<SettingsAdapter.ViewHolder> {

    protected List<Settings> mSettingsList;
    private RecyclerView recyclerView;

    static class ViewHolder extends RecyclerView.ViewHolder {
        View settingsView;
        TextView settingsText;

        public ViewHolder(View itemView) {
            super(itemView);
            settingsView = itemView;
            settingsText = itemView.findViewById(R.id.settings_item_text);
        }
    }

    public SettingsAdapter(List<Settings> mSettingsList) {
        this.mSettingsList = mSettingsList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.settings_item, parent, false);
        final ViewHolder holder = new ViewHolder(view);
        holder.settingsView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                int position = holder.getAbsoluteAdapterPosition();
                Settings setting = mSettingsList.get(position);
                switch (setting.getName()) {
                    case "Search Engine":
                        showSearchEngines(parent);
                        break;
                    case "Homepage":
                        HomepageActivity.actionStart(parent.getContext());
                        break;
                    case "Advanced":
                        break;
                    case "About":
                        AboutActivity.actionStart(parent.getContext());
                        break;
                    case "Open Source":
                        showOpenSource(parent);
                        break;
                }
            }
        });
        return holder;
    }

    @Override
    public void onBindViewHolder(SettingsAdapter.ViewHolder holder, int position) {
        Settings settings = mSettingsList.get(position);
        holder.settingsText.setText(settings.getName());

    }

    @Override
    public int getItemCount() {
        return mSettingsList.size();
    }

    private static void showSearchEngines(ViewGroup parent) {
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

    private static void showOpenSource(ViewGroup parent) {
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
        AlertDialog alert = ad.show();
    }
}
