package com.sileneer.hydrogenbrowser.settings

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.core.net.toUri
import androidx.recyclerview.widget.RecyclerView
import com.sileneer.hydrogenbrowser.MainActivity
import com.sileneer.hydrogenbrowser.R
import com.sileneer.hydrogenbrowser.common.SearchEngine
import com.sileneer.hydrogenbrowser.common.utils.Utils

class SettingsAdapter(
    private val settingsList: List<Settings>,
    private val context: Context
) : RecyclerView.Adapter<SettingsAdapter.ViewHolder>() {

    private val editor = context.getSharedPreferences("config", 0).edit()

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val settingsView: View = itemView
        val settingsText: TextView = itemView.findViewById(R.id.settings_item_text)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.settings_item, parent, false)
        val holder = ViewHolder(view)
        holder.settingsView.setOnClickListener {
            val position = holder.absoluteAdapterPosition
            val setting = settingsList[position]
            when (setting.name) {
                "Search Engine" -> {
                    AlertDialog.Builder(parent.context)
                        .setTitle("Please select your search engine:")
                        .setSingleChoiceItems(
                            SearchEngine.displayNames,
                            context.getSharedPreferences("config", 0).getInt("search engines", 0)
                        ) { _, which ->
                            editor.putInt("search engines", which)
                        }
                        .setPositiveButton("OK") { _, _ -> editor.apply() }
                        .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
                        .show()
                }
                "Homepage" -> {
                    val adView = LayoutInflater.from(context)
                        .inflate(R.layout.homepage_alert_dialog, null, false)
                    val et = adView.findViewById<EditText>(R.id.homepage_edittext)
                    AlertDialog.Builder(context)
                        .setTitle("Homepage")
                        .setMessage(
                            "\nCurrent homepage: " +
                                    context.getSharedPreferences("config", Context.MODE_PRIVATE)
                                        .getString("homepage", "www.google.com")
                        )
                        .setView(adView)
                        .setPositiveButton("OK") { _, _ ->
                            val input = et.text.toString()
                            if (!TextUtils.isEmpty(input)) {
                                editor.putString("homepage", input)
                                editor.apply()
                                Toast.makeText(context, "Homepage edited successfully", Toast.LENGTH_LONG).show()
                            } else {
                                Toast.makeText(context, "Error: Input URL is empty", Toast.LENGTH_LONG).show()
                            }
                            Utils.hideKeyboard(context, adView)
                        }
                        .setNegativeButton("Cancel") { dialog, _ ->
                            Utils.hideKeyboard(context, adView)
                            dialog.dismiss()
                        }
                        .setCancelable(false)
                        .show()
                    et.requestFocus()
                    Utils.showKeyboard(context, et)
                }
                "Advanced" -> { }
                "About" -> AboutActivity.actionStart(parent.context)
                "Open Source" -> {
                    AlertDialog.Builder(context)
                        .setTitle("Open Source")
                        .setMessage("You will be redirected github.com. Are you sure to continue?")
                        .setPositiveButton("Yes") { _, _ ->
                            MainActivity.actionStart(parent.context, "https://github.com/sileneer/Hydrogen-Browser")
                        }
                        .setNegativeButton("No") { _, _ -> }
                        .setNeutralButton("Open in Default Browser") { _, _ ->
                            val intent = Intent(Intent.ACTION_VIEW)
                            intent.data = "https://github.com/sileneer/Hydrogen-Browser".toUri()
                            context.startActivity(intent)
                        }
                        .show()
                }
            }
        }
        return holder
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.settingsText.text = settingsList[position].name
    }

    override fun getItemCount(): Int = settingsList.size
}
