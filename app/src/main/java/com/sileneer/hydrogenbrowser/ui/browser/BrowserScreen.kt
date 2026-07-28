package com.sileneer.hydrogenbrowser.ui.browser

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.view.ViewGroup
import android.webkit.WebBackForwardList
import android.widget.FrameLayout
import android.widget.Toast
import android.webkit.WebView
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.CardDefaults
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sileneer.hydrogenbrowser.R
import com.sileneer.hydrogenbrowser.common.UrlUtils
import com.sileneer.hydrogenbrowser.ui.common.MoveToFolderDialog
import kotlinx.coroutines.launch
import androidx.core.view.isEmpty

@Composable
fun BrowserScreen(
    viewModel: BrowserViewModel,
    onNavigateToSettings: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToBookmarks: () -> Unit
) {
    val currentUrl by viewModel.currentUrl.collectAsStateWithLifecycle()
    val pageTitle by viewModel.pageTitle.collectAsStateWithLifecycle()
    val loadingProgress by viewModel.loadingProgress.collectAsStateWithLifecycle()
    val canGoBack by viewModel.canGoBack.collectAsStateWithLifecycle()
    val canGoForward by viewModel.canGoForward.collectAsStateWithLifecycle()
    val tabCount by viewModel.tabCount.collectAsStateWithLifecycle()
    val activeTabId by viewModel.activeTabId.collectAsStateWithLifecycle()
    val favicon by viewModel.favicon.collectAsStateWithLifecycle()
    val errorMessage by viewModel.errorMessage.collectAsStateWithLifecycle()
    val findInPageActive by viewModel.findInPageActive.collectAsStateWithLifecycle()
    val isBookmarked by viewModel.isCurrentPageBookmarked.collectAsStateWithLifecycle()
    val showTabGrid by viewModel.showTabGrid.collectAsStateWithLifecycle()

    val context = LocalContext.current

    var showExitDialog by remember { mutableStateOf(false) }
    var showMoreMenu by remember { mutableStateOf(false) }
    var addressBarFocused by remember { mutableStateOf(false) }
    var overlayTextValue by remember { mutableStateOf(TextFieldValue("")) }
    val overlayFocusRequester = remember { FocusRequester() }
    val snackbarHostState = remember { SnackbarHostState() }
    val bookmarkAddedToLabel = stringResource(R.string.bookmark_added_to)
    val bookmarkRemovedLabel = stringResource(R.string.bookmark_removed)
    val changeFolderLabel = stringResource(R.string.change_folder)
    val scope = rememberCoroutineScope()
    var movingBookmarkId by remember { mutableStateOf<Long?>(null) }

    val currentWebView = remember(activeTabId) {
        viewModel.getActiveWebView()
    }

    val activity = LocalActivity.current as? ComponentActivity
    var intentHandled by remember { mutableStateOf(false) }
    LaunchedEffect(activity?.intent) {
        if (!intentHandled) {
            val intent = activity?.intent
            val url = intent?.data?.toString() ?: intent?.getStringExtra("url")
            if (url != null) {
                viewModel.getActiveWebView()?.loadUrl(url)
                intentHandled = true
            }
        }
    }

    BackHandler {
        when {
            showTabGrid -> viewModel.dismissTabGrid()
            findInPageActive -> viewModel.dismissFindInPage()
            addressBarFocused -> addressBarFocused = false
            else -> {
                val activeWebView = viewModel.getActiveWebView()
                if (activeWebView != null && activeWebView.canGoBack()) {
                    activeWebView.goBack()
                } else {
                    showExitDialog = true
                }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // ---- Main content ----
        Column(modifier = Modifier.fillMaxSize()) {
            // Status bar spacer
            Spacer(
                Modifier
                    .windowInsetsTopHeight(WindowInsets.statusBars)
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
            )

            // Find in page bar
            if (findInPageActive) {
                FindInPageBar(
                    onSearch = { viewModel.findInPage(it) },
                    onNext = { viewModel.findNext(true) },
                    onPrevious = { viewModel.findNext(false) },
                    onDismiss = { viewModel.dismissFindInPage() }
                )
            }

            // WebView + New Tab Page overlay
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
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
                        if (currentWebView != null && (container.isEmpty() || container.getChildAt(0) !== currentWebView)) {
                            container.removeAllViews()
                            (currentWebView.parent as? ViewGroup)?.removeView(currentWebView)
                            container.addView(currentWebView)
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )

                // Error overlay for failed page loads
                if (errorMessage != null && loadingProgress < 0f) {
                    ErrorOverlay(
                        message = errorMessage!!,
                        onRetry = {
                            viewModel.clearError()
                            viewModel.getActiveWebView()?.let { wv ->
                                wv.url?.let { wv.loadUrl(it) }
                            }
                        }
                    )
                }

                if (currentUrl.isEmpty() && loadingProgress < 0f && errorMessage == null) {
                    NewTabPage(
                        onSearch = {
                            overlayTextValue = TextFieldValue("")
                            addressBarFocused = true
                        },
                        onNavigate = { url ->
                            viewModel.getActiveWebView()?.loadUrl(url)
                        },
                        onHistoryClick = onNavigateToHistory,
                        onBookmarksClick = onNavigateToBookmarks,
                        prefs = viewModel.prefs,
                        searchEngineDisplayName = viewModel.getSearchEngine().displayName
                    )
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

            // Bottom bar
            BottomBar(
                currentUrl = currentUrl,
                searchEngineDisplayName = viewModel.getSearchEngine().displayName,
                canGoBack = canGoBack,
                canGoForward = canGoForward,
                tabCount = tabCount,
                showMoreMenu = showMoreMenu,
                onAddressBarClick = {
                    overlayTextValue = TextFieldValue("")
                    addressBarFocused = true
                },
                onRefresh = {
                    viewModel.getActiveWebView()?.let { wv ->
                        wv.url?.let { wv.loadUrl(it) }
                    }
                },
                onGoBack = { viewModel.getActiveWebView()?.goBack() },
                onGoForward = { viewModel.getActiveWebView()?.goForward() },
                backForwardList = currentWebView?.copyBackForwardList(),
                onGoToHistoryIndex = { index ->
                    val wv = viewModel.getActiveWebView() ?: return@BottomBar
                    val list = wv.copyBackForwardList()
                    val steps = index - list.currentIndex
                    wv.goBackOrForward(steps)
                },
                onGoHome = {
                    if (viewModel.prefs.homeButtonGoesToStartPage) {
                        viewModel.goToStartPage()
                    } else {
                        viewModel.getActiveWebView()?.loadUrl(viewModel.getHomepage())
                    }
                },
                onTabGridOpen = { viewModel.openTabGrid() },
                onMoreMenuToggle = { showMoreMenu = it },
                onNavigateToHistory = onNavigateToHistory,
                onNavigateToSettings = onNavigateToSettings,
                onNavigateToBookmarks = onNavigateToBookmarks,
                onFindInPage = { viewModel.showFindInPage() },
                isBookmarked = isBookmarked,
                onToggleBookmark = {
                    if (isBookmarked) {
                        viewModel.removeBookmarkForCurrentPage()
                        scope.launch {
                            snackbarHostState.showSnackbar(bookmarkRemovedLabel, duration = SnackbarDuration.Short)
                        }
                    } else {
                        val rootLabel = context.getString(R.string.root_folder)
                        viewModel.addBookmark { id, folderName ->
                            scope.launch {
                                val result = snackbarHostState.showSnackbar(
                                    message = bookmarkAddedToLabel.format(folderName ?: rootLabel),
                                    actionLabel = changeFolderLabel,
                                    duration = SnackbarDuration.Short
                                )
                                if (result == SnackbarResult.ActionPerformed) {
                                    movingBookmarkId = id
                                }
                            }
                        }
                    }
                },
                activeTabIndex = viewModel.tabManager.activeTabIndex,
                tabTotalCount = viewModel.tabManager.tabCount,
                onSwipeToTab = { viewModel.switchTab(it) }
            )
        }

        // Tab grid overlay
        if (showTabGrid) {
            TabGridOverlay(
                // Snapshot copy: tabManager.tabs is a live MutableList of non-observable Tabs,
                // so passing it directly lets Compose skip the grid after a close.
                tabs = viewModel.tabManager.tabs.toList(),
                activeTabId = activeTabId,
                onSelectTab = { tabId ->
                    val idx = viewModel.tabManager.indexOfId(tabId)
                    if (idx >= 0) {
                        viewModel.switchTab(idx)
                        viewModel.dismissTabGrid()
                    }
                },
                onCloseTab = { tabId ->
                    val idx = viewModel.tabManager.indexOfId(tabId)
                    if (idx >= 0) viewModel.closeTab(idx)
                },
                onAddTab = {
                    viewModel.addTab()
                    viewModel.dismissTabGrid()
                },
                onDismiss = { viewModel.dismissTabGrid() }
            )
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }

    // Move bookmark to folder dialog
    movingBookmarkId?.let { bookmarkId ->
        val allFolders by viewModel.allFolders.collectAsStateWithLifecycle()
        MoveToFolderDialog(
            folders = allFolders,
            onPick = { folderId ->
                viewModel.moveBookmark(bookmarkId, folderId)
                movingBookmarkId = null
            },
            onDismiss = { movingBookmarkId = null }
        )
    }

    // ---- Address bar overlay ---- (AddressBarOverlay is itself a fillMaxSize Box)
    if (addressBarFocused) {
        AddressBarOverlay(
            currentUrl = currentUrl,
            pageTitle = pageTitle,
            favicon = favicon,
            searchEngineDisplayName = viewModel.getSearchEngine().displayName,
            overlayTextValue = overlayTextValue,
            overlayFocusRequester = overlayFocusRequester,
            onOverlayTextChange = { overlayTextValue = it },
            onDismiss = { addressBarFocused = false },
            onNavigate = { input ->
                val url = viewModel.resolveUrl(input)
                viewModel.getActiveWebView()?.loadUrl(url)
                addressBarFocused = false
            },
            onEditUrl = {
                overlayTextValue = TextFieldValue(
                    text = currentUrl,
                    selection = TextRange(currentUrl.length)
                )
            }
        )
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

// ---- Extracted components ----

@Composable
private fun ErrorOverlay(message: String, onRetry: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = stringResource(R.string.webview_error, message),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(16.dp))
            TextButton(onClick = onRetry) {
                Text(stringResource(R.string.retry))
            }
        }
    }
}

@Composable
private fun FindInPageBar(
    onSearch: (String) -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onDismiss: () -> Unit
) {
    var query by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Surface(color = MaterialTheme.colorScheme.surfaceContainer) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(20.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                if (query.isEmpty()) {
                    Text(
                        text = stringResource(R.string.find_in_page),
                        style = TextStyle(
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
                BasicTextField(
                    value = query,
                    onValueChange = {
                        query = it
                        if (it.isNotEmpty()) onSearch(it)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester),
                    singleLine = true,
                    textStyle = TextStyle(
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(
                        onSearch = { if (query.isNotEmpty()) onSearch(query) }
                    )
                )
            }
            IconButton(onClick = onPrevious) {
                Icon(Icons.Default.KeyboardArrowUp, contentDescription = stringResource(R.string.find_previous), modifier = Modifier.size(20.dp))
            }
            IconButton(onClick = onNext) {
                Icon(Icons.Default.KeyboardArrowDown, contentDescription = stringResource(R.string.find_next), modifier = Modifier.size(20.dp))
            }
            IconButton(onClick = onDismiss) {
                Icon(Icons.Default.Close, contentDescription = stringResource(R.string.close_tab), modifier = Modifier.size(20.dp))
            }
        }
    }
}

@Composable
private fun BottomBar(
    currentUrl: String,
    searchEngineDisplayName: String,
    canGoBack: Boolean,
    canGoForward: Boolean,
    tabCount: Int,
    showMoreMenu: Boolean,
    onAddressBarClick: () -> Unit,
    onRefresh: () -> Unit,
    onGoBack: () -> Unit,
    onGoForward: () -> Unit,
    backForwardList: WebBackForwardList?,
    onGoToHistoryIndex: (Int) -> Unit,
    onGoHome: () -> Unit,
    onTabGridOpen: () -> Unit,
    onMoreMenuToggle: (Boolean) -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToBookmarks: () -> Unit,
    onFindInPage: () -> Unit,
    isBookmarked: Boolean,
    onToggleBookmark: () -> Unit,
    activeTabIndex: Int,
    tabTotalCount: Int,
    onSwipeToTab: (Int) -> Unit
) {
    val density = LocalDensity.current
    val minDragPx = with(density) { 50.dp.toPx() }

    Surface(color = MaterialTheme.colorScheme.surfaceContainer) {
        Column {
            // Address pill + tab counter + more button (with swipe-to-switch)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .pointerInput(activeTabIndex, tabTotalCount) {
                        // dragAmount is a single pointer-move delta, so it must be accumulated;
                        // `switched` latches one tab hop per gesture.
                        var dragTotal = 0f
                        var switched = false
                        detectHorizontalDragGestures(
                            onDragStart = { dragTotal = 0f; switched = false },
                            onDragEnd = { dragTotal = 0f; switched = false },
                            onDragCancel = { dragTotal = 0f; switched = false }
                        ) { _, dragAmount ->
                            dragTotal += dragAmount
                            if (!switched) {
                                if (dragTotal < -minDragPx && activeTabIndex < tabTotalCount - 1) {
                                    switched = true
                                    onSwipeToTab(activeTabIndex + 1)
                                } else if (dragTotal > minDragPx && activeTabIndex > 0) {
                                    switched = true
                                    onSwipeToTab(activeTabIndex - 1)
                                }
                            }
                        }
                    }
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    AddressPill(
                        currentUrl = currentUrl,
                        searchEngineDisplayName = searchEngineDisplayName,
                        onClick = onAddressBarClick,
                        onRefresh = onRefresh
                    )
                }
                // Tab counter button — opens tab grid
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .border(1.5.dp, MaterialTheme.colorScheme.onSurfaceVariant, RoundedCornerShape(8.dp))
                        .clickable { onTabGridOpen() },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = tabCount.toString(),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                // More menu button
                Box {
                    IconButton(onClick = { onMoreMenuToggle(true) }) {
                        Icon(Icons.Default.MoreVert, contentDescription = stringResource(R.string.more_options), modifier = Modifier.size(22.dp))
                    }
                    DropdownMenu(expanded = showMoreMenu, onDismissRequest = { onMoreMenuToggle(false) }) {
                        // Navigation row: back, forward, home, bookmark
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            NavHistoryButton(
                                icon = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(R.string.menu_back),
                                enabled = canGoBack,
                                onClick = { onMoreMenuToggle(false); onGoBack() },
                                backForwardList = backForwardList,
                                isBack = true,
                                onGoToIndex = { onMoreMenuToggle(false); onGoToHistoryIndex(it) }
                            )
                            NavHistoryButton(
                                icon = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = stringResource(R.string.menu_forward),
                                enabled = canGoForward,
                                onClick = { onMoreMenuToggle(false); onGoForward() },
                                backForwardList = backForwardList,
                                isBack = false,
                                onGoToIndex = { onMoreMenuToggle(false); onGoToHistoryIndex(it) }
                            )
                            IconButton(onClick = { onMoreMenuToggle(false); onGoHome() }) {
                                Icon(Icons.Default.Home, contentDescription = stringResource(R.string.menu_home), modifier = Modifier.size(22.dp))
                            }
                            IconButton(onClick = { onMoreMenuToggle(false); onToggleBookmark() }) {
                                Icon(
                                    painter = painterResource(
                                        if (isBookmarked) R.drawable.ic_bookmark_filled
                                        else R.drawable.ic_bookmark_border
                                    ),
                                    contentDescription = stringResource(
                                        if (isBookmarked) R.string.remove_bookmark else R.string.bookmark_this_page
                                    ),
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                        HorizontalDivider()
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.bookmarks)) },
                            leadingIcon = { Icon(painterResource(R.drawable.ic_bookmark_border), contentDescription = null) },
                            onClick = { onMoreMenuToggle(false); onNavigateToBookmarks() }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.find_in_page)) },
                            leadingIcon = { Icon(painterResource(R.drawable.ic_find_in_page), contentDescription = null) },
                            onClick = { onMoreMenuToggle(false); onFindInPage() }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.history)) },
                            leadingIcon = { Icon(painterResource(R.drawable.ic_history), contentDescription = null) },
                            onClick = { onMoreMenuToggle(false); onNavigateToHistory() }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.settings)) },
                            leadingIcon = { Icon(Icons.Default.Settings, contentDescription = null) },
                            onClick = { onMoreMenuToggle(false); onNavigateToSettings() }
                        )
                    }
                }
            }

            Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.ime.union(WindowInsets.navigationBars)))
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun NavHistoryButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    enabled: Boolean,
    onClick: () -> Unit,
    backForwardList: WebBackForwardList?,
    isBack: Boolean,
    onGoToIndex: (Int) -> Unit
) {
    var showHistory by remember { mutableStateOf(false) }
    val alpha = if (enabled) 1f else 0.38f

    Box {
        Box(
            modifier = Modifier
                .size(48.dp)
                .combinedClickable(
                    enabled = enabled,
                    onClick = onClick,
                    onLongClick = { showHistory = true }
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                icon,
                contentDescription = contentDescription,
                modifier = Modifier
                    .size(22.dp)
                    .alpha(alpha)
            )
        }

        DropdownMenu(
            expanded = showHistory,
            onDismissRequest = { showHistory = false }
        ) {
            backForwardList?.let { list ->
                val currentIndex = list.currentIndex
                val items = if (isBack) {
                    (currentIndex - 1 downTo 0).map { i -> i to list.getItemAtIndex(i) }
                } else {
                    ((currentIndex + 1) until list.size).map { i -> i to list.getItemAtIndex(i) }
                }
                items.forEach { (index, item) ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = item.title?.takeIf { it.isNotBlank() } ?: item.url,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                        onClick = {
                            onGoToIndex(index)
                            showHistory = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun AddressPill(
    currentUrl: String,
    searchEngineDisplayName: String,
    onClick: () -> Unit,
    onRefresh: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            .clickable(onClick = onClick),
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
            Text(
                text = if (currentUrl.isNotEmpty())
                    UrlUtils.simplifyForDisplay(currentUrl)
                else stringResource(R.string.address_bar_hint, searchEngineDisplayName),
                style = TextStyle(
                    fontSize = 14.sp,
                    color = if (currentUrl.isNotEmpty())
                        MaterialTheme.colorScheme.onSurface
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 10.dp)
            )
            if (currentUrl.isNotEmpty()) {
                IconButton(onClick = onRefresh, modifier = Modifier.size(36.dp)) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = stringResource(R.string.refresh),
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun AddressBarOverlay(
    currentUrl: String,
    pageTitle: String,
    favicon: Bitmap?,
    searchEngineDisplayName: String,
    overlayTextValue: TextFieldValue,
    overlayFocusRequester: FocusRequester,
    onOverlayTextChange: (TextFieldValue) -> Unit,
    onDismiss: () -> Unit,
    onNavigate: (String) -> Unit,
    onEditUrl: () -> Unit
) {
    LaunchedEffect(Unit) {
        overlayFocusRequester.requestFocus()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) { /* consume touch */ }
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Spacer(
                Modifier
                    .windowInsetsTopHeight(WindowInsets.statusBars)
                    .fillMaxWidth()
            )

            // Address bar row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 8.dp)
            ) {
                IconButton(onClick = onDismiss) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.menu_back))
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(24.dp))
                        .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    if (overlayTextValue.text.isEmpty()) {
                        Text(
                            text = stringResource(R.string.address_bar_hint, searchEngineDisplayName),
                            style = TextStyle(fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurfaceVariant),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    BasicTextField(
                        value = overlayTextValue,
                        onValueChange = onOverlayTextChange,
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(overlayFocusRequester),
                        singleLine = true,
                        textStyle = TextStyle(fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface),
                        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Go),
                        keyboardActions = KeyboardActions(
                            onGo = {
                                val input = overlayTextValue.text.trim()
                                if (input.isNotEmpty()) onNavigate(input)
                                else onDismiss()
                            }
                        )
                    )
                }

                IconButton(onClick = { onOverlayTextChange(TextFieldValue("")) }) {
                    Icon(Icons.Default.Close, contentDescription = stringResource(R.string.clear_text), modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            // Current page info card
            if (currentUrl.isNotEmpty()) {
                PageInfoCard(
                    currentUrl = currentUrl,
                    pageTitle = pageTitle,
                    favicon = favicon,
                    onEditUrl = onEditUrl
                )
            }

            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun PageInfoCard(
    currentUrl: String,
    pageTitle: String,
    favicon: Bitmap?,
    onEditUrl: () -> Unit
) {
    val context = LocalContext.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // Favicon
            if (favicon != null) {
                Image(
                    bitmap = favicon.asImageBitmap(),
                    contentDescription = null,
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceContainerHighest),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = pageTitle.ifEmpty { stringResource(R.string.new_tab) },
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = currentUrl,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Share
            IconButton(
                onClick = {
                    val sendIntent = Intent().apply {
                        action = Intent.ACTION_SEND
                        putExtra(Intent.EXTRA_TEXT, currentUrl)
                        type = "text/plain"
                    }
                    context.startActivity(Intent.createChooser(sendIntent, null))
                },
                modifier = Modifier.size(36.dp)
            ) {
                Icon(Icons.Default.Share, contentDescription = stringResource(R.string.share), modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            // Copy
            IconButton(
                onClick = {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    clipboard.setPrimaryClip(ClipData.newPlainText("url", currentUrl))
                    Toast.makeText(context, context.getString(R.string.link_copied), Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.size(36.dp)
            ) {
                Icon(painterResource(R.drawable.ic_content_copy), contentDescription = stringResource(R.string.copy_link), modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            // Edit
            IconButton(onClick = onEditUrl, modifier = Modifier.size(36.dp)) {
                Icon(Icons.Default.Edit, contentDescription = stringResource(R.string.edit_url), modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
