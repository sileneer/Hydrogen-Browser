package com.sileneer.hydrogenbrowser.ui.browser

import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsBottomHeight
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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sileneer.hydrogenbrowser.R

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
    val activeTabId by viewModel.activeTabId.collectAsStateWithLifecycle()

    val focusManager = LocalFocusManager.current

    var showExitDialog by remember { mutableStateOf(false) }
    var showTabMenu by remember { mutableStateOf(false) }
    var showMoreMenu by remember { mutableStateOf(false) }
    var addressBarFocused by remember { mutableStateOf(false) }
    var addressBarValue by remember { mutableStateOf(TextFieldValue("")) }
    val addressBarFocusRequester = remember { FocusRequester() }

    val currentWebView = remember(activeTabId) {
        viewModel.getActiveWebView()
    }

    LaunchedEffect(pageTitle, currentUrl, addressBarFocused) {
        if (!addressBarFocused) {
            val displayText = pageTitle.ifEmpty { currentUrl }
            addressBarValue = TextFieldValue(displayText)
        }
    }

    val activity = LocalActivity.current as? ComponentActivity
    LaunchedEffect(activity?.intent) {
        val intent = activity?.intent
        val url = intent?.data?.toString() ?: intent?.getStringExtra("url")
        if (url != null) {
            viewModel.getActiveWebView()?.loadUrl(url)
            intent?.data = null
            intent?.removeExtra("url")
        }
    }

    BackHandler {
        val activeWebView = viewModel.getActiveWebView()
        if (activeWebView != null && activeWebView.canGoBack()) {
            activeWebView.goBack()
        } else {
            showExitDialog = true
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Status bar spacer
        Spacer(
            Modifier
                .windowInsetsTopHeight(WindowInsets.statusBars)
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
        )

        // WebView — full bleed
        AndroidView(
            factory = { ctx ->
                FrameLayout(ctx).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                }
            },
            update = { container ->
                if (currentWebView != null && (container.childCount == 0 || container.getChildAt(0) !== currentWebView)) {
                    container.removeAllViews()
                    (currentWebView.parent as? ViewGroup)?.removeView(currentWebView)
                    container.addView(currentWebView)
                }
            },
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        )

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

        // Bottom bar
        Surface(color = MaterialTheme.colorScheme.surfaceContainer) {
            Column {
                // Address pill
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                        .clickable { addressBarFocusRequester.requestFocus() },
                    contentAlignment = Alignment.CenterStart
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 14.dp, end = 4.dp, top = 10.dp, bottom = 10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 10.dp)
                        ) {
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
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
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
                                        viewModel.getActiveWebView()?.loadUrl(url)
                                        focusManager.clearFocus()
                                    }
                                )
                            )
                        }
                        IconButton(
                            onClick = {
                                viewModel.getActiveWebView()?.let { wv ->
                                    wv.url?.let { wv.loadUrl(it) }
                                }
                            },
                            modifier = Modifier.size(36.dp)
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

                // Navigation row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp)
                        .padding(bottom = 4.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { viewModel.getActiveWebView()?.goBack() },
                        enabled = canGoBack
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.menu_back), modifier = Modifier.size(22.dp))
                    }
                    IconButton(
                        onClick = { viewModel.getActiveWebView()?.goForward() },
                        enabled = canGoForward
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = stringResource(R.string.menu_forward), modifier = Modifier.size(22.dp))
                    }
                    IconButton(
                        onClick = { viewModel.getActiveWebView()?.loadUrl(viewModel.getHomepage()) }
                    ) {
                        Icon(Icons.Default.Home, contentDescription = stringResource(R.string.menu_home), modifier = Modifier.size(22.dp))
                    }
                    Box {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .border(1.5.dp, MaterialTheme.colorScheme.onSurfaceVariant, RoundedCornerShape(8.dp))
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
                        DropdownMenu(expanded = showTabMenu, onDismissRequest = { showTabMenu = false }) {
                            viewModel.tabManager.tabs.forEachIndexed { index, tab ->
                                val title = if (index == viewModel.tabManager.activeTabIndex) "\u25B6 ${tab.displayTitle}" else tab.displayTitle
                                DropdownMenuItem(
                                    text = { Text(title) },
                                    onClick = {
                                        showTabMenu = false
                                        if (index != viewModel.tabManager.activeTabIndex) viewModel.switchTab(index)
                                    }
                                )
                            }
                            HorizontalDivider()
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.new_tab_action)) },
                                onClick = { showTabMenu = false; viewModel.addTab() }
                            )
                        }
                    }
                    Box {
                        IconButton(onClick = { showMoreMenu = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = null, modifier = Modifier.size(22.dp))
                        }
                        DropdownMenu(expanded = showMoreMenu, onDismissRequest = { showMoreMenu = false }) {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.settings)) },
                                leadingIcon = { Icon(Icons.Default.Settings, contentDescription = null) },
                                onClick = { showMoreMenu = false; onNavigateToSettings() }
                            )
                        }
                    }
                }

                Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
            }
        }
    }

    if (showExitDialog) {
        AlertDialog(
            onDismissRequest = { showExitDialog = false },
            title = { Text(stringResource(R.string.exit_warning_title)) },
            text = { Text(stringResource(R.string.exit_warning_message)) },
            confirmButton = {
                TextButton(onClick = { activity?.finish() }) { Text(stringResource(R.string.yes)) }
            },
            dismissButton = {
                TextButton(onClick = { showExitDialog = false }) { Text(stringResource(R.string.no)) }
            }
        )
    }
}
