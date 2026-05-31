package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class BrowserRepository(private val browserDao: BrowserDao) {

    val allShortcuts: Flow<List<ShortcutEntity>> = browserDao.getAllShortcuts()
    val allHistory: Flow<List<HistoryEntity>> = browserDao.getAllHistory()
    val allTabs: Flow<List<TabEntity>> = browserDao.getAllTabs()
    val allBookmarks: Flow<List<BookmarkEntity>> = browserDao.getAllBookmarks()
    val allDownloads: Flow<List<DownloadEntity>> = browserDao.getAllDownloads()

    suspend fun initializeDefaults() {
        // Delete private tabs on startup
        browserDao.deletePrivateTabs()

        // Sync or default tabs: if no tabs exist, create a default Home tab
        val currentTabs = browserDao.getAllTabs().first()
        if (currentTabs.isEmpty()) {
            browserDao.insertTab(TabEntity(url = "home", title = "Home", isPrivate = false, isSelected = true))
        }

        // Pre-populate shortcuts if empty
        val currentShortcuts = allShortcuts.first()
        if (currentShortcuts.isEmpty()) {
            val defaults = listOf(
                ShortcutEntity(name = "Google", url = "https://www.google.com", isDefault = true),
                ShortcutEntity(name = "YouTube", url = "https://www.youtube.com", isDefault = true),
                ShortcutEntity(name = "Instagram", url = "https://www.instagram.com", isDefault = true),
                ShortcutEntity(name = "Telegram", url = "https://web.telegram.org", isDefault = true),
                ShortcutEntity(name = "Wikipedia", url = "https://www.wikipedia.org", isDefault = true)
            )
            for (shortcut in defaults) {
                browserDao.insertShortcut(shortcut)
            }
        }

        // Pre-populate search engine key if empty
        val currentEngine = browserDao.getSettingByKey("search_engine")
        if (currentEngine == null) {
            browserDao.insertSetting(SettingEntity("search_engine", "DuckDuckGo"))
        }

        // Minor-safe mode: default false
        if (browserDao.getSettingByKey("minor_safe_mode") == null) {
            browserDao.insertSetting(SettingEntity("minor_safe_mode", "false"))
        }

        // Adult content block: default false
        if (browserDao.getSettingByKey("adult_site_blocking") == null) {
            browserDao.insertSetting(SettingEntity("adult_site_blocking", "false"))
        }
    }

    suspend fun insertShortcut(shortcut: ShortcutEntity) {
        browserDao.insertShortcut(shortcut)
    }

    suspend fun updateShortcut(shortcut: ShortcutEntity) {
        browserDao.updateShortcut(shortcut)
    }

    suspend fun deleteShortcut(shortcut: ShortcutEntity) {
        browserDao.deleteShortcut(shortcut)
    }

    fun getSettingFlow(key: String, defaultValue: String): Flow<String> {
        return browserDao.getSettingFlowByKey(key).map { setting ->
            setting?.value ?: defaultValue
        }
    }

    suspend fun getSetting(key: String, defaultValue: String): String {
        return browserDao.getSettingByKey(key)?.value ?: defaultValue
    }

    suspend fun saveSetting(key: String, value: String) {
        browserDao.insertSetting(SettingEntity(key, value))
    }

    suspend fun addHistory(title: String, url: String) {
        // Prevent duplicate history flooding for the exact same URL right after each other
        val currentList = allHistory.first()
        if (currentList.isNotEmpty() && currentList.first().url == url) {
            return
        }
        browserDao.insertHistory(HistoryEntity(title = title, url = url))
    }

    suspend fun clearHistory() {
        browserDao.clearHistory()
    }

    suspend fun deleteHistory(history: HistoryEntity) {
        browserDao.deleteHistory(history)
    }

    // --- Tabs Operations ---
    suspend fun insertTab(tab: TabEntity): Long {
        return browserDao.insertTab(tab)
    }

    suspend fun updateTab(tab: TabEntity) {
        browserDao.updateTab(tab)
    }

    suspend fun deleteTab(tab: TabEntity) {
        browserDao.deleteTab(tab)
    }

    suspend fun clearAllTabs() {
        browserDao.clearAllTabs()
    }

    suspend fun deletePrivateTabs() {
        browserDao.deletePrivateTabs()
    }

    // --- Bookmarks Operations ---
    suspend fun insertBookmark(bookmark: BookmarkEntity) {
        browserDao.insertBookmark(bookmark)
    }

    suspend fun deleteBookmark(bookmark: BookmarkEntity) {
        browserDao.deleteBookmark(bookmark)
    }

    suspend fun deleteBookmarkByUrl(url: String) {
        browserDao.deleteBookmarkByUrl(url)
    }

    suspend fun getBookmarkByUrl(url: String): BookmarkEntity? {
        return browserDao.getBookmarkByUrl(url)
    }

    fun getBookmarkFlowByUrl(url: String): Flow<BookmarkEntity?> {
        return browserDao.getBookmarkFlowByUrl(url)
    }

    suspend fun clearAllBookmarks() {
        browserDao.clearAllBookmarks()
    }

    // --- Downloads Operations ---
    suspend fun insertDownload(download: DownloadEntity) {
        browserDao.insertDownload(download)
    }

    suspend fun deleteDownload(download: DownloadEntity) {
        browserDao.deleteDownload(download)
    }

    suspend fun clearAllDownloads() {
        browserDao.clearAllDownloads()
    }

    suspend fun resetShortcutsToDefault() {
        browserDao.clearAllShortcuts()
        val defaults = listOf(
            ShortcutEntity(name = "Google", url = "https://www.google.com", isDefault = true),
            ShortcutEntity(name = "YouTube", url = "https://www.youtube.com", isDefault = true),
            ShortcutEntity(name = "Instagram", url = "https://www.instagram.com", isDefault = true),
            ShortcutEntity(name = "Telegram", url = "https://web.telegram.org", isDefault = true),
            ShortcutEntity(name = "Wikipedia", url = "https://www.wikipedia.org", isDefault = true)
        )
        for (shortcut in defaults) {
            browserDao.insertShortcut(shortcut)
        }
    }
}
