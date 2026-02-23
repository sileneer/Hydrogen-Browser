package com.sileneer.hydrogenbrowser.ui.browser

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.net.http.SslError
import android.view.ViewGroup
import android.webkit.SslErrorHandler
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.webkit.WebSettingsCompat
import androidx.webkit.WebViewFeature
import com.sileneer.hydrogenbrowser.R

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun BrowserScreen(
    viewModel: BrowserViewModel,
    onNavigateToSettings: () -> Unit
) {
    val currentUrl by viewModel.currentUrl.collectAsStateWithLifecycle()
    val pageTitle by viewModel.pageTitle.collectAsStateWithLifecycle()
    val loadingProgress by viewModel.loadingProgress.collectAsStateWithLifecycle()
    val canGoBack by viewModel.canGoBack.collectAsStateWithLifecycle()
    val canGoForward by viewModel.canGoForward.collectAsStateWithLifecycle()
    val tabCount by viewModel.tabCount.collectAsStateWithLifecycle()

    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    var showExitDialog by remember { mutableStateOf(false) }
    var showTabMenu by remember { mutableStateOf(false) }
    var showMoreMenu by remember { mutableStateOf(false) }
    var addressBarFocused by remember { mutableStateOf(false) }
    var addressBarValue by remember { mutableStateOf(TextFieldValue("")) }
    val addressBarFocusRequester = remember { FocusRequester() }

    // Update address bar text when not focused
    LaunchedEffect(pageTitle, currentUrl, addressBarFocused) {
        if (!addressBarFocused) {
            val displayText = pageTitle.ifEmpty { currentUrl }
            addressBarValue = TextFieldValue(displayText)
        }
    }

    val webView = remember {
        WebView(context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            settings.javaScriptEnabled = true
            settings.setSupportMultipleWindows(true)

            try {
                if (WebViewFeature.isFeatureSupported(WebViewFeature.ALGORITHMIC_DARKENING)) {
                    WebSettingsCompat.setAlgorithmicDarkeningAllowed(settings, true)
                }
            } catch (_: UnsupportedOperationException) {
                // Not supported in this environment
            }

            webViewClient = object : WebViewClient() {
                override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                    viewModel.onPageStarted(url)
                    viewModel.updateNavigation(
                        canBack = view?.canGoBack() ?: false,
                        canForward = view?.canGoForward() ?: false
                    )
                }

                override fun onPageFinished(view: WebView?, url: String?) {
                    viewModel.onPageFinished(url, view?.title)
                    viewModel.updateNavigation(
                        canBack = view?.canGoBack() ?: false,
                        canForward = view?.canGoForward() ?: false
                    )
                    viewModel.saveCurrentTabState(
                        url = view?.url ?: "",
                        title = view?.title ?: ""
                    )
                }

                override fun onReceivedSslError(
                    view: WebView?,
                    handler: SslErrorHandler?,
                    error: SslError?
                ) {
                    handler?.cancel()
                }

                override fun onReceivedError(
                    view: WebView?,
                    request: WebResourceRequest?,
                    error: WebResourceError?
                ) {
                    if (request?.isForMainFrame == true) {
                        viewModel.onError(error?.description?.toString())
                    }
                }
            }

            webChromeClient = object : WebChromeClient() {
                override fun onProgressChanged(view: WebView?, newProgress: Int) {
                    viewModel.onProgressChanged(newProgress)
                }
            }

            loadUrl(viewModel.getHomepage())
        }
    }

    // Handle intent URLs
    val activity = LocalActivity.current as? ComponentActivity
    LaunchedEffect(activity?.intent) {
        val intent = activity?.intent
        val url = intent?.data?.toString() ?: intent?.getStringExtra("url")
        if (url != null) {
            webView.loadUrl(url)
            intent?.data = null
            intent?.removeExtra("url")
        }
    }

    // Clean up WebView on dispose
    DisposableEffect(Unit) {
        onDispose {
            webView.destroy()
        }
    }

    // Back handler
    BackHandler {
        if (webView.canGoBack()) {
            webView.goBack()
        } else {
            showExitDialog = true
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Address bar — surface extends behind status bar
        Surface(
            color = MaterialTheme.colorScheme.surface
        ) {
            Column {
                Spacer(Modifier.windowInsetsTopHeight(WindowInsets.statusBars))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .padding(horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Search field with refresh trailing icon
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(36.dp)
                            .padding(horizontal = 4.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .background(MaterialTheme.colorScheme.surfaceContainerHighest),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 10.dp, end = 2.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.width(6.dp))
                            Box(modifier = Modifier.weight(1f)) {
                                if (addressBarValue.text.isEmpty() && !addressBarFocused) {
                                    Text(
                                        text = stringResource(
                                            R.string.address_bar_hint,
                                            viewModel.getSearchEngine().displayName
                                        ),
                                        style = TextStyle(
                                            fontSize = 14.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        ),
                                        maxLines = 1
                                    )
                                }
                                BasicTextField(
                                    value = addressBarValue,
                                    onValueChange = { addressBarValue = it },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .focusRequester(addressBarFocusRequester)
                                        .onFocusChanged { focusState ->
                                            addressBarFocused = focusState.isFocused
                                            if (focusState.isFocused) {
                                                val urlText = currentUrl
                                                addressBarValue = TextFieldValue(
                                                    text = urlText,
                                                    selection = TextRange(0, urlText.length)
                                                )
                                            }
                                        },
                                    singleLine = true,
                                    textStyle = TextStyle(
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    ),
                                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Go),
                                    keyboardActions = KeyboardActions(
                                        onGo = {
                                            val input = addressBarValue.text.trim()
                                            val url = viewModel.resolveUrl(input)
                                            webView.loadUrl(url)
                                            focusManager.clearFocus()
                                        }
                                    )
                                )
                            }
                            // Refresh button inside address bar
                            IconButton(
                                onClick = { webView.url?.let { webView.loadUrl(it) } },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    // Tab count button
                    Box {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                                .border(
                                    width = 1.5.dp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    shape = RoundedCornerShape(6.dp)
                                )
                                .clickable { showTabMenu = true },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = tabCount.toString(),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        DropdownMenu(
                            expanded = showTabMenu,
                            onDismissRequest = { showTabMenu = false }
                        ) {
                            viewModel.tabManager.tabs.forEachIndexed { index, tab ->
                                val title = if (index == viewModel.tabManager.activeTabIndex) {
                                    "\u25B6 ${tab.displayTitle}"
                                } else {
                                    tab.displayTitle
                                }
                                DropdownMenuItem(
                                    text = { Text(title) },
                                    onClick = {
                                        showTabMenu = false
                                        if (index != viewModel.tabManager.activeTabIndex) {
                                            viewModel.saveCurrentTabState(
                                                url = webView.url ?: "",
                                                title = webView.title ?: ""
                                            )
                                            viewModel.saveWebViewState(webView)
                                            viewModel.switchTab(index)
                                            val activeTab = viewModel.tabManager.activeTab
                                            if (!viewModel.restoreWebViewState(webView)) {
                                                if (activeTab.url.isNotEmpty()) {
                                                    webView.loadUrl(activeTab.url)
                                                } else {
                                                    webView.loadUrl(viewModel.getHomepage())
                                                }
                                            }
                                        }
                                    }
                                )
                            }
                            HorizontalDivider()
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.new_tab_action)) },
                                onClick = {
                                    showTabMenu = false
                                    viewModel.saveCurrentTabState(
                                        url = webView.url ?: "",
                                        title = webView.title ?: ""
                                    )
                                    viewModel.saveWebViewState(webView)
                                    viewModel.addTab()
                                    webView.loadUrl(viewModel.getHomepage())
                                }
                            )
                        }
                    }

                    // More menu button
                    Box {
                        IconButton(
                            onClick = { showMoreMenu = true },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        DropdownMenu(
                            expanded = showMoreMenu,
                            onDismissRequest = { showMoreMenu = false }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(
                                    onClick = {
                                        showMoreMenu = false
                                        webView.goBack()
                                    },
                                    enabled = canGoBack
                                ) {
                                    Icon(
                                        Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = stringResource(R.string.menu_back)
                                    )
                                }
                                IconButton(
                                    onClick = {
                                        showMoreMenu = false
                                        webView.goForward()
                                    },
                                    enabled = canGoForward
                                ) {
                                    Icon(
                                        Icons.AutoMirrored.Filled.ArrowForward,
                                        contentDescription = stringResource(R.string.menu_forward)
                                    )
                                }
                                IconButton(
                                    onClick = {
                                        showMoreMenu = false
                                        webView.loadUrl(viewModel.getHomepage())
                                    }
                                ) {
                                    Icon(
                                        Icons.Default.Home,
                                        contentDescription = stringResource(R.string.menu_home)
                                    )
                                }
                            }
                            HorizontalDivider()
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.settings)) },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.Settings,
                                        contentDescription = null
                                    )
                                },
                                onClick = {
                                    showMoreMenu = false
                                    onNavigateToSettings()
                                }
                            )
                        }
                    }
                }
            }
        }

        // Progress bar
        if (loadingProgress in 0f..1f) {
            LinearProgressIndicator(
                progress = { loadingProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp),
                color = MaterialTheme.colorScheme.primary,
            )
        }

        // WebView
        AndroidView(
            factory = { webView },
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        )
    }

    // Exit confirmation dialog
    if (showExitDialog) {
        AlertDialog(
            onDismissRequest = { showExitDialog = false },
            title = { Text(stringResource(R.string.exit_warning_title)) },
            text = { Text(stringResource(R.string.exit_warning_message)) },
            confirmButton = {
                TextButton(onClick = { activity?.finish() }) {
                    Text(stringResource(R.string.yes))
                }
            },
            dismissButton = {
                TextButton(onClick = { showExitDialog = false }) {
                    Text(stringResource(R.string.no))
                }
            }
        )
    }
}
