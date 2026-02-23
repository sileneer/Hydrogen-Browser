package com.sileneer.hydrogenbrowser.ui.settings

import android.content.Intent
import android.util.Patterns
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
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

    var showSearchEngineDialog by remember { mutableStateOf(false) }
    var showHomepageDialog by remember { mutableStateOf(false) }
    var showOpenSourceDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings)) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            item {
                SettingsItem(
                    text = stringResource(R.string.settings_search_engine),
                    onClick = { showSearchEngineDialog = true }
                )
            }
            item {
                SettingsItem(
                    text = stringResource(R.string.settings_homepage),
                    onClick = { showHomepageDialog = true }
                )
            }
            item {
                SettingsItem(
                    text = stringResource(R.string.settings_language),
                    onClick = { navController.navigate(Routes.LANGUAGE) }
                )
            }
            item {
                SettingsItem(
                    text = stringResource(R.string.settings_about),
                    onClick = { navController.navigate(Routes.ABOUT) }
                )
            }
            item {
                SettingsItem(
                    text = stringResource(R.string.settings_open_source),
                    onClick = { showOpenSourceDialog = true }
                )
            }
        }
    }

    // Search Engine Dialog
    if (showSearchEngineDialog) {
        var selectedIndex by remember { mutableIntStateOf(prefs.searchEngineIndex) }

        AlertDialog(
            onDismissRequest = { showSearchEngineDialog = false },
            title = { Text(stringResource(R.string.select_search_engine)) },
            text = {
                LazyColumn {
                    val engines = SearchEngine.entries
                    items(engines.size) { index ->
                        val engine = engines[index]
                        SettingsRadioItem(
                            text = engine.displayName,
                            selected = index == selectedIndex,
                            onClick = { selectedIndex = index }
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    prefs.searchEngineIndex = selectedIndex
                    showSearchEngineDialog = false
                }) {
                    Text(stringResource(R.string.ok))
                }
            },
            dismissButton = {
                TextButton(onClick = { showSearchEngineDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    // Homepage Dialog
    if (showHomepageDialog) {
        var homepageInput by remember { mutableStateOf("") }
        var errorMessage by remember { mutableStateOf<String?>(null) }

        AlertDialog(
            onDismissRequest = { showHomepageDialog = false },
            title = { Text(stringResource(R.string.homepage)) },
            text = {
                @Suppress("KotlinConstantConditions")
                (androidx.compose.foundation.layout.Column {
                    Text(stringResource(R.string.current_homepage, prefs.homepage))
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
                            .padding(top = 8.dp)
                    )
                })
            },
            confirmButton = {
                TextButton(onClick = {
                    when {
                        homepageInput.isEmpty() -> {
                            errorMessage = context.getString(R.string.homepage_edit_error_empty)
                        }
                        !Patterns.WEB_URL.matcher(homepageInput).matches() -> {
                            errorMessage = context.getString(R.string.homepage_edit_error_invalid)
                        }
                        else -> {
                            prefs.homepage = homepageInput
                            showHomepageDialog = false
                        }
                    }
                }) {
                    Text(stringResource(R.string.ok))
                }
            },
            dismissButton = {
                TextButton(onClick = { showHomepageDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    // Open Source Dialog
    if (showOpenSourceDialog) {
        AlertDialog(
            onDismissRequest = { showOpenSourceDialog = false },
            title = { Text(stringResource(R.string.open_source_title)) },
            text = { Text(stringResource(R.string.open_source_license)) },
            confirmButton = {
                TextButton(onClick = {
                    showOpenSourceDialog = false
                    val intent = Intent(Intent.ACTION_VIEW)
                    intent.data = "https://github.com/sileneer/Hydrogen-Browser".toUri()
                    context.startActivity(intent)
                }) {
                    Text(stringResource(R.string.view_on_github))
                }
            },
            dismissButton = {
                TextButton(onClick = { showOpenSourceDialog = false }) {
                    Text(stringResource(R.string.ok))
                }
            }
        )
    }
}

@Composable
private fun SettingsItem(text: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

@Composable
private fun SettingsRadioItem(text: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        androidx.compose.foundation.layout.Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(selected = selected, onClick = onClick)
            Text(
                text = text,
                modifier = Modifier.padding(start = 8.dp),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
