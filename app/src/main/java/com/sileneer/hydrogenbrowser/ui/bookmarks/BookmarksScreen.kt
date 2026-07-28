package com.sileneer.hydrogenbrowser.ui.bookmarks

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sileneer.hydrogenbrowser.R
import com.sileneer.hydrogenbrowser.data.BookmarkEntry
import com.sileneer.hydrogenbrowser.ui.common.FaviconBadge
import com.sileneer.hydrogenbrowser.ui.common.ListSearchField
import com.sileneer.hydrogenbrowser.ui.common.MoveToFolderDialog
import com.sileneer.hydrogenbrowser.ui.common.SwipeDeleteBackground
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookmarksScreen(
    viewModel: BookmarkViewModel,
    onBack: () -> Unit,
    onNavigate: (String) -> Unit
) {
    val children by viewModel.children.collectAsStateWithLifecycle()
    val currentFolder by viewModel.currentFolder.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val isSearching by viewModel.isSearching.collectAsStateWithLifecycle()

    var showNewFolderDialog by remember { mutableStateOf(false) }
    var editingEntry by remember { mutableStateOf<BookmarkEntry?>(null) }
    var deletingFolder by remember { mutableStateOf<BookmarkEntry?>(null) }
    var contextMenuEntry by remember { mutableStateOf<BookmarkEntry?>(null) }
    var movingEntry by remember { mutableStateOf<BookmarkEntry?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    val deletedLabel = stringResource(R.string.bookmark_removed)
    val undoLabel = stringResource(R.string.undo)

    val deleteWithUndo: (BookmarkEntry) -> Unit = { entry ->
        viewModel.deleteEntry(entry)
        scope.launch {
            val result = snackbarHostState.showSnackbar(
                message = deletedLabel,
                actionLabel = undoLabel,
                duration = SnackbarDuration.Short
            )
            if (result == SnackbarResult.ActionPerformed) {
                viewModel.undoDelete(entry)
            }
        }
    }

    BackHandler {
        when {
            isSearching -> viewModel.toggleSearch()
            !viewModel.navigateUp() -> onBack()
        }
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = { Text(currentFolder?.title ?: stringResource(R.string.bookmarks)) },
                navigationIcon = {
                    IconButton(onClick = {
                        if (isSearching) viewModel.toggleSearch()
                        else if (!viewModel.navigateUp()) onBack()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.menu_back))
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.toggleSearch() }) {
                        Icon(Icons.Default.Search, contentDescription = stringResource(R.string.search_bookmarks))
                    }
                    IconButton(onClick = { showNewFolderDialog = true }) {
                        Icon(
                            painter = painterResource(R.drawable.ic_folder),
                            contentDescription = stringResource(R.string.new_folder)
                        )
                    }
                },
                scrollBehavior = scrollBehavior
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            AnimatedVisibility(visible = isSearching) {
                ListSearchField(
                    query = searchQuery,
                    onQueryChange = { viewModel.onSearchQueryChanged(it) },
                    placeholder = stringResource(R.string.search_bookmarks)
                )
            }

            if (children.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(if (currentFolder != null) R.string.empty_folder else R.string.no_bookmarks),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(children, key = { it.id }) { entry ->
                        val dismissState = rememberSwipeToDismissBoxState(
                            confirmValueChange = { dismissValue ->
                                when {
                                    dismissValue == SwipeToDismissBoxValue.Settled -> false
                                    // A folder takes its children with it, so it asks first
                                    // and the row stays put until the dialog is answered.
                                    entry.isFolder -> {
                                        deletingFolder = entry
                                        false
                                    }
                                    else -> {
                                        deleteWithUndo(entry)
                                        true
                                    }
                                }
                            }
                        )
                        SwipeToDismissBox(
                            state = dismissState,
                            backgroundContent = { SwipeDeleteBackground(dismissState.targetValue) }
                        ) {
                            BookmarkItem(
                                entry = entry,
                                onClick = {
                                    if (entry.isFolder) viewModel.navigateToFolder(entry)
                                    else entry.url?.let { url -> onNavigate(url) }
                                },
                                onLongClick = { contextMenuEntry = entry }
                            )
                        }
                    }
                }
            }
        }
    }

    // Context menu dialog
    contextMenuEntry?.let { entry ->
        AlertDialog(
            onDismissRequest = { contextMenuEntry = null },
            title = { Text(entry.title) },
            text = {
                Column {
                    TextButton(onClick = {
                        editingEntry = entry
                        contextMenuEntry = null
                    }, modifier = Modifier.fillMaxWidth()) {
                        Text(stringResource(R.string.edit), modifier = Modifier.fillMaxWidth())
                    }
                    TextButton(onClick = {
                        movingEntry = entry
                        contextMenuEntry = null
                    }, modifier = Modifier.fillMaxWidth()) {
                        Text(stringResource(R.string.move_to), modifier = Modifier.fillMaxWidth())
                    }
                    TextButton(onClick = {
                        if (entry.isFolder) deletingFolder = entry else deleteWithUndo(entry)
                        contextMenuEntry = null
                    }, modifier = Modifier.fillMaxWidth()) {
                        Text(stringResource(R.string.delete), color = MaterialTheme.colorScheme.error, modifier = Modifier.fillMaxWidth())
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { contextMenuEntry = null }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    // New folder dialog
    if (showNewFolderDialog) {
        var folderName by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showNewFolderDialog = false },
            title = { Text(stringResource(R.string.new_folder)) },
            text = {
                OutlinedTextField(
                    value = folderName,
                    onValueChange = { folderName = it },
                    label = { Text(stringResource(R.string.folder_name)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (folderName.isNotBlank()) {
                            viewModel.createFolder(folderName.trim())
                            showNewFolderDialog = false
                        }
                    },
                    enabled = folderName.isNotBlank()
                ) { Text(stringResource(R.string.save)) }
            },
            dismissButton = {
                TextButton(onClick = { showNewFolderDialog = false }) { Text(stringResource(R.string.cancel)) }
            }
        )
    }

    // Edit bookmark/folder dialog
    editingEntry?.let { entry ->
        var editTitle by remember { mutableStateOf(entry.title) }
        var editUrl by remember { mutableStateOf(entry.url ?: "") }
        AlertDialog(
            onDismissRequest = { editingEntry = null },
            title = { Text(stringResource(if (entry.isFolder) R.string.edit else R.string.edit_bookmark)) },
            text = {
                Column {
                    OutlinedTextField(
                        value = editTitle,
                        onValueChange = { editTitle = it },
                        label = { Text(stringResource(R.string.shortcut_name)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (!entry.isFolder) {
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(
                            value = editUrl,
                            onValueChange = { editUrl = it },
                            label = { Text(stringResource(R.string.shortcut_url)) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val updated = if (entry.isFolder) {
                            entry.copy(title = editTitle.trim())
                        } else {
                            entry.copy(title = editTitle.trim(), url = editUrl.trim())
                        }
                        viewModel.updateEntry(updated)
                        editingEntry = null
                    },
                    enabled = editTitle.isNotBlank() && (entry.isFolder || editUrl.isNotBlank())
                ) { Text(stringResource(R.string.save)) }
            },
            dismissButton = {
                TextButton(onClick = { editingEntry = null }) { Text(stringResource(R.string.cancel)) }
            }
        )
    }

    // Delete folder confirmation dialog
    deletingFolder?.let { folder ->
        AlertDialog(
            onDismissRequest = { deletingFolder = null },
            title = { Text(stringResource(R.string.delete_folder_title)) },
            text = { Text(stringResource(R.string.delete_folder_message)) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteEntry(folder)
                    deletingFolder = null
                }) { Text(stringResource(R.string.delete)) }
            },
            dismissButton = {
                TextButton(onClick = { deletingFolder = null }) { Text(stringResource(R.string.cancel)) }
            }
        )
    }

    // Move to folder dialog
    movingEntry?.let { entry ->
        val allFolders by viewModel.allFolders.collectAsStateWithLifecycle()
        MoveToFolderDialog(
            folders = allFolders.filter {
                it.id != entry.id && it.id !in allFolders.descendantsOf(entry.id)
            },
            onPick = { folderId ->
                viewModel.moveEntry(entry.id, folderId)
                movingEntry = null
            },
            onDismiss = { movingEntry = null }
        )
    }
}

/**
 * Ids under [rootId] at any depth. Offering one as a move target is a dead end:
 * BookmarkRepository.moveEntry detects the cycle and returns without updating, silently.
 */
private fun List<BookmarkEntry>.descendantsOf(rootId: Long): Set<Long> {
    val byParent = groupBy { it.parentId }
    val found = mutableSetOf<Long>()
    val pending = ArrayDeque(byParent[rootId].orEmpty())
    while (pending.isNotEmpty()) {
        val folder = pending.removeFirst()
        if (found.add(folder.id)) pending.addAll(byParent[folder.id].orEmpty())
    }
    return found
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun BookmarkItem(
    entry: BookmarkEntry,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        if (entry.isFolder) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_folder),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        } else {
            FaviconBadge(entry.favicon, painterResource(R.drawable.ic_bookmark_border))
        }

        Spacer(Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = entry.title,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (!entry.isFolder && entry.url != null) {
                Spacer(Modifier.height(2.dp))
                Text(
                    text = entry.url,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
