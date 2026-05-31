package com.example.ui.screens

import android.webkit.CookieManager
import android.webkit.WebView
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.BrowserViewModel
import androidx.compose.ui.platform.testTag

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: BrowserViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currentEngine by viewModel.searchEngine.collectAsStateWithLifecycle()
    val isMinorSafe by viewModel.minorSafeMode.collectAsStateWithLifecycle()
    val isAdultBlocking by viewModel.adultSiteBlocking.collectAsStateWithLifecycle()

    var showClearHistoryDialog by remember { mutableStateOf(false) }
    var showClearCookiesDialog by remember { mutableStateOf(false) }
    var showResetShortcutsDialog by remember { mutableStateOf(false) }
    var showClearBookmarksDialog by remember { mutableStateOf(false) }
    var showClearDownloadsDialog by remember { mutableStateOf(false) }
    var showResetAllDataDialog by remember { mutableStateOf(false) }

    var showCreatePinDialog by remember { mutableStateOf(false) }
    var showVerifyPinDialogForToggleOff by remember { mutableStateOf(false) }
    var showVerifyPinDialogForLists by remember { mutableStateOf(false) }
    var showResetSafetyDialog by remember { mutableStateOf(false) }
    var showSafetyListsDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Browser Settings",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("settings_back_button")
                    ) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Section 1: Search Engine Selector
            Text(
                text = "DEFAULT SEARCH ENGINE",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 4.dp)
            )

            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    val engines = listOf("Google", "Microsoft Bing", "Yandex", "DuckDuckGo")
                    engines.forEachIndexed { index, name ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.updateSearchEngine(name) }
                                .padding(horizontal = 20.dp, vertical = 16.dp)
                                .testTag("engine_${name.lowercase().replace(" ", "_")}"),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = when (name) {
                                        "Google" -> Icons.Default.Search
                                        "Microsoft Bing" -> Icons.Default.TravelExplore
                                        "Yandex" -> Icons.Default.Language
                                        else -> Icons.Default.Shield
                                    },
                                    contentDescription = name,
                                    tint = if (currentEngine == name) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.width(16.dp))
                                Text(
                                    text = name,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = if (currentEngine == name) FontWeight.Bold else FontWeight.Normal,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            RadioButton(
                                selected = currentEngine == name,
                                onClick = { viewModel.updateSearchEngine(name) }
                            )
                        }
                        if (index < engines.size - 1) {
                            HorizontalDivider(
                                modifier = Modifier.padding(horizontal = 20.dp),
                                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                            )
                        }
                    }
                }
            }

            // Section 2: Browser Filters & Future hooks
            Text(
                text = "SAFETY FILTERS",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 4.dp)
            )

            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    val savedHash = viewModel.safetyPinHash.collectAsStateWithLifecycle().value

                    // Minor safe toggle with check
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                val turnOn = !isMinorSafe
                                if (turnOn) {
                                    if (savedHash.isEmpty()) showCreatePinDialog = true
                                    else viewModel.updateMinorSafeMode(true)
                                } else {
                                    if (savedHash.isNotEmpty()) showVerifyPinDialogForToggleOff = true
                                    else viewModel.updateMinorSafeMode(false)
                                }
                            }
                            .padding(horizontal = 20.dp, vertical = 16.dp)
                            .testTag("minor_safe_mode_row"),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Minor Safe-Browsing Mode",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Enforces safe search parameters and filters adult text flags on web queries.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = isMinorSafe,
                            onCheckedChange = { checked ->
                                if (checked) {
                                    if (savedHash.isEmpty()) showCreatePinDialog = true
                                    else viewModel.updateMinorSafeMode(true)
                                } else {
                                    if (savedHash.isNotEmpty()) showVerifyPinDialogForToggleOff = true
                                    else viewModel.updateMinorSafeMode(false)
                                }
                            },
                            modifier = Modifier.testTag("minor_safe_switch")
                        )
                    }

                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 20.dp),
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                    )

                    // Adult filtering toggle with check
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                val turnOn = !isAdultBlocking
                                if (turnOn) {
                                    if (savedHash.isEmpty()) showCreatePinDialog = true
                                    else viewModel.updateAdultSiteBlocking(true)
                                } else {
                                    if (savedHash.isNotEmpty()) showVerifyPinDialogForToggleOff = true
                                    else viewModel.updateAdultSiteBlocking(false)
                                }
                            }
                            .padding(horizontal = 20.dp, vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Adult-Site Shield Guard",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Blocks redirection requests to adult content platforms automatically.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = isAdultBlocking,
                            onCheckedChange = { checked ->
                                if (checked) {
                                    if (savedHash.isEmpty()) showCreatePinDialog = true
                                    else viewModel.updateAdultSiteBlocking(true)
                                } else {
                                    if (savedHash.isNotEmpty()) showVerifyPinDialogForToggleOff = true
                                    else viewModel.updateAdultSiteBlocking(false)
                                }
                            },
                            modifier = Modifier.testTag("adult_site_switch")
                        )
                    }

                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 20.dp),
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                    )

                    // Manage Blocklists with passcode protection hook
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                if (savedHash.isEmpty()) showCreatePinDialog = true
                                else showVerifyPinDialogForLists = true
                            }
                            .padding(horizontal = 20.dp, vertical = 16.dp)
                            .testTag("manage_safety_lists_row"),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Lock",
                            tint = if (isMinorSafe) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Access & Custom Lists",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "PIN protected tool to customize your blocked domains, unsafe terms and check URL safety status.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowRight,
                            contentDescription = "Go",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Section 3: Browser Cache / Options Control
            Text(
                text = "DATA MANAGEMENT",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 4.dp)
            )

            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    // Clear History Action
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showClearHistoryDialog = true }
                            .padding(horizontal = 20.dp, vertical = 16.dp)
                            .testTag("clear_history_row"),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = "History",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = "Clear Browsing Logs",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 20.dp),
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                    )

                    // Clear Cache Action
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                try {
                                    WebView(context).clearCache(true)
                                    Toast.makeText(context, "Browser cache successfully cleared", Toast.LENGTH_SHORT).show()
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Error clearing cache", Toast.LENGTH_SHORT).show()
                                }
                            }
                            .padding(horizontal = 20.dp, vertical = 16.dp)
                            .testTag("clear_cache_row"),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteSweep,
                            contentDescription = "Cache",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = "Clear Cache Files",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 20.dp),
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                    )

                    // Clear Cookies Action
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showClearCookiesDialog = true }
                            .padding(horizontal = 20.dp, vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CleaningServices,
                            contentDescription = "Cookies",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = "Clear Site Cookies",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 20.dp),
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                    )

                    // Rest Shortcuts defaults
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showResetShortcutsDialog = true }
                            .padding(horizontal = 20.dp, vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Restore,
                            contentDescription = "Restore Shortcuts",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = "Reset Website Shortcuts",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 20.dp),
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                    )

                    // Clear Bookmarks
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showClearBookmarksDialog = true }
                            .padding(horizontal = 20.dp, vertical = 16.dp)
                            .testTag("clear_bookmarks_row"),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.BookmarkBorder,
                            contentDescription = "Clear Bookmarks",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = "Clear All Bookmarks",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 20.dp),
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                    )

                    // Clear Downloads
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showClearDownloadsDialog = true }
                            .padding(horizontal = 20.dp, vertical = 16.dp)
                            .testTag("clear_downloads_row"),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.DownloadForOffline,
                            contentDescription = "Clear Downloads",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = "Clear Downloads List",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 20.dp),
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                    )

                    // Reset All Data
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showResetAllDataDialog = true }
                            .padding(horizontal = 20.dp, vertical = 16.dp)
                            .testTag("reset_browser_data_row"),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Reset Browser",
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = "Reset All Browser Data",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Section 4: Appearance
            Text(
                text = "APPEARANCE",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 4.dp)
            )

            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Palette,
                                contentDescription = "Theme",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(
                                    text = "Color Palette",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Light Theme (Default)",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Text(
                            text = "Light",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // Section 5: About
            Text(
                text = "ABOUT",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 4.dp)
            )

            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Language,
                        contentDescription = "Vido Browser Logo",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(48.dp)
                    )
                    Text(
                        text = "Vido Browser",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Version 1.2.0 (Stable)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "A clean, safe, lightweight browser built for security, high performance, and absolute speed.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        // --- Dialog Overlays ---
        if (showClearHistoryDialog) {
            AlertDialog(
                onDismissRequest = { showClearHistoryDialog = false },
                title = { Text("Clear Browsing History?") },
                text = { Text("Are you sure you want to delete all saved items from your local logs list? This action is irreversible.") },
                confirmButton = {
                    Button(
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        onClick = {
                            viewModel.clearBrowserHistory()
                            showClearHistoryDialog = false
                            Toast.makeText(context, "Browsing history cleared", Toast.LENGTH_SHORT).show()
                        }
                    ) {
                        Text("Clear All")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showClearHistoryDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }

        if (showClearCookiesDialog) {
            AlertDialog(
                onDismissRequest = { showClearCookiesDialog = false },
                title = { Text("Clear Site Cookies?") },
                text = { Text("This will sign you out of most active websites within the browser window. Proceed?") },
                confirmButton = {
                    Button(
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        onClick = {
                            CookieManager.getInstance().removeAllCookies { success ->
                                Toast.makeText(context, "Site cookies cleared", Toast.LENGTH_SHORT).show()
                            }
                            CookieManager.getInstance().flush()
                            showClearCookiesDialog = false
                        }
                    ) {
                        Text("Clear Cookies")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showClearCookiesDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }

        if (showResetShortcutsDialog) {
            AlertDialog(
                onDismissRequest = { showResetShortcutsDialog = false },
                title = { Text("Reset Shortcuts List?") },
                text = { Text("This deletes all added custom shortcuts and drops them back to the factory set (Google, YouTube, Wikipedia, etc). Confirm?") },
                confirmButton = {
                    Button(
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        onClick = {
                            viewModel.resetShortcutsToDefault()
                            showResetShortcutsDialog = false
                            Toast.makeText(context, "Reset shortcuts completed", Toast.LENGTH_SHORT).show()
                        }
                    ) {
                        Text("Reset Default")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showResetShortcutsDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }

        if (showClearBookmarksDialog) {
            AlertDialog(
                onDismissRequest = { showClearBookmarksDialog = false },
                title = { Text("Clear All Bookmarks?") },
                text = { Text("Are you sure you want to delete all bookmarked web and favorite references? This action is irreversible.") },
                confirmButton = {
                    Button(
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        onClick = {
                            viewModel.clearAllBookmarks()
                            showClearBookmarksDialog = false
                            Toast.makeText(context, "Bookmarks cleared", Toast.LENGTH_SHORT).show()
                        }
                    ) {
                        Text("Clear All")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showClearBookmarksDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }

        if (showClearDownloadsDialog) {
            AlertDialog(
                onDismissRequest = { showClearDownloadsDialog = false },
                title = { Text("Clear Downloads History?") },
                text = { Text("Are you sure you want to clear the logs of all downloads? The actual files on your phone storage will NOT be deleted.") },
                confirmButton = {
                    Button(
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        onClick = {
                            viewModel.clearAllDownloads()
                            showClearDownloadsDialog = false
                            Toast.makeText(context, "Downloads list cleared", Toast.LENGTH_SHORT).show()
                        }
                    ) {
                        Text("Clear List")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showClearDownloadsDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }

        if (showResetAllDataDialog) {
            AlertDialog(
                onDismissRequest = { showResetAllDataDialog = false },
                title = { Text("Reset Browser to Factory Settings?") },
                text = { Text("This will aggressively clear all history logs, bookmark lists, tab histories, downloads catalogs, cookie tokens, caches, and reset web engine preferences. This cannot be undone.") },
                confirmButton = {
                    Button(
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        onClick = {
                            viewModel.resetAllBrowserData()
                            try {
                                WebView(context).clearCache(true)
                                CookieManager.getInstance().removeAllCookies { }
                            } catch (_: Exception) {}
                            showResetAllDataDialog = false
                            Toast.makeText(context, "Full browser system reset successfully", Toast.LENGTH_SHORT).show()
                        }
                    ) {
                        Text("Reset Browser")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showResetAllDataDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }

        // --- Minor-Safe Mode Dialogs ---

        if (showCreatePinDialog) {
            var pinInput by remember { mutableStateOf("") }
            var pinConfirmInput by remember { mutableStateOf("") }
            var errorMessage by remember { mutableStateOf("") }

            AlertDialog(
                onDismissRequest = { showCreatePinDialog = false },
                title = { Text("Set 4-Digit Security PIN") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            "Set a 4-digit security PIN to prevent accidental disabling of Minor-Safe Mode or modifying safety configurations.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        OutlinedTextField(
                            value = pinInput,
                            onValueChange = {
                                if (it.length <= 4 && it.all { c -> c.isDigit() }) {
                                    pinInput = it
                                    errorMessage = ""
                                }
                            },
                            label = { Text("New PIN") },
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("new_pin_input")
                        )
                        OutlinedTextField(
                            value = pinConfirmInput,
                            onValueChange = {
                                if (it.length <= 4 && it.all { c -> c.isDigit() }) {
                                    pinConfirmInput = it
                                    errorMessage = ""
                                }
                            },
                            label = { Text("Confirm PIN") },
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("confirm_pin_input")
                        )
                        if (errorMessage.isNotEmpty()) {
                            Text(errorMessage, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (pinInput.length != 4) {
                                errorMessage = "PIN must be exactly 4 digits long."
                            } else if (pinInput != pinConfirmInput) {
                                errorMessage = "PINs do not match."
                            } else {
                                viewModel.setSafetyPin(pinInput)
                                viewModel.updateMinorSafeMode(true)
                                viewModel.updateAdultSiteBlocking(true)
                                showCreatePinDialog = false
                                Toast.makeText(context, "PIN code successfully saved", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.testTag("save_pin_button")
                    ) {
                        Text("Enable & Lock")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showCreatePinDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }

        if (showVerifyPinDialogForToggleOff) {
            var pinVerifyInput by remember { mutableStateOf("") }
            var showVerifyError by remember { mutableStateOf(false) }

            AlertDialog(
                onDismissRequest = { showVerifyPinDialogForToggleOff = false },
                title = { Text("Security Verification") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Enter your 4-digit safety PIN to turn off safe mode filters.")
                        OutlinedTextField(
                            value = pinVerifyInput,
                            onValueChange = {
                                if (it.length <= 4 && it.all { c -> c.isDigit() }) {
                                    pinVerifyInput = it
                                    showVerifyError = false
                                }
                            },
                            label = { Text("Enter PIN") },
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("verify_pin_input")
                        )
                        if (showVerifyError) {
                            Text("Incorrect PIN. Please try again.", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        TextButton(
                            colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
                            onClick = { showResetSafetyDialog = true }
                        ) {
                            Text("Forgot PIN? Reset Settings")
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (viewModel.checkSafetyPin(pinVerifyInput)) {
                                viewModel.updateMinorSafeMode(false)
                                viewModel.updateAdultSiteBlocking(false)
                                showVerifyPinDialogForToggleOff = false
                                Toast.makeText(context, "Safety filters unlocked successfully", Toast.LENGTH_SHORT).show()
                            } else {
                                showVerifyError = true
                            }
                        },
                        modifier = Modifier.testTag("submit_verify_pin_button")
                    ) {
                        Text("Verify & Unlock")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showVerifyPinDialogForToggleOff = false }) {
                        Text("Cancel")
                    }
                }
            )
        }

        if (showVerifyPinDialogForLists) {
            var pinVerifyInput by remember { mutableStateOf("") }
            var showVerifyError by remember { mutableStateOf(false) }

            AlertDialog(
                onDismissRequest = { showVerifyPinDialogForLists = false },
                title = { Text("Passcode Verification") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Enter your 4-digit safety PIN to access custom blocking lists and safety tester tools.")
                        OutlinedTextField(
                            value = pinVerifyInput,
                            onValueChange = {
                                if (it.length <= 4 && it.all { c -> c.isDigit() }) {
                                    pinVerifyInput = it
                                    showVerifyError = false
                                }
                            },
                            label = { Text("Enter PIN") },
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("verify_lists_pin_input")
                        )
                        if (showVerifyError) {
                            Text("Incorrect PIN. Please try again.", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        TextButton(
                            colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
                            onClick = { showResetSafetyDialog = true }
                        ) {
                            Text("Forgot PIN? Reset Settings")
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (viewModel.checkSafetyPin(pinVerifyInput)) {
                                showVerifyPinDialogForLists = false
                                showSafetyListsDialog = true
                            } else {
                                showVerifyError = true
                            }
                        },
                        modifier = Modifier.testTag("submit_lists_pin_button")
                    ) {
                        Text("Verify & Continue")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showVerifyPinDialogForLists = false }) {
                        Text("Cancel")
                    }
                }
            )
        }

        if (showResetSafetyDialog) {
            AlertDialog(
                onDismissRequest = { showResetSafetyDialog = false },
                title = { Text("Reset Safety Settings?") },
                text = {
                    Text(
                        "WARNING: Resetting will completely clear the PIN passcode, disable Minor-Safe Mode, turn off all adult domain-redirection shields, and wipe custom list settings.\n\nAll security locks will be removed.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                },
                confirmButton = {
                    Button(
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        onClick = {
                            viewModel.resetSafetySettings()
                            showResetSafetyDialog = false
                            showVerifyPinDialogForToggleOff = false
                            showVerifyPinDialogForLists = false
                            showSafetyListsDialog = false
                            Toast.makeText(context, "Passcode and safety filters reset completely", Toast.LENGTH_LONG).show()
                        },
                        modifier = Modifier.testTag("confirm_reset_safety_button")
                    ) {
                        Text("Reset Everything")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showResetSafetyDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }

        if (showSafetyListsDialog) {
            val customDomains by viewModel.customBlockedDomains.collectAsStateWithLifecycle()
            val customKeywords by viewModel.customUnsafeKeywords.collectAsStateWithLifecycle()
            
            var domainInput by remember { mutableStateOf("") }
            var keywordInput by remember { mutableStateOf("") }
            
            var checkInput by remember { mutableStateOf("") }
            var checkResult by remember { mutableStateOf<String?>(null) }
            var checkIsBlocked by remember { mutableStateOf(false) }

            AlertDialog(
                onDismissRequest = { showSafetyListsDialog = false },
                title = { 
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Safety List Manager", fontWeight = FontWeight.Bold)
                        IconButton(onClick = { showSafetyListsDialog = false }) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                        }
                    }
                },
                text = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 380.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Custom Domains List
                        Text("CUSTOM BLOCKED DOMAINS", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = domainInput,
                                    onValueChange = { domainInput = it },
                                    label = { Text("e.g. badsite.com") },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f).testTag("add_domain_input")
                                )
                                Button(
                                    onClick = {
                                        if (domainInput.isNotBlank()) {
                                            viewModel.addCustomBlockedDomain(domainInput)
                                            domainInput = ""
                                            Toast.makeText(context, "Domain blocked", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    modifier = Modifier.testTag("add_domain_button")
                                ) {
                                    Text("Add")
                                }
                            }
                            
                            if (customDomains.isEmpty()) {
                                Text("No custom domains added.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            } else {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                                        .padding(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    customDomains.forEach { domain ->
                                        Row(
                                            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(domain, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                                            IconButton(
                                                onClick = { viewModel.deleteCustomBlockedDomain(domain) },
                                                modifier = Modifier.size(24.dp).testTag("delete_domain_$domain")
                                            ) {
                                                Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(20.dp))
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                        // Custom Keywords List
                        Text("CUSTOM UNSAFE KEYWORDS", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = keywordInput,
                                    onValueChange = { keywordInput = it },
                                    label = { Text("e.g. violence") },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f).testTag("add_keyword_input")
                                )
                                Button(
                                    onClick = {
                                        if (keywordInput.isNotBlank()) {
                                            viewModel.addCustomUnsafeKeyword(keywordInput)
                                            keywordInput = ""
                                            Toast.makeText(context, "Keyword blocked", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    modifier = Modifier.testTag("add_keyword_button")
                                ) {
                                    Text("Add")
                                }
                            }
                            
                            if (customKeywords.isEmpty()) {
                                Text("No custom keywords added.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            } else {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                                        .padding(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    customKeywords.forEach { word ->
                                        Row(
                                            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(word, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                                            IconButton(
                                                onClick = { viewModel.deleteCustomUnsafeKeyword(word) },
                                                modifier = Modifier.size(24.dp).testTag("delete_keyword_$word")
                                            ) {
                                                Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(20.dp))
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                        // URL Safety Checker Tester Tool
                        Text("URL SAFETY CHECKER", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = checkInput,
                                    onValueChange = { checkInput = it },
                                    label = { Text("Enter URL or search query") },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f).testTag("checker_input")
                                )
                                Button(
                                    onClick = {
                                        if (checkInput.isNotBlank()) {
                                            val input = checkInput.trim()
                                            val isBlock = com.example.data.BrowserSafetyFilter.isUrlBlocked(input, customDomains, customKeywords) ||
                                                          com.example.data.BrowserSafetyFilter.isSearchQueryBlocked(input, customKeywords)
                                            checkIsBlocked = isBlock
                                            checkResult = if (isBlock) "🚨 BLOCKED under safety rules" else "✅ SAFE for browsing"
                                        }
                                    },
                                    modifier = Modifier.testTag("checker_test_button")
                                ) {
                                    Text("Check")
                                }
                            }
                            
                            checkResult?.let { result ->
                                Card(
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (checkIsBlocked) {
                                            MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                                        } else {
                                            Color(0xFFE8F5E9)
                                        }
                                    ),
                                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = if (checkIsBlocked) Icons.Default.Warning else Icons.Default.Check,
                                            contentDescription = "Status icon",
                                            tint = if (checkIsBlocked) MaterialTheme.colorScheme.error else Color(0xFF2E7D32)
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text(
                                            text = result,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = if (checkIsBlocked) MaterialTheme.colorScheme.onErrorContainer else Color(0xFF2E7D32)
                                        )
                                    }
                                }
                            }
                        }
                    }
                },
                confirmButton = {},
                dismissButton = {
                    TextButton(
                        onClick = { showSafetyListsDialog = false },
                        modifier = Modifier.testTag("close_lists_button")
                    ) {
                        Text("Close")
                    }
                }
            )
        }
    }
}
