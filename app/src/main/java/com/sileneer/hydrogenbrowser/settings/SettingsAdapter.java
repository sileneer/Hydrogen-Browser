package com.sileneer.hydrogenbrowser.settings;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.sileneer.hydrogenbrowser.MainActivity;
import com.sileneer.hydrogenbrowser.R;
import com.sileneer.hydrogenbrowser.common.utils.Utils;

import java.util.List;

public class SettingsAdapter extends RecyclerView.Adapter<SettingsAdapter.ViewHolder> {

    protected List<Settings> mSettingsList;
    private Context context;

    private static SharedPreferences.Editor editor;

    static class ViewHolder extends RecyclerView.ViewHolder {
        View settingsView;
        TextView settingsText;

        public ViewHolder(View itemView) {
            super(itemView);
            settingsView = itemView;
            settingsText = itemView.findViewById(R.id.settings_item_text);
        }
    }

    public SettingsAdapter(List<Settings> mSettingsList, Context context) {
        this.mSettingsList = mSettingsList;
        this.context = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.settings_item, parent, false);
        final ViewHolder holder = new ViewHolder(view);
        editor = context.getSharedPreferences("config", 0).edit();
        holder.settingsView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                int position = holder.getAbsoluteAdapterPosition();
                Settings setting = mSettingsList.get(position);
                switch (setting.getName()) {

                    case "Search Engine":
                        AlertDialog.Builder ad_searchEngine = new AlertDialog.Builder(parent.getContext());
                        ad_searchEngine.setTitle("Please select your search engine:");

                        ad_searchEngine.setSingleChoiceItems(MainActivity.searchEngines, context.getSharedPreferences("config", 0).getInt("search engines", 0), new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                editor.putInt("search engines", which);
                            }
                        });
                        ad_searchEngine.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                editor.apply();
                            }
                        });

                        ad_searchEngine.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                        ad_searchEngine.show();
                        break;

                    case "Homepage":
                        AlertDialog.Builder ad_homepage = new AlertDialog.Builder(context);
                        ad_homepage.setTitle("Homepage");
                        ad_homepage.setMessage("\nCurrent homepage: " +
                                context.getSharedPreferences("config", Context.MODE_PRIVATE)
                                        .getString("homepage", "www.google.com"));

                        View ad_homepage_view = LayoutInflater.from(context).inflate(R.layout.homepage_alert_dialog, null, false);
                        ad_homepage.setView(ad_homepage_view);

                        EditText et = ad_homepage_view.findViewById(R.id.homepage_edittext);

                        ad_homepage.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                String input = et.getText().toString();
                                if (!TextUtils.isEmpty(input)) {
                                    editor.putString("homepage", input);
                                    editor.apply();
                                    Toast.makeText(context, "Homepage edited successfully", Toast.LENGTH_LONG).show();
                                } else {
                                    Toast.makeText(context, "Error: Input URL is empty", Toast.LENGTH_LONG).show();
                                }
                                Utils.hideKeyboard(context, ad_homepage_view);
                            }
                        });
                        ad_homepage.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Utils.hideKeyboard(context, ad_homepage_view);
                                dialogInterface.dismiss();
                            }
                        });
                        ad_homepage.setCancelable(false);
                        ad_homepage.show();

                        et.requestFocus();
                        Utils.showKeyboard(context, et);


                        break;

                    case "Advanced":
                        break;

                    case "About":
                        AboutActivity.actionStart(parent.getContext());
                        break;

                    case "Open Source":
                        AlertDialog.Builder ad_openSource = new AlertDialog.Builder(context);
                        ad_openSource.setTitle("Open Source");
                        ad_openSource.setMessage("You will be redirected github.com. Are you sure to continue?");
                        ad_openSource.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int i) {
                                MainActivity.actionStart(parent.getContext(), "https://github.com/sileneer/Hydrogen-Browser");
                            }
                        });
                        ad_openSource.setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int i) {
                            }
                        });
                        ad_openSource.setNeutralButton("Open in Default Browser", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Intent intent = new Intent(Intent.ACTION_VIEW);
                                intent.setData(Uri.parse("https://github.com/sileneer/Hydrogen-Browser"));
                                context.startActivity(intent);
                            }
                        });
                        ad_openSource.show();
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

