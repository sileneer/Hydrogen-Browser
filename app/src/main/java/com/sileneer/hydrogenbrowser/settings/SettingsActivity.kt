package com.sileneer.hydrogenbrowser.settings

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.sileneer.hydrogenbrowser.common.base.BaseActivity
import com.sileneer.hydrogenbrowser.databinding.ActivitySettingsBinding

class SettingsActivity : BaseActivity() {

    private lateinit var binding: ActivitySettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.title.setTitleText(getString(com.sileneer.hydrogenbrowser.R.string.settings))

        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = SettingsAdapter(SettingsItem.entries, this)
    }

    companion object {
        fun actionStart(context: Context) {
            val intent = Intent(context, SettingsActivity::class.java)
            context.startActivity(intent)
        }
    }
}
