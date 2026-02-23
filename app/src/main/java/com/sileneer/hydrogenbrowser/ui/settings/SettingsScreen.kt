package com.sileneer.hydrogenbrowser.ui.settings

import android.util.Patterns
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.sileneer.hydrogenbrowser.R
import com.sileneer.hydrogenbrowser.common.SearchEngine
import com.sileneer.hydrogenbrowser.ui.browser.BrowserViewModel
import com.sileneer.hydrogenbrowser.ui.navigation.Routes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    browserViewModel: BrowserViewModel
) {
    val prefs = browserViewModel.prefs
    val context = LocalContext.current

    var showSearchEngineSheet by remember { mutableStateOf(false) }
    var showHomepageSheet by remember { mutableStateOf(false) }

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = { Text(stringResource(R.string.settings)) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null
                        )
                    }
                },
                scrollBehavior = scrollBehavior
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            // General group
            SettingsGroup {
                SettingsListItem(
                    icon = Icons.Default.Search,
                    title = stringResource(R.string.settings_search_engine),
                    subtitle = prefs.getSearchEngine().displayName,
                    onClick = { showSearchEngineSheet = true }
                )
                HorizontalDivider(modifier = Modifier.padding(start = 56.dp))
                SettingsListItem(
                    icon = Icons.Default.Home,
                    title = stringResource(R.string.settings_homepage),
                    subtitle = prefs.homepage,
                    onClick = { showHomepageSheet = true }
                )
                HorizontalDivider(modifier = Modifier.padding(start = 56.dp))
                SettingsListItem(
                    icon = Icons.Default.Info,
                    title = stringResource(R.string.settings_language),
                    onClick = { navController.navigate(Routes.LANGUAGE) }
                )
            }

            Spacer(Modifier.height(16.dp))

            // About group
            SettingsGroup {
                SettingsListItem(
                    icon = Icons.Default.Info,
                    title = stringResource(R.string.settings_about),
                    onClick = { navController.navigate(Routes.ABOUT) }
                )
            }

            Spacer(Modifier.height(16.dp))
        }
    }

    // Search Engine Bottom Sheet
    if (showSearchEngineSheet) {
        var selectedIndex by remember { mutableIntStateOf(prefs.searchEngineIndex) }
        val sheetState = rememberModalBottomSheetState()

        ModalBottomSheet(
            onDismissRequest = { showSearchEngineSheet = false },
            sheetState = sheetState
        ) {
            Text(
                text = stringResource(R.string.select_search_engine),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
            )
            SearchEngine.entries.forEachIndexed { index, engine ->
                ListItem(
                    headlineContent = { Text(engine.displayName) },
                    leadingContent = {
                        RadioButton(
                            selected = index == selectedIndex,
                            onClick = { selectedIndex = index }
                        )
                    },
                    modifier = Modifier.clickable { selectedIndex = index }
                )
            }
            Spacer(Modifier.height(8.dp))
            TextButton(
                onClick = {
                    prefs.searchEngineIndex = selectedIndex
                    showSearchEngineSheet = false
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 8.dp)
            ) {
                Text(stringResource(R.string.ok))
            }
            Spacer(Modifier.height(16.dp))
        }
    }

    // Homepage Bottom Sheet
    if (showHomepageSheet) {
        var homepageInput by remember { mutableStateOf("") }
        var errorMessage by remember { mutableStateOf<String?>(null) }
        val sheetState = rememberModalBottomSheetState()

        ModalBottomSheet(
            onDismissRequest = { showHomepageSheet = false },
            sheetState = sheetState
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 24.dp)
            ) {
                Text(
                    text = stringResource(R.string.homepage),
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.current_homepage, prefs.homepage),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedTextField(
                    value = homepageInput,
                    onValueChange = {
                        homepageInput = it
                        errorMessage = null
                    },
                    label = { Text(stringResource(R.string.input_new_homepage_url_below)) },
                    singleLine = true,
                    isError = errorMessage != null,
                    supportingText = errorMessage?.let { { Text(it) } },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                )
                Spacer(Modifier.height(8.dp))
                TextButton(
                    onClick = {
                        when {
                            homepageInput.isEmpty() -> {
                                errorMessage = context.getString(R.string.homepage_edit_error_empty)
                            }
                            !Patterns.WEB_URL.matcher(homepageInput).matches() -> {
                                errorMessage = context.getString(R.string.homepage_edit_error_invalid)
                            }
                            else -> {
                                prefs.homepage = homepageInput
                                showHomepageSheet = false
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.ok))
                }
                Spacer(Modifier.height(16.dp))
            }
        }
    }

}

@Composable
private fun SettingsGroup(content: @Composable () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        shape = MaterialTheme.shapes.large
    ) {
        content()
    }
}

@Composable
private fun SettingsListItem(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    onClick: () -> Unit
) {
    ListItem(
        headlineContent = { Text(title) },
        supportingContent = subtitle?.let {
            {
                Text(
                    text = it,
                    maxLines = 1,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        leadingContent = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        modifier = Modifier.clickable(onClick = onClick),
        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
    )
}
