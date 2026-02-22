package com.sileneer.hydrogenbrowser.settings

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import com.sileneer.hydrogenbrowser.R
import com.sileneer.hydrogenbrowser.common.base.BaseActivity
import com.sileneer.hydrogenbrowser.common.utils.TitleLayout

class AboutActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)

        findViewById<TitleLayout>(R.id.title).setTitleText(getString(R.string.about))

        val version = findViewById<TextView>(R.id.app_version)
        val versionName = packageManager.getPackageInfo(packageName, 0).versionName
        version.text = getString(R.string.version_format, versionName)
    }

    companion object {
        fun actionStart(context: Context) {
            val intent = Intent(context, AboutActivity::class.java)
            context.startActivity(intent)
        }
    }
}
