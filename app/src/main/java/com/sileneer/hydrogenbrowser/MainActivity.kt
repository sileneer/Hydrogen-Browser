package com.sileneer.hydrogenbrowser

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.KeyEvent
import android.view.Menu
import android.view.View
import android.widget.ArrayAdapter
import android.widget.PopupMenu
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import com.sileneer.hydrogenbrowser.common.base.BaseActivity
import com.sileneer.hydrogenbrowser.common.utils.ActivityCollector
import com.sileneer.hydrogenbrowser.common.utils.Utils
import androidx.webkit.WebSettingsCompat
import androidx.webkit.WebViewFeature
import com.sileneer.hydrogenbrowser.databinding.ActivityMainBinding
import com.sileneer.hydrogenbrowser.settings.SettingsActivity

class MainActivity : BaseActivity() {

    private val viewModel: MainViewModel by viewModels()
    private lateinit var binding: ActivityMainBinding

    private val webViewClient by lazy {
        BrowserWebViewClient(
            onPageStarted = { url ->
                updateNavigationButtons()
                binding.url.setText(url)
                binding.progressBar.show()
            },
            onPageFinished = { _, title ->
                updateNavigationButtons()
                binding.progressBar.hide()
                saveCurrentTabState()
                if (!binding.url.isFocused && !title.isNullOrEmpty()) {
                    binding.url.setText(title)
                }
            },
            onError = { _, description, _ ->
                binding.url.setText(getString(R.string.webview_error, description))
            }
        )
    }

    private val webChromeClient by lazy {
        BrowserWebChromeClient { progress ->
            binding.progressBar.setWebProgress(progress)
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.menu.setOnClickListener { showPopupMenu(binding.menu) }
        binding.multiTabButton.setOnClickListener { showMultiTabMenu(binding.multiTabButton) }

        initWebView()
        updateTabCount()

        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, data)
        binding.url.setAdapter(adapter)

        binding.url.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                binding.url.setText(binding.webview.url)
                binding.url.selectAll()
            } else {
                val title = binding.webview.title
                if (!TextUtils.isEmpty(title)) {
                    binding.url.setText(title)
                }
            }
        }

        binding.url.setOnKeyListener { _, keyCode, _ ->
            if (keyCode == KeyEvent.KEYCODE_ENTER) {
                Utils.hideKeyboard(this@MainActivity)
                val input = binding.url.text.toString().trim()
                val url = viewModel.resolveUrl(input)
                binding.webview.loadUrl(url)
                binding.url.clearFocus()
                true
            } else {
                false
            }
        }

        binding.back.isEnabled = false
        binding.forward.isEnabled = false

        binding.back.setOnClickListener { pageGoBack() }
        binding.forward.setOnClickListener { pageGoForward() }
        binding.refresh.setOnClickListener { binding.webview.url?.let { binding.webview.loadUrl(it) } }
        binding.home.setOnClickListener { binding.webview.loadUrl(viewModel.getHomepage()) }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (binding.webview.canGoBack()) {
                    pageGoBack()
                } else {
                    confirmExit()
                }
            }
        })
    }

    override fun onDestroy() {
        binding.webview.destroy()
        super.onDestroy()
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun initWebView() {
        updateAddressBarHint()
        binding.webview.webViewClient = webViewClient
        binding.webview.webChromeClient = webChromeClient
        binding.webview.settings.javaScriptEnabled = true
        binding.webview.loadUrl(viewModel.getHomepage())
        binding.webview.settings.setSupportMultipleWindows(true)

        if (WebViewFeature.isFeatureSupported(WebViewFeature.ALGORITHMIC_DARKENING)) {
            WebSettingsCompat.setAlgorithmicDarkeningAllowed(binding.webview.settings, true)
        }
    }

    override fun onResume() {
        super.onResume()
        Log.d("MainActivity", "onResume")
        updateAddressBarHint()

        val url = intent.data?.toString() ?: intent.getStringExtra("url")
        if (url != null) {
            openWebpage(url)
            intent.data = null
            intent.removeExtra("url")
        }
    }

    private fun updateNavigationButtons() {
        binding.back.isEnabled = binding.webview.canGoBack()
        binding.forward.isEnabled = binding.webview.canGoForward()
    }

    fun pageGoBack(): Boolean {
        binding.webview.goBack()
        return true
    }

    fun pageGoForward(): Boolean {
        binding.webview.goForward()
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
                    binding.webview.loadUrl(viewModel.getHomepage())
                    true
                }
                else -> {
                    val index = item.itemId
                    if (index != tabManager.activeTabIndex && index in tabManager.tabs.indices) {
                        saveCurrentTabState()
                        tabManager.switchTo(index)
                        val tab = tabManager.activeTab
                        if (tab.url.isNotEmpty()) {
                            binding.webview.loadUrl(tab.url)
                        } else {
                            binding.webview.loadUrl(viewModel.getHomepage())
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
            url = binding.webview.url ?: "",
            title = binding.webview.title ?: ""
        )
    }

    private fun updateTabCount() {
        binding.multiTabButton.text = viewModel.tabManager.tabCount.toString()
    }

    private fun updateAddressBarHint() {
        val engine = viewModel.getSearchEngine()
        binding.url.hint = getString(R.string.address_bar_hint, engine.displayName)
    }

    fun openWebpage(url: String?) {
        if (url != null) binding.webview.loadUrl(url)
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
