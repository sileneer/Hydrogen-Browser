package com.sileneer.hydrogenbrowser.ui.browser

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.sileneer.hydrogenbrowser.R
import com.sileneer.hydrogenbrowser.tab.Tab

@Composable
fun TabGridOverlay(
    tabs: List<Tab>,
    activeTabId: Int,
    onSelectTab: (Int) -> Unit,
    onCloseTab: (Int) -> Unit,
    onAddTab: () -> Unit,
    onDismiss: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .windowInsetsPadding(WindowInsets.systemBars)
    ) {
        // Top bar
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 8.dp)
        ) {
            IconButton(onClick = onDismiss) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.menu_back)
                )
            }
            Text(
                text = stringResource(R.string.tabs_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f)
            )
        }

        // Grid body
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // ponytail: close is the X on each card only — swipe-to-dismiss fights the grid's scroll
            items(tabs, key = { it.id }) { tab ->
                TabCard(
                    tab = tab,
                    isActive = tab.id == activeTabId,
                    onClick = { onSelectTab(tab.id) },
                    onClose = { onCloseTab(tab.id) }
                )
            }
        }

        // New tab button
        Button(
            onClick = onAddTab,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(Modifier.size(8.dp))
            Text(stringResource(R.string.new_tab_action))
        }
    }
}

@Composable
private fun TabCard(
    tab: Tab,
    isActive: Boolean,
    onClick: () -> Unit,
    onClose: () -> Unit
) {
    val shape = RoundedCornerShape(12.dp)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(3f / 4f)
            .clip(shape)
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            .border(if (isActive) 2.dp else 0.dp, MaterialTheme.colorScheme.primary, shape)
            .clickable(onClick = onClick)
    ) {
        tab.thumbnail?.let { thumbnail ->
            Image(
                bitmap = thumbnail.asImageBitmap(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }

        // Top scrim with title and close button
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.75f))
                .padding(start = 10.dp, top = 4.dp, bottom = 4.dp, end = 2.dp)
        ) {
            Text(
                text = tab.displayTitle,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = onClose, modifier = Modifier.size(28.dp)) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = stringResource(R.string.close_tab),
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}
