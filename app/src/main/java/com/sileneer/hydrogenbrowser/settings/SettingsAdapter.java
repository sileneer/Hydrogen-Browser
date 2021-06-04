package com.sileneer.hydrogenbrowser.settings;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.sileneer.hydrogenbrowser.R;

import java.util.List;

public class SettingsAdapter extends RecyclerView.Adapter<SettingsAdapter.ViewHolder> {

    protected List<Settings> mSettingsList;

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
                        SettingsActivity.showSearchEngines(parent);
                        break;
                    case "Homepage":
                        HomepageSettingActivity.actionStart(parent.getContext());
                        break;
                    case "Advanced":
                        break;
                    case "About":
                        AboutActivity.actionStart(parent.getContext());
                        break;
                    case "Open Source":
                        SettingsActivity.showOpenSource(parent);
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
}
