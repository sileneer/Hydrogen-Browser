package com.sileneer.hydrogenbrowser

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.KeyEvent
import android.view.Menu
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import com.sileneer.hydrogenbrowser.common.base.BaseActivity
import com.sileneer.hydrogenbrowser.common.utils.ActivityCollector
import com.sileneer.hydrogenbrowser.common.utils.Utils
import com.sileneer.hydrogenbrowser.settings.SettingsActivity
import me.jingbin.progress.WebProgress

class MainActivity : BaseActivity() {

    private val viewModel: MainViewModel by viewModels()

    private lateinit var addressBar: AutoCompleteTextView
    private lateinit var webView: WebView
    private lateinit var back: ImageView
    private lateinit var forward: ImageView
    private lateinit var refresh: ImageView
    private lateinit var home: ImageView
    private lateinit var menuImage: ImageView
    private lateinit var multiTabButton: TextView
    private lateinit var progressBar: WebProgress

    private val webViewClient = MyWebViewClient()
    private val webChromeClient = MyWebChromeClient()

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        webView = findViewById(R.id.webview)
        addressBar = findViewById(R.id.url)
        back = findViewById(R.id.back)
        forward = findViewById(R.id.forward)
        refresh = findViewById(R.id.refresh)
        home = findViewById(R.id.home)
        menuImage = findViewById(R.id.menu)
        progressBar = findViewById(R.id.progress_bar)
        multiTabButton = findViewById(R.id.multi_tab_button)

        menuImage.setOnClickListener { showPopupMenu(menuImage) }
        multiTabButton.setOnClickListener { showMultiTabMenu(multiTabButton) }

        initWebView()
        updateTabCount()

        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, data)
        addressBar.setAdapter(adapter)

        addressBar.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                addressBar.setText(webView.url)
                addressBar.selectAll()
            } else {
                val title = webView.title
                if (!TextUtils.isEmpty(title)) {
                    addressBar.setText(title)
                }
            }
        }

        addressBar.setOnKeyListener { _, keyCode, _ ->
            if (keyCode == KeyEvent.KEYCODE_ENTER) {
                Utils.hideKeyboard(this@MainActivity)
                val input = addressBar.text.toString().trim()
                val url = viewModel.resolveUrl(input)
                webView.loadUrl(url)
                addressBar.clearFocus()
                true
            } else {
                false
            }
        }

        back.isEnabled = false
        forward.isEnabled = false

        back.setOnClickListener { pageGoBack() }
        forward.setOnClickListener { pageGoForward() }
        refresh.setOnClickListener { webView.loadUrl(webView.url!!) }
        home.setOnClickListener { webView.loadUrl(viewModel.getHomepage()) }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (webView.canGoBack()) {
                    pageGoBack()
                } else {
                    confirmExit()
                }
            }
        })
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun initWebView() {
        updateAddressBarHint()
        webView.webViewClient = webViewClient
        webView.webChromeClient = webChromeClient
        webView.settings.javaScriptEnabled = true
        webView.loadUrl(viewModel.getHomepage())
        webView.settings.setSupportMultipleWindows(true)
    }

    override fun onResume() {
        super.onResume()
        Log.d("MainActivity", "onResume")
        updateAddressBarHint()

        val intent = intent
        val url = intent.data?.toString() ?: intent.getStringExtra("url")
        if (url != null) {
            openWebpage(url)
        }
    }

    private inner class MyWebViewClient : WebViewClient() {
        override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
            updateNavigationButtons()
            addressBar.setText(webView.url)
            progressBar.show()
        }

        override fun onPageFinished(view: WebView, url: String) {
            super.onPageFinished(view, url)
            updateNavigationButtons()
            progressBar.hide()
            saveCurrentTabState()

            if (!addressBar.isFocused) {
                val title = webView.title
                if (!TextUtils.isEmpty(title)) {
                    addressBar.setText(title)
                }
            }
        }
    }

    private inner class MyWebChromeClient : WebChromeClient() {
        override fun onProgressChanged(view: WebView, newProgress: Int) {
            super.onProgressChanged(view, newProgress)
            progressBar.setWebProgress(newProgress)
        }
    }

    private fun updateNavigationButtons() {
        back.isEnabled = webView.canGoBack()
        forward.isEnabled = webView.canGoForward()
    }

    fun pageGoBack(): Boolean {
        webView.goBack()
        return true
    }

    fun pageGoForward(): Boolean {
        webView.goForward()
        return true
    }

    private fun confirmExit() {
        AlertDialog.Builder(this)
            .setTitle(R.string.exit_warning_title)
            .setMessage(R.string.exit_warning_message)
            .setPositiveButton(R.string.yes) { _, _ -> ActivityCollector.finishAll() }
            .setNegativeButton(R.string.no) { _, _ -> }
            .show()
    }

    private fun showPopupMenu(view: View) {
        val popupMenu = PopupMenu(this, view)
        popupMenu.menuInflater.inflate(R.menu.main, popupMenu.menu)
        popupMenu.show()
        popupMenu.setOnMenuItemClickListener { item ->
            if (item.itemId == R.id.settings) {
                SettingsActivity.actionStart(this@MainActivity)
                true
            } else {
                false
            }
        }
    }

    private fun showMultiTabMenu(view: View) {
        val popupMenu = PopupMenu(this, view)
        val tabManager = viewModel.tabManager
        for ((index, tab) in tabManager.tabs.withIndex()) {
            val title = if (index == tabManager.activeTabIndex)
                "\u25B6 ${tab.displayTitle}" else tab.displayTitle
            popupMenu.menu.add(0, index, Menu.NONE, title)
        }
        popupMenu.menu.add(1, MENU_NEW_TAB, Menu.NONE, getString(R.string.new_tab_action))
        popupMenu.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                MENU_NEW_TAB -> {
                    saveCurrentTabState()
                    tabManager.addTab()
                    updateTabCount()
                    webView.loadUrl(viewModel.getHomepage())
                    true
                }
                else -> {
                    val index = item.itemId
                    if (index != tabManager.activeTabIndex && index in tabManager.tabs.indices) {
                        saveCurrentTabState()
                        tabManager.switchTo(index)
                        val tab = tabManager.activeTab
                        if (tab.url.isNotEmpty()) {
                            webView.loadUrl(tab.url)
                        } else {
                            webView.loadUrl(viewModel.getHomepage())
                        }
                    }
                    true
                }
            }
        }
        popupMenu.show()
    }

    private fun saveCurrentTabState() {
        viewModel.tabManager.updateActiveTab(
            url = webView.url ?: "",
            title = webView.title ?: ""
        )
    }

    private fun updateTabCount() {
        multiTabButton.text = viewModel.tabManager.tabCount.toString()
    }

    private fun updateAddressBarHint() {
        val engine = viewModel.getSearchEngine()
        addressBar.hint = getString(R.string.address_bar_hint, engine.displayName)
    }

    fun openWebpage(url: String?) {
        if (url != null) webView.loadUrl(url)
    }

    companion object {
        private const val MENU_NEW_TAB = 1000

        private val data = arrayOf(
            "www.baidu.com", "baidu.com",
            "www.google.com", "google.com",
            "www.youtube.com", "youtube.com",
            "www.bilibili.com", "bilibili.com",
            "www.zhihu.com", "zhihu.com"
        )

        fun actionStart(context: Context, url: String) {
            val intent = Intent(context, MainActivity::class.java)
            intent.putExtra("url", url)
            context.startActivity(intent)
        }
    }
}
