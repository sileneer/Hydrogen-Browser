package com.sileneer.hydrogenbrowser.settings

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.sileneer.hydrogenbrowser.R
import com.sileneer.hydrogenbrowser.common.base.BaseActivity
import com.sileneer.hydrogenbrowser.databinding.ActivityAboutBinding

class AboutActivity : BaseActivity() {

    private lateinit var binding: ActivityAboutBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAboutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.title.setTitleText(getString(R.string.about))

        val versionName = packageManager.getPackageInfo(packageName, 0).versionName
        binding.appVersion.text = getString(R.string.version_format, versionName)
    }

    companion object {
        fun actionStart(context: Context) {
            val intent = Intent(context, AboutActivity::class.java)
            context.startActivity(intent)
        }
    }
}
