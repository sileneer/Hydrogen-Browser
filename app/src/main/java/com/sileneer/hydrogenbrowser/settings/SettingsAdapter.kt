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
    private val items: List<SettingsItem>,
    private val context: Context
) : RecyclerView.Adapter<SettingsAdapter.ViewHolder>() {

    private val editor = context.getSharedPreferences("config", 0).edit()

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val settingsText: TextView = itemView.findViewById(R.id.settings_item_text)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.settings_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.settingsText.setText(item.titleRes)
        holder.itemView.setOnClickListener {
            when (item) {
                SettingsItem.SEARCH_ENGINE -> showSearchEngineDialog(holder.itemView.context)
                SettingsItem.HOMEPAGE -> showHomepageDialog()
                SettingsItem.ABOUT -> AboutActivity.actionStart(holder.itemView.context)
                SettingsItem.OPEN_SOURCE -> showOpenSourceDialog(holder.itemView.context)
            }
        }
    }

    override fun getItemCount(): Int = items.size

    private fun showSearchEngineDialog(dialogContext: Context) {
        AlertDialog.Builder(dialogContext)
            .setTitle(R.string.select_search_engine)
            .setSingleChoiceItems(
                SearchEngine.displayNames,
                context.getSharedPreferences("config", 0).getInt("search engines", 0)
            ) { _, which ->
                editor.putInt("search engines", which)
            }
            .setPositiveButton(R.string.ok) { _, _ -> editor.apply() }
            .setNegativeButton(R.string.cancel) { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun showHomepageDialog() {
        val adView = LayoutInflater.from(context)
            .inflate(R.layout.homepage_alert_dialog, null, false)
        val et = adView.findViewById<EditText>(R.id.homepage_edittext)
        val currentHomepage = context.getSharedPreferences("config", Context.MODE_PRIVATE)
            .getString("homepage", "www.google.com")
        AlertDialog.Builder(context)
            .setTitle(R.string.homepage)
            .setMessage(context.getString(R.string.current_homepage, currentHomepage))
            .setView(adView)
            .setPositiveButton(R.string.ok) { _, _ ->
                val input = et.text.toString()
                if (!TextUtils.isEmpty(input)) {
                    editor.putString("homepage", input)
                    editor.apply()
                    Toast.makeText(context, R.string.homepage_edit_success, Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(context, R.string.homepage_edit_error_empty, Toast.LENGTH_LONG).show()
                }
                Utils.hideKeyboard(context, adView)
            }
            .setNegativeButton(R.string.cancel) { dialog, _ ->
                Utils.hideKeyboard(context, adView)
                dialog.dismiss()
            }
            .setCancelable(false)
            .show()
        et.requestFocus()
        Utils.showKeyboard(context, et)
    }

    private fun showOpenSourceDialog(dialogContext: Context) {
        AlertDialog.Builder(context)
            .setTitle(R.string.open_source_title)
            .setMessage(R.string.open_source_redirect)
            .setPositiveButton(R.string.yes) { _, _ ->
                MainActivity.actionStart(dialogContext, "https://github.com/sileneer/Hydrogen-Browser")
            }
            .setNegativeButton(R.string.no) { _, _ -> }
            .setNeutralButton(R.string.open_in_default_browser) { _, _ ->
                val intent = Intent(Intent.ACTION_VIEW)
                intent.data = "https://github.com/sileneer/Hydrogen-Browser".toUri()
                context.startActivity(intent)
            }
            .show()
    }
}
