package com.example.ui.screens

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.webkit.CookieManager
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.BrowserSafetyFilter
import com.example.ui.BrowserViewModel
import androidx.compose.ui.platform.testTag

@SuppressLint("SetJavaScriptEnabled", "ClickableViewAccessibility")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BrowserScreen(
    initialUrl: String,
    viewModel: BrowserViewModel,
    onNavigateHome: () -> Unit,
    onNavigateToTabs: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToBookmarks: () -> Unit,
    onNavigateToDownloads: () -> Unit,
    onNavigateToSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val loadProgress by viewModel.loadProgress.collectAsStateWithLifecycle()
    val currentUrl by viewModel.currentBrowserUrl.collectAsStateWithLifecycle()
    val pageTitle by viewModel.browserTitle.collectAsStateWithLifecycle()

    val isMinorSafe by viewModel.minorSafeMode.collectAsStateWithLifecycle()
    val isAdultBlocking by viewModel.adultSiteBlocking.collectAsStateWithLifecycle()

    // Observe active selected tab to load correct contents and colors
    val activeTab by viewModel.selectedTab.collectAsStateWithLifecycle()
    val bookmarksList by viewModel.bookmarks.collectAsStateWithLifecycle()

    val isBookmarked = remember(bookmarksList, currentUrl) {
        bookmarksList.any { it.url == currentUrl }
    }

    // Screen-level state to track if current URL is blocked or errored
    var isBlocked by remember { mutableStateOf(false) }
    var blockedUrlAttempted by remember { mutableStateOf("") }
    var isLoadError by remember { mutableStateOf(false) }

    // Desktop mode toggle
    var isDesktopMode by remember { mutableStateOf(false) }

    // Search dialog/sheet triggered by bottom navigation Search button
    var showUrlSearchInput by remember { mutableStateOf(false) }
    var searchDialgInputQuery by remember { mutableStateOf("") }

    // Find in Page state
    var isFindInPageActive by remember { mutableStateOf(false) }
    var findInPageQuery by remember { mutableStateOf("") }

    // Tools Sheet/Dialog state
    var showBrowserToolsSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()

    // Reference to local WebView
    var webViewInstance by remember { mutableStateOf<WebView?>(null) }

    val navTargetUrl by viewModel.currentNavTargetUrl.collectAsStateWithLifecycle()

    // Initialize tabs if empty and handle direct incoming Url requests
    LaunchedEffect(initialUrl) {
        val urlToLoad = if (initialUrl == "home") "https://www.google.com" else initialUrl
        viewModel.openInput(urlToLoad, com.example.ui.OpenTarget.CURRENT_TAB)
    }

    // Monitor for tab shift or URL change to load new Page in WebView safely
    LaunchedEffect(navTargetUrl, webViewInstance) {
        val webView = webViewInstance
        if (webView != null && navTargetUrl.isNotBlank()) {
            val currentWebUrl = webView.url ?: ""
            // Normalize trailing slashes and spaces
            val normWebUrl = currentWebUrl.trim().removeSuffix("/")
            val normTargetUrl = navTargetUrl.trim().removeSuffix("/")
            
            if (normWebUrl != normTargetUrl) {
                isBlocked = false
                isLoadError = false
                webView.stopLoading()
                webView.loadUrl(navTargetUrl)
            }
        }
    }

    val isPrivate = activeTab?.isPrivate == true

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        if (isPrivate) Color(0xFF1A1A1A) else MaterialTheme.colorScheme.surface
                    )
            ) {
                // Compact high fidelity top loading progress bar
                if (isLoading) {
                    LinearProgressIndicator(
                        progress = { loadProgress / 100f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(3.dp)
                            .testTag("web_load_progress"),
                        color = if (isPrivate) Color(0xFFF44336) else MaterialTheme.colorScheme.primary,
                        trackColor = Color.Transparent
                    )
                } else {
                    Spacer(modifier = Modifier.height(3.dp))
                }

                TopAppBar(
                    title = {
                        Column(
                            modifier = Modifier
                                .clickable {
                                    searchDialgInputQuery = currentUrl
                                    showUrlSearchInput = true
                                }
                                .padding(vertical = 2.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (isPrivate) {
                                    Icon(
                                        imageVector = Icons.Default.Security,
                                        contentDescription = "Private Tab Badge",
                                        tint = Color(0xFFEF5350),
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                }
                                Text(
                                    text = pageTitle.ifBlank { "Unlabeled Webpage" },
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    color = if (isPrivate) Color.White else MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Text(
                                text = currentUrl.replace("https://", "").replace("http://", "").ifBlank { "google.com" },
                                style = MaterialTheme.typography.bodySmall,
                                color = if (isPrivate) Color.LightGray.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = {
                                if (webViewInstance?.canGoBack() == true) {
                                    isBlocked = false
                                    isLoadError = false
                                    webViewInstance?.goBack()
                                } else {
                                    onNavigateHome()
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Back Navigation",
                                tint = if (isPrivate) Color.White else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = { viewModel.toggleBookmark(pageTitle, currentUrl) },
                            modifier = Modifier.testTag("star_bookmark_btn")
                        ) {
                            Icon(
                                imageVector = if (isBookmarked) Icons.Default.Star else Icons.Default.StarBorder,
                                contentDescription = "Toggle favorite",
                                tint = if (isBookmarked) Color(0xFFFFC107) else if (isPrivate) Color.LightGray else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        IconButton(onClick = {
                            isBlocked = false
                            isLoadError = false
                            webViewInstance?.reload()
                        }) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Refresh Page",
                                tint = if (isPrivate) Color.White else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = if (isPrivate) Color(0xFF1A1A1A) else MaterialTheme.colorScheme.surface
                    )
                )
            }
        },
        bottomBar = {
            // Unify bottom style to standard fast 4-button mobile navigation bar (Home, Search, Tabs, Menu)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(if (isPrivate) Color(0xFF1A1A1A) else MaterialTheme.colorScheme.surface)
            ) {
                // Find in page tool bar if active
                AnimatedVisibility(visible = isFindInPageActive) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Find status",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            TextField(
                                value = findInPageQuery,
                                onValueChange = { query ->
                                    findInPageQuery = query
                                    webViewInstance?.findAllAsync(query)
                                },
                                placeholder = { Text("Find text on page...") },
                                singleLine = true,
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent
                                ),
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(onClick = { webViewInstance?.findNext(false) }) {
                                Icon(imageVector = Icons.Default.KeyboardArrowUp, contentDescription = "Previous occurrence")
                            }
                            IconButton(onClick = { webViewInstance?.findNext(true) }) {
                                Icon(imageVector = Icons.Default.KeyboardArrowDown, contentDescription = "Next occurrence")
                            }
                            IconButton(onClick = {
                                webViewInstance?.clearMatches()
                                isFindInPageActive = false
                                findInPageQuery = ""
                            }) {
                                Icon(imageVector = Icons.Default.Close, contentDescription = "Close Find")
                            }
                        }
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                NavigationBar(
                    containerColor = if (isPrivate) Color(0xFF1A1A1A) else MaterialTheme.colorScheme.surface,
                    tonalElevation = 6.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .windowInsetsPadding(WindowInsets.navigationBars)
                        .height(56.dp)
                ) {
                    NavigationBarItem(
                        selected = false,
                        onClick = onNavigateHome,
                        icon = { Icon(imageVector = Icons.Default.Home, contentDescription = "Home Page") },
                        label = { Text("Home", style = MaterialTheme.typography.labelSmall) },
                        colors = NavigationBarItemDefaults.colors(
                            unselectedIconColor = if (isPrivate) Color.White.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.testTag("nav_home_btn")
                    )

                    NavigationBarItem(
                        selected = false,
                        onClick = {
                            searchDialgInputQuery = currentUrl
                            showUrlSearchInput = true
                        },
                        icon = { Icon(imageVector = Icons.Default.Language, contentDescription = "Edit Search URL") },
                        label = { Text("Search", style = MaterialTheme.typography.labelSmall) },
                        colors = NavigationBarItemDefaults.colors(
                            unselectedIconColor = if (isPrivate) Color.White.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.testTag("nav_search_btn")
                    )

                    NavigationBarItem(
                        selected = false,
                        onClick = onNavigateToTabs,
                        icon = { Icon(imageVector = Icons.Default.Tab, contentDescription = "Tabs screen") },
                        label = { Text("Tabs", style = MaterialTheme.typography.labelSmall) },
                        colors = NavigationBarItemDefaults.colors(
                            unselectedIconColor = if (isPrivate) Color.White.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.testTag("nav_tabs_btn")
                    )

                    NavigationBarItem(
                        selected = false,
                        onClick = { showBrowserToolsSheet = true },
                        icon = { Icon(imageVector = Icons.Default.Menu, contentDescription = "Menu popup") },
                        label = { Text("Menu", style = MaterialTheme.typography.labelSmall) },
                        colors = NavigationBarItemDefaults.colors(
                            unselectedIconColor = if (isPrivate) Color.White.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.testTag("nav_menu_btn")
                    )
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            if (isBlocked) {
                SafetyWarningScreen(
                    blockedUrl = blockedUrlAttempted,
                    onGoHome = {
                        isBlocked = false
                        viewModel.updateBrowserInfo("Home", "home")
                        onNavigateHome()
                    },
                    onGoBack = {
                        if (webViewInstance?.canGoBack() == true) {
                            webViewInstance?.goBack()
                            isBlocked = false
                        } else {
                            isBlocked = false
                            viewModel.updateBrowserInfo("Home", "home")
                            onNavigateHome()
                        }
                    },
                    onOpenSettings = {
                        isBlocked = false
                        onNavigateToSettings()
                    }
                )
            } else if (isLoadError) {
                // Connection or Page loading Failure screen
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.WifiOff,
                        contentDescription = "Failure Icon",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(56.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Page Load Error",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "We couldn't open this webpage. Check your internet connection and try reloading.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = {
                            isLoadError = false
                            webViewInstance?.reload()
                        },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Refresh, contentDescription = "Retry Icon", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Retry Reloading", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            } else {
                // Web representation
                AndroidView(
                    factory = { ctx ->
                        WebView(ctx).apply {
                            layoutParams = android.view.ViewGroup.LayoutParams(
                                android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                                android.view.ViewGroup.LayoutParams.MATCH_PARENT
                            )

                            // Apply robust fast configuration options
                            settings.apply {
                                javaScriptEnabled = true
                                domStorageEnabled = true
                                useWideViewPort = true
                                loadWithOverviewMode = true
                                setSupportZoom(true)
                                builtInZoomControls = true
                                displayZoomControls = false
                                cacheMode = WebSettings.LOAD_DEFAULT
                                mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
                                databaseEnabled = true
                                
                                if (isPrivate) {
                                    savePassword = false
                                    saveFormData = false
                                }
                            }

                            webViewClient = object : WebViewClient() {
                                override fun shouldOverrideUrlLoading(
                                    view: WebView?,
                                    request: WebResourceRequest?
                                ): Boolean {
                                    val requestUrl = request?.url?.toString() ?: ""
                                    
                                    val allowed = BrowserSafetyFilter.isUrlAllowed(
                                        url = requestUrl,
                                        isMinorSafe = isMinorSafe,
                                        isAdultBlocking = isAdultBlocking,
                                        customDomains = viewModel.customBlockedDomains.value,
                                        customKeywords = viewModel.customUnsafeKeywords.value
                                    )
                                    if (!allowed) {
                                        isBlocked = true
                                        blockedUrlAttempted = requestUrl
                                        return true // Stop load
                                    }
                                    return false
                                }

                                override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                                    super.onPageStarted(view, url, favicon)
                                    val loadedUrl = url ?: ""
                                    val allowed = BrowserSafetyFilter.isUrlAllowed(
                                        url = loadedUrl,
                                        isMinorSafe = isMinorSafe,
                                        isAdultBlocking = isAdultBlocking,
                                        customDomains = viewModel.customBlockedDomains.value,
                                        customKeywords = viewModel.customUnsafeKeywords.value
                                    )
                                    if (!allowed) {
                                        isBlocked = true
                                        blockedUrlAttempted = loadedUrl
                                        view?.stopLoading()
                                        return
                                    }
                                    isLoadError = false
                                    viewModel.updateLoadingState(true, 15)
                                }

                                override fun onPageFinished(view: WebView?, url: String?) {
                                    super.onPageFinished(view, url)
                                    viewModel.updateLoadingState(false, 100)
                                    viewModel.updateBrowserInfo(view?.title ?: "", url ?: "")
                                }

                                override fun onReceivedError(
                                    view: WebView?,
                                    request: WebResourceRequest?,
                                    error: android.webkit.WebResourceError?
                                ) {
                                    super.onReceivedError(view, request, error)
                                    if (request?.isForMainFrame == true) {
                                        isLoadError = true
                                    }
                                }
                            }

                            webChromeClient = object : WebChromeClient() {
                                override fun onProgressChanged(view: WebView?, newProgress: Int) {
                                    super.onProgressChanged(view, newProgress)
                                    viewModel.updateLoadingState(newProgress < 100, newProgress)
                                }

                                override fun onReceivedTitle(view: WebView?, title: String?) {
                                    super.onReceivedTitle(view, title)
                                    viewModel.updateBrowserInfo(title ?: "", view?.url ?: "")
                                }
                            }

                            // Download Handling Guard matches parental and custom block list terms
                            setDownloadListener { url, userAgent, contentDisposition, mimetype, contentLength ->
                                if (viewModel.minorSafeMode.value && BrowserSafetyFilter.isUrlBlocked(url, viewModel.customBlockedDomains.value, viewModel.customUnsafeKeywords.value)) {
                                    Toast.makeText(ctx, "Download Blocked: Violates minor-safety guidelines.", Toast.LENGTH_LONG).show()
                                    return@setDownloadListener
                                }
                                try {
                                    val request = android.app.DownloadManager.Request(android.net.Uri.parse(url)).apply {
                                        setMimeType(mimetype)
                                        addRequestHeader("User-Agent", userAgent)
                                        setDescription("File download triggered from browser...")
                                        val guessedFilename = android.webkit.URLUtil.guessFileName(url, contentDisposition, mimetype)
                                        setTitle(guessedFilename)
                                        setNotificationVisibility(android.app.DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                                        setDestinationInExternalPublicDir(
                                            android.os.Environment.DIRECTORY_DOWNLOADS,
                                            guessedFilename
                                        )
                                    }
                                    val dm = ctx.getSystemService(android.content.Context.DOWNLOAD_SERVICE) as android.app.DownloadManager
                                    val downloadId = dm.enqueue(request)
                                    val filename = android.webkit.URLUtil.guessFileName(url, contentDisposition, mimetype)
                                    viewModel.addDownload(downloadId, filename, url)
                                    Toast.makeText(ctx, "Download Started: $filename", Toast.LENGTH_SHORT).show()
                                } catch (e: Exception) {
                                    Toast.makeText(ctx, "Failed to download file: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                                }
                            }

                            webViewInstance = this
                        }
                    },
                    update = { view ->
                        webViewInstance = view
                    },
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("real_webview_client")
                )
            }
        }

        // --- Dialog Search Input overlay for URL search button or top bar click ---
        if (showUrlSearchInput) {
            Dialog(onDismissRequest = { showUrlSearchInput = false }) {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Navigate URL",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        OutlinedTextField(
                            value = searchDialgInputQuery,
                            onValueChange = { searchDialgInputQuery = it },
                            placeholder = { Text("Search or type URL domain...") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Go),
                            keyboardActions = KeyboardActions(
                                onGo = {
                                    if (searchDialgInputQuery.isNotBlank()) {
                                        viewModel.openInput(searchDialgInputQuery, com.example.ui.OpenTarget.CURRENT_TAB)
                                        showUrlSearchInput = false
                                    }
                                }
                            ),
                            modifier = Modifier.fillMaxWidth().testTag("browser_search_overlay_input")
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextButton(onClick = { showUrlSearchInput = false }) {
                                Text("Cancel")
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    if (searchDialgInputQuery.isNotBlank()) {
                                        viewModel.openInput(searchDialgInputQuery, com.example.ui.OpenTarget.CURRENT_TAB)
                                        showUrlSearchInput = false
                                    }
                                },
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Go Page")
                            }
                        }
                    }
                }
            }
        }

        // --- Menu Bottom Sheet displaying all other features ---
        if (showBrowserToolsSheet) {
            ModalBottomSheet(
                onDismissRequest = { showBrowserToolsSheet = false },
                sheetState = sheetState,
                containerColor = MaterialTheme.colorScheme.surface,
                dragHandle = { BottomSheetDefaults.DragHandle() }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .padding(bottom = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    Text(
                        text = "Browser Settings",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    // Desktop mode switcher bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.DesktopMac,
                                contentDescription = "Desktop view icon",
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                "Desktop Website View",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Switch(
                            checked = isDesktopMode,
                            onCheckedChange = { desktopSelected ->
                                isDesktopMode = desktopSelected
                                webViewInstance?.let { webView ->
                                    val s = webView.settings
                                    if (desktopSelected) {
                                        s.userAgentString = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/110.0.0.0 Safari/537.36"
                                    } else {
                                        s.userAgentString = null
                                    }
                                    webView.reload()
                                }
                                showBrowserToolsSheet = false
                            }
                        )
                    }

                    // Tools grid
                    val tools = listOf(
                        Triple(Icons.Default.History, "History", onNavigateToHistory),
                        Triple(Icons.Default.Bookmark, "Bookmarks", onNavigateToBookmarks),
                        Triple(Icons.Default.Download, "Downloads", onNavigateToDownloads),
                        Triple(Icons.Default.Search, "Find in Page", {
                            isFindInPageActive = true
                            showBrowserToolsSheet = false
                        }),
                        Triple(Icons.Default.ContentCopy, "Copy Link", {
                            clipboardManager.setText(AnnotatedString(currentUrl))
                            Toast.makeText(context, "URL Copied to clipboard", Toast.LENGTH_SHORT).show()
                            showBrowserToolsSheet = false
                        }),
                        Triple(Icons.Default.Share, "Share", {
                            val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(android.content.Intent.EXTRA_TEXT, currentUrl)
                            }
                            context.startActivity(android.content.Intent.createChooser(intent, "Share Link"))
                            showBrowserToolsSheet = false
                        }),
                        Triple(Icons.Default.Settings, "Settings", onNavigateToSettings),
                        Triple(Icons.Default.OpenInNew, "External Browser", {
                            try {
                                val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(currentUrl))
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                Toast.makeText(context, "No external browser found", Toast.LENGTH_SHORT).show()
                            }
                            showBrowserToolsSheet = false
                        })
                    )

                    val columns = 4
                    val toolRows = tools.chunked(columns)

                    Column(
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        toolRows.forEach { rowItems ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                rowItems.forEach { (icon, name, action) ->
                                    Column(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(12.dp))
                                            .clickable { action() }
                                            .padding(vertical = 12.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(46.dp)
                                                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f), CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = icon,
                                                contentDescription = name,
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                        Text(
                                            text = name,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                                val placeholders = columns - rowItems.size
                                if (placeholders > 0) {
                                    repeat(placeholders) {
                                        Spacer(modifier = Modifier.weight(1f))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SafetyWarningScreen(
    blockedUrl: String,
    onGoHome: () -> Unit,
    onGoBack: () -> Unit,
    onOpenSettings: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(76.dp)
                .background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Security,
                contentDescription = "Safe Shield active",
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(40.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Parental Mode Restricted",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "This webpage has been blocked under active minor-safe browsing guidelines and terms.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (blockedUrl.isNotEmpty()) {
            Text(
                text = blockedUrl,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            )
        }

        Spacer(modifier = Modifier.height(28.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
        ) {
            Button(
                onClick = onGoHome,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1f).testTag("block_go_home_btn")
            ) {
                Icon(imageVector = Icons.Default.Home, contentDescription = "Home", modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Home", style = MaterialTheme.typography.bodyMedium)
            }

            Button(
                onClick = onGoBack,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1f).testTag("block_go_back_btn")
            ) {
                Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Go Back", style = MaterialTheme.typography.bodyMedium)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(
            onClick = onOpenSettings,
            modifier = Modifier.testTag("block_open_settings_btn")
        ) {
            Icon(imageVector = Icons.Default.Settings, contentDescription = "Settings", modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Open Safety Settings", style = MaterialTheme.typography.bodyMedium)
        }
    }
}
