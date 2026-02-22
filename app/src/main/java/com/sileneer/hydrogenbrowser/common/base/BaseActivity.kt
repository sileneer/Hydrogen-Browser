package com.sileneer.hydrogenbrowser.common.base

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.gyf.immersionbar.ImmersionBar
import com.sileneer.hydrogenbrowser.common.utils.ActivityCollector

open class BaseActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        ActivityCollector.addActivity(this)

        ImmersionBar.with(this)
            .statusBarDarkFont(true)
            .autoDarkModeEnable(true)
            .transparentBar()
            .fullScreen(false)
            .keyboardEnable(true)
            .fitsSystemWindows(true)
            .init()

        supportActionBar?.hide()
    }

    override fun onDestroy() {
        super.onDestroy()
        ActivityCollector.removeActivity(this)
    }
}
