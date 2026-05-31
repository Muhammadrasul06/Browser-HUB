package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.BrowserRepository
import com.example.data.ShortcutEntity
import com.example.data.BrowserSafetyFilter
import com.example.data.TabEntity
import com.example.data.BookmarkEntity
import com.example.data.DownloadEntity
import com.example.data.HistoryEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class OpenTarget {
    CURRENT_TAB,
    NEW_TAB
}

class BrowserViewModel(
    application: Application,
    private val repository: BrowserRepository
) : AndroidViewModel(application) {

    // Web shortcuts
    val shortcuts: StateFlow<List<ShortcutEntity>> = repository.allShortcuts
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // User browsing history
    val history = repository.allHistory
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Tabs
    val tabs: StateFlow<List<TabEntity>> = repository.allTabs
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Selected active tab (Flow-derived)
    val selectedTab: StateFlow<TabEntity?> = repository.allTabs
        .map { list -> list.find { it.isSelected } ?: list.firstOrNull() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    // Bookmarks
    val bookmarks: StateFlow<List<BookmarkEntity>> = repository.allBookmarks
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Downloads
    val downloads: StateFlow<List<DownloadEntity>> = repository.allDownloads
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Search Engine selector
    private val _searchEngine = MutableStateFlow("DuckDuckGo")
    val searchEngine: StateFlow<String> = _searchEngine.asStateFlow()

    // Minor safe mode (safe parameters appending)
    private val _minorSafeMode = MutableStateFlow(false)
    val minorSafeMode: StateFlow<Boolean> = _minorSafeMode.asStateFlow()

    // Adult Web Filter
    private val _adultSiteBlocking = MutableStateFlow(false)
    val adultSiteBlocking: StateFlow<Boolean> = _adultSiteBlocking.asStateFlow()

    // Hashed Safety PIN passcode
    private val _safetyPinHash = MutableStateFlow("")
    val safetyPinHash: StateFlow<String> = _safetyPinHash.asStateFlow()

    // Custom blocked domains list
    private val _customBlockedDomains = MutableStateFlow<List<String>>(emptyList())
    val customBlockedDomains: StateFlow<List<String>> = _customBlockedDomains.asStateFlow()

    // Custom unsafe keywords list
    private val _customUnsafeKeywords = MutableStateFlow<List<String>>(emptyList())
    val customUnsafeKeywords: StateFlow<List<String>> = _customUnsafeKeywords.asStateFlow()

    // Active state of WebView
    private val _currentBrowserUrl = MutableStateFlow("")
    val currentBrowserUrl: StateFlow<String> = _currentBrowserUrl.asStateFlow()

    private val _currentNavTargetUrl = MutableStateFlow("")
    val currentNavTargetUrl: StateFlow<String> = _currentNavTargetUrl.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _loadProgress = MutableStateFlow(0)
    val loadProgress: StateFlow<Int> = _loadProgress.asStateFlow()

    // For tracking browser titles, active states, tab controls
    private val _browserTitle = MutableStateFlow("Browser")
    val browserTitle: StateFlow<String> = _browserTitle.asStateFlow()

    init {
        viewModelScope.launch {
            repository.initializeDefaults()
            // Pull initial configurations
            _searchEngine.value = repository.getSetting("search_engine", "DuckDuckGo")
            _minorSafeMode.value = repository.getSetting("minor_safe_mode", "false").toBoolean()
            _adultSiteBlocking.value = repository.getSetting("adult_site_blocking", "false").toBoolean()
            _safetyPinHash.value = repository.getSetting("safety_pin_hash", "")
            
            val domainsRaw = repository.getSetting("custom_blocked_domains", "")
            _customBlockedDomains.value = if (domainsRaw.isBlank()) emptyList() else domainsRaw.split(",").map { it.trim() }.filter { it.isNotEmpty() }
            
            val keywordsRaw = repository.getSetting("custom_unsafe_keywords", "")
            _customUnsafeKeywords.value = if (keywordsRaw.isBlank()) emptyList() else keywordsRaw.split(",").map { it.trim() }.filter { it.isNotEmpty() }
        }

        viewModelScope.launch {
            selectedTab.collect { tab ->
                if (tab != null) {
                    val targetUrl = if (tab.url == "home") "https://www.google.com" else tab.url
                    val normTarget = targetUrl.trim().removeSuffix("/")
                    val normCurrent = _currentNavTargetUrl.value.trim().removeSuffix("/")
                    if (normTarget != normCurrent) {
                        _currentNavTargetUrl.value = targetUrl
                    }
                }
            }
        }
    }

    fun updateLoadingState(loading: Boolean, progress: Int) {
        _isLoading.value = loading
        _loadProgress.value = progress
    }

    private var lastSavedHistoryUrl: String = ""
    private var lastSavedHistoryTime: Long = 0L

    fun updateBrowserInfo(title: String, url: String) {
        // Core Protection: if Minor Safe mode is active, do not update tabs or history with blocked URLs or search terms
        if (_minorSafeMode.value && BrowserSafetyFilter.isUrlBlocked(url, _customBlockedDomains.value, _customUnsafeKeywords.value)) {
            return
        }

        _browserTitle.value = if (title.isBlank()) "Web Page" else title
        _currentBrowserUrl.value = url
        
        val active = selectedTab.value
        viewModelScope.launch {
            if (active != null) {
                // Update this specific tab's URL and title in database
                repository.updateTab(active.copy(url = url, title = if (title.isBlank()) "Web Page" else title))
                
                // Save history only for normal/non-private tabs
                if (!active.isPrivate && url.isNotBlank() && !url.startsWith("about:") && !url.startsWith("file:") && !url.equals("home", ignoreCase = true)) {
                    val now = System.currentTimeMillis()
                    if (url != lastSavedHistoryUrl || (now - lastSavedHistoryTime) > 5000) {
                        lastSavedHistoryUrl = url
                        lastSavedHistoryTime = now
                        repository.addHistory(if (title.isBlank()) url else title, url)
                    }
                }
            }
        }
    }

    /**
     * Toggles default search engine settings
     */
    fun updateSearchEngine(engine: String) {
        _searchEngine.value = engine
        viewModelScope.launch {
            repository.saveSetting("search_engine", engine)
        }
    }

    /**
     * Toggles minor-safe search parameters
     */
    fun updateMinorSafeMode(enabled: Boolean) {
        _minorSafeMode.value = enabled
        viewModelScope.launch {
            repository.saveSetting("minor_safe_mode", enabled.toString())
        }
    }

    /**
     * Toggles aggressive URL adult site blocking filters
     */
    fun updateAdultSiteBlocking(enabled: Boolean) {
        _adultSiteBlocking.value = enabled
        viewModelScope.launch {
            repository.saveSetting("adult_site_blocking", enabled.toString())
        }
    }

    /**
     * Custom Shortcuts CRUD Operations
     */
    fun addShortcut(name: String, url: String) {
        viewModelScope.launch {
            repository.insertShortcut(ShortcutEntity(name = name, url = url, isDefault = false))
        }
    }

    fun updateShortcut(shortcut: ShortcutEntity) {
        viewModelScope.launch {
            repository.updateShortcut(shortcut)
        }
    }

    fun deleteShortcut(shortcut: ShortcutEntity) {
        viewModelScope.launch {
            repository.deleteShortcut(shortcut)
        }
    }

    fun resetShortcutsToDefault() {
        viewModelScope.launch {
            repository.resetShortcutsToDefault()
        }
    }

    /**
     * Browsing History clear
     */
    fun clearBrowserHistory() {
        viewModelScope.launch {
            repository.clearHistory()
        }
    }

    fun deleteHistoryItem(historyItem: HistoryEntity) {
        viewModelScope.launch {
            repository.deleteHistory(historyItem)
        }
    }

    // --- Tabs Management ---
    fun openNewTab(url: String = "home", isPrivate: Boolean = false) {
        val targetUrl = if (url == "home") "https://www.google.com" else resolveInput(url)
        _currentNavTargetUrl.value = targetUrl
        viewModelScope.launch {
            val currentList = tabs.value
            for (t in currentList) {
                if (t.isSelected) {
                    repository.updateTab(t.copy(isSelected = false))
                }
            }
            repository.insertTab(TabEntity(url = url, title = if (url == "home") "Home" else "New Tab", isPrivate = isPrivate, isSelected = true))
        }
    }

    fun openInput(input: String, target: OpenTarget) {
        val resolvedUrl = resolveInput(input)
        if (resolvedUrl.isBlank()) return

        if (target == OpenTarget.NEW_TAB) {
            openNewTab(resolvedUrl)
        } else {
            val active = selectedTab.value
            if (active != null) {
                _currentNavTargetUrl.value = resolvedUrl
                updateBrowserInfo("Loading...", resolvedUrl)
            } else {
                openNewTab(resolvedUrl)
            }
        }
    }

    fun openUrlInCurrentTab(url: String) {
        openInput(url, OpenTarget.CURRENT_TAB)
    }

    fun createNewTab(url: String) {
        openInput(url, OpenTarget.NEW_TAB)
    }

    fun closeTab(tab: TabEntity) {
        viewModelScope.launch {
            repository.deleteTab(tab)
            if (tab.isSelected) {
                val remaining = tabs.value.filter { it.id != tab.id }
                if (remaining.isNotEmpty()) {
                    repository.updateTab(remaining.first().copy(isSelected = true))
                } else {
                    repository.insertTab(TabEntity(url = "home", title = "Home", isPrivate = false, isSelected = true))
                }
            }
        }
    }

    fun selectTab(tab: TabEntity) {
        viewModelScope.launch {
            val currentList = tabs.value
            for (t in currentList) {
                val shouldBeSelected = t.id == tab.id
                if (t.isSelected != shouldBeSelected) {
                    repository.updateTab(t.copy(isSelected = shouldBeSelected))
                }
            }
        }
    }

    // --- Bookmarks Management ---
    fun toggleBookmark(title: String, url: String) {
        // Prevent bookmarking unsafe or blocked pages when Minor Safe Mode is ON
        if (_minorSafeMode.value && BrowserSafetyFilter.isUrlBlocked(url, _customBlockedDomains.value, _customUnsafeKeywords.value)) {
            return
        }
        viewModelScope.launch {
            val existing = repository.getBookmarkByUrl(url)
            if (existing != null) {
                repository.deleteBookmark(existing)
            } else {
                repository.insertBookmark(
                    BookmarkEntity(
                        title = if (title.isBlank()) url else title,
                        url = url
                    )
                )
            }
        }
    }

    fun isBookmarked(url: String): Boolean {
        return bookmarks.value.any { it.url == url }
    }

    fun deleteBookmark(bookmark: BookmarkEntity) {
        viewModelScope.launch {
            repository.deleteBookmark(bookmark)
        }
    }

    fun clearAllBookmarks() {
        viewModelScope.launch {
            repository.clearAllBookmarks()
        }
    }

    // --- Safety PIN and Filters Operations ---
    fun setSafetyPin(pin: String) {
        val hash = BrowserSafetyFilter.hashPin(pin)
        _safetyPinHash.value = hash
        viewModelScope.launch {
            repository.saveSetting("safety_pin_hash", hash)
        }
    }

    fun checkSafetyPin(pin: String): Boolean {
        if (_safetyPinHash.value.isEmpty()) return true
        return BrowserSafetyFilter.hashPin(pin) == _safetyPinHash.value
    }

    fun addCustomBlockedDomain(domain: String) {
        val normalized = BrowserSafetyFilter.normalizeDomain(domain)
        if (normalized.isEmpty()) return
        val current = _customBlockedDomains.value.toMutableList()
        if (!current.contains(normalized)) {
            current.add(normalized)
            _customBlockedDomains.value = current
            viewModelScope.launch {
                repository.saveSetting("custom_blocked_domains", current.joinToString(","))
            }
        }
    }

    fun deleteCustomBlockedDomain(domain: String) {
        val current = _customBlockedDomains.value.toMutableList()
        if (current.remove(domain)) {
            _customBlockedDomains.value = current
            viewModelScope.launch {
                repository.saveSetting("custom_blocked_domains", current.joinToString(","))
            }
        }
    }

    fun addCustomUnsafeKeyword(word: String) {
        val normalized = word.trim().lowercase()
        if (normalized.isEmpty()) return
        val current = _customUnsafeKeywords.value.toMutableList()
        if (!current.contains(normalized)) {
            current.add(normalized)
            _customUnsafeKeywords.value = current
            viewModelScope.launch {
                repository.saveSetting("custom_unsafe_keywords", current.joinToString(","))
            }
        }
    }

    fun deleteCustomUnsafeKeyword(word: String) {
        val current = _customUnsafeKeywords.value.toMutableList()
        if (current.remove(word)) {
            _customUnsafeKeywords.value = current
            viewModelScope.launch {
                repository.saveSetting("custom_unsafe_keywords", current.joinToString(","))
            }
        }
    }

    fun resetSafetySettings() {
        _minorSafeMode.value = false
        _adultSiteBlocking.value = false
        _safetyPinHash.value = ""
        _customBlockedDomains.value = emptyList()
        _customUnsafeKeywords.value = emptyList()
        viewModelScope.launch {
            repository.saveSetting("minor_safe_mode", "false")
            repository.saveSetting("adult_site_blocking", "false")
            repository.saveSetting("safety_pin_hash", "")
            repository.saveSetting("custom_blocked_domains", "")
            repository.saveSetting("custom_unsafe_keywords", "")
        }
    }

    // --- Downloads Management ---
    fun addDownload(id: Long, filename: String, url: String) {
        viewModelScope.launch {
            repository.insertDownload(
                DownloadEntity(
                    downloadId = id,
                    filename = filename,
                    url = url,
                    status = "Downloading"
                )
            )
        }
    }

    fun updateDownloadStatus(id: Long, status: String) {
        viewModelScope.launch {
            val currentList = downloads.value
            val existing = currentList.find { it.downloadId == id }
            if (existing != null) {
                repository.insertDownload(existing.copy(status = status))
            }
        }
    }

    fun deleteDownload(download: DownloadEntity) {
        viewModelScope.launch {
            repository.deleteDownload(download)
        }
    }

    fun clearAllDownloads() {
        viewModelScope.launch {
            repository.clearAllDownloads()
        }
    }

    // --- Reset All Data ---
    fun resetAllBrowserData() {
        viewModelScope.launch {
            repository.clearHistory()
            repository.clearAllBookmarks()
            repository.clearAllDownloads()
            repository.clearAllTabs()
            repository.resetShortcutsToDefault()
            resetSafetySettings()
            // Create a single clean Home tab
            repository.insertTab(TabEntity(url = "home", title = "Home", isPrivate = false, isSelected = true))
        }
    }

    /**
     * Input Evaluator for search queries vs valid URL redirection
     */
    fun resolveInput(input: String): String {
        val trimmed = input.trim()
        if (trimmed.isEmpty()) return ""

        // Check if it's already a full URI scheme
        if (trimmed.startsWith("http://", ignoreCase = true) || trimmed.startsWith("https://", ignoreCase = true)) {
            if (_minorSafeMode.value && BrowserSafetyFilter.isUrlBlocked(trimmed, _customBlockedDomains.value, _customUnsafeKeywords.value)) {
                return "about:blocked?url=${android.net.Uri.encode(trimmed)}"
            }
            return trimmed
        }

        if (trimmed.startsWith("about:", ignoreCase = true) || trimmed.startsWith("file:", ignoreCase = true)) {
            return trimmed
        }

        // Check if it appears to be a domain (contains dot, no spaces)
        val isDomain = !trimmed.contains(" ") && trimmed.contains(".") && trimmed.substringAfterLast(".").all { it.isLetter() }
        if (isDomain) {
            val url = "https://$trimmed"
            if (_minorSafeMode.value && BrowserSafetyFilter.isUrlBlocked(url, _customBlockedDomains.value, _customUnsafeKeywords.value)) {
                return "about:blocked?url=${android.net.Uri.encode(url)}"
            }
            return url
        }

        // Under minor safe mode, if search keyword is unsafe, return as blocked query URL
        if (_minorSafeMode.value && BrowserSafetyFilter.isSearchQueryBlocked(trimmed, _customUnsafeKeywords.value)) {
            return "about:blocked?query=${android.net.Uri.encode(trimmed)}"
        }

        // Return engine-specific formatted query URL
        return if (_minorSafeMode.value) {
            BrowserSafetyFilter.buildSafeSearchUrl(trimmed, _searchEngine.value)
        } else {
            BrowserSafetyFilter.modifySearchQuery(trimmed, _searchEngine.value, false)
        }
    }

    /**
     * Factory class to build the ViewModel with required Database/Repository injections
     */
    class Factory(private val application: Application) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(BrowserViewModel::class.java)) {
                val database = AppDatabase.getDatabase(application)
                val repository = BrowserRepository(database.browserDao())
                @Suppress("UNCHECKED_CAST")
                return BrowserViewModel(application, repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
