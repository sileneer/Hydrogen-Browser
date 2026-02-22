package com.sileneer.hydrogenbrowser.settings

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.sileneer.hydrogenbrowser.R
import com.sileneer.hydrogenbrowser.common.base.BaseActivity
import com.sileneer.hydrogenbrowser.common.utils.TitleLayout

class SettingsActivity : BaseActivity() {

    private val settingsList = mutableListOf<Settings>()
    private val settingsItems = arrayOf("Search Engine", "Homepage", "Advanced", "About", "Open Source")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        findViewById<TitleLayout>(R.id.title).setTitleText("Settings")

        initSettings()
        val recyclerView = findViewById<RecyclerView>(R.id.recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = SettingsAdapter(settingsList, this)
    }

    private fun initSettings() {
        for (name in settingsItems) {
            settingsList.add(Settings(name))
        }
    }

    companion object {
        fun actionStart(context: Context) {
            val intent = Intent(context, SettingsActivity::class.java)
            context.startActivity(intent)
        }
    }
}
