package com.example.data

import android.net.Uri
import java.security.MessageDigest

object BrowserSafetyFilter {

    // Default blocked domains (standard patterns representing adult and gambling categories)
    private val defaultBlockedDomains = listOf(
        "pornhub.com", "xvideos.com", "xnxx.com", "xhamster.com", "onlyfans.com", 
        "chaturbate.com", "redtube.com", "youporn.com", "gambling.com", "bet365.com", 
        "pokerstars.com", "casino.com", "jackpoty.com", "bovada.lv", "draftkings.com",
        "sportybet.com", "porn.com", "xxx.com"
    )

    // Default unsafe keywords for searches and queries
    private val defaultUnsafeKeywords = listOf(
        "porn", "xxx", "sex", "erotic", "nude", "naked", "casino", "gamble", 
        "betting", "strip", "onlyfans", "playboy", "hustler"
    )

    /**
     * Normalizes a URL or domain string to lower-case, stripping schemes and 'www.' prefix.
     * Example: "https://www.google.com/search?q=test" -> "google.com"
     */
    fun normalizeDomain(urlOrDomain: String): String {
        var trimmed = urlOrDomain.trim().lowercase()
        if (trimmed.startsWith("https://")) trimmed = trimmed.substring(8)
        if (trimmed.startsWith("http://")) trimmed = trimmed.substring(7)
        if (trimmed.startsWith("www.")) trimmed = trimmed.substring(4)
        
        // Strip everything after slash or port if exist
        val slashIndex = trimmed.indexOf('/')
        if (slashIndex != -1) {
            trimmed = trimmed.substring(0, slashIndex)
        }
        val colonIndex = trimmed.indexOf(':')
        if (colonIndex != -1) {
            trimmed = trimmed.substring(0, colonIndex)
        }
        return trimmed
    }

    /**
     * Checks if a domain/host itself is in the default or custom blocklist.
     * Respects exact matching or subdomain matching.
     */
    fun isDomainBlocked(host: String, customDomains: List<String>): Boolean {
        if (host.isBlank()) return false
        val normHost = normalizeDomain(host)

        // 1. Check default blocked domains
        for (blocked in defaultBlockedDomains) {
            if (normHost == blocked || normHost.endsWith(".$blocked")) {
                return true
            }
        }

        // 2. Check custom blocked domains
        for (custom in customDomains) {
            val normCustom = normalizeDomain(custom)
            if (normCustom.isNotEmpty()) {
                if (normHost == normCustom || normHost.endsWith(".$normCustom")) {
                    return true
                }
            }
        }

        return false
    }

    /**
     * Checks if a given search query contains any unsafe terms.
     */
    fun isSearchQueryBlocked(query: String, customKeywords: List<String>): Boolean {
        val normQuery = query.lowercase().trim()
        if (normQuery.isEmpty()) return false

        val words = normQuery.split(Regex("\\s+"))

        // Check defaults
        for (kw in defaultUnsafeKeywords) {
            if (normQuery == kw || words.contains(kw) || normQuery.contains(" $kw") || normQuery.contains("$kw ")) {
                return true
            }
        }

        // Check custom
        for (kw in customKeywords) {
            val normKw = kw.lowercase().trim()
            if (normKw.isNotEmpty()) {
                if (normQuery == normKw || words.contains(normKw) || normQuery.contains(" $normKw") || normQuery.contains("$normKw ")) {
                    return true
                }
            }
        }

        return false
    }

    /**
     * High-level check to determine if a URL navigation should be blocked.
     */
    fun shouldBlockNavigation(url: String, customDomains: List<String>, customKeywords: List<String>): Boolean {
        return isUrlBlocked(url, customDomains, customKeywords)
    }

    /**
     * Checks whether a URL or query is blocked.
     */
    fun isUrlBlocked(url: String, customDomains: List<String>, customKeywords: List<String>): Boolean {
        val lowercaseUrl = url.lowercase().trim()
        if (lowercaseUrl.isEmpty()) return false

        // Extract host or normalize domain
        val host = try {
            val uri = Uri.parse(url)
            uri.host?.lowercase() ?: ""
        } catch (e: Exception) {
            ""
        }
        
        val targetHost = if (host.isNotEmpty()) host else normalizeDomain(lowercaseUrl)

        // 1. Check if the domain is directly blocked
        if (isDomainBlocked(targetHost, customDomains)) {
            return true
        }

        // 2. Check search queries inside search engines
        if (isSearchEngineUrl(lowercaseUrl)) {
            val query = extractSearchQuery(url)
            if (query != null && isSearchQueryBlocked(query, customKeywords)) {
                return true
            }
        }

        // 3. Fallback: Check if URL segments or paths contain explicit default blocked domain labels
        for (blocked in defaultBlockedDomains) {
            if (lowercaseUrl.contains("/$blocked") || lowercaseUrl.contains(".$blocked")) {
                return true
            }
        }

        for (custom in customDomains) {
            val normCustom = normalizeDomain(custom)
            if (normCustom.isNotEmpty()) {
                if (lowercaseUrl.contains("/$normCustom") || lowercaseUrl.contains(".$normCustom")) {
                    return true
                }
            }
        }

        // 4. Fallback: Quick path keyword scanner (avoid false-positives, check with surrounding separators)
        for (kw in defaultUnsafeKeywords) {
            if (lowercaseUrl.contains("/$kw") || lowercaseUrl.contains("=$kw") || lowercaseUrl.contains("-$kw") || lowercaseUrl.contains("_$kw")) {
                return true
            }
        }
        for (kw in customKeywords) {
            val normKw = kw.lowercase().trim()
            if (normKw.isNotEmpty()) {
                if (lowercaseUrl.contains("/$normKw") || lowercaseUrl.contains("=$normKw") || lowercaseUrl.contains("-$normKw") || lowercaseUrl.contains("_$normKw")) {
                    return true
                }
            }
        }

        return false
    }

    /**
     * Safe search URL construction
     */
    fun buildSafeSearchUrl(query: String, searchEngine: String): String {
        val baseUrl = when (searchEngine.lowercase()) {
            "google" -> "https://www.google.com/search?q="
            "microsoft bing", "bing" -> "https://www.bing.com/search?q="
            "yandex" -> "https://yandex.com/search/?text="
            "duckduckgo" -> "https://duckduckgo.com/?q="
            else -> "https://www.google.com/search?q="
        }

        val encodedQuery = Uri.encode(query)
        val fullUrl = "$baseUrl$encodedQuery"

        return when (searchEngine.lowercase()) {
            "google" -> "$fullUrl&safe=active"
            "microsoft bing", "bing" -> "$fullUrl&adlt=strict"
            "yandex" -> "$fullUrl&family=yes"
            "duckduckgo" -> "$fullUrl&kp=1"
            else -> "$fullUrl&safe=active"
        }
    }

    /**
     * Modifies existing search engine navigation URLs to enforce SafeSearch parameters dynamically.
     */
    fun enforceSafeSearchParameters(url: String): String {
        try {
            val uri = Uri.parse(url)
            val host = uri.host?.lowercase() ?: ""
            val scheme = uri.scheme ?: ""
            if (!scheme.startsWith("http")) return url

            val builder = uri.buildUpon()
            var modified = false

            if (host.contains("google.com") && uri.path?.contains("/search") == true) {
                val safeParam = uri.getQueryParameter("safe")
                if (safeParam != "active") {
                    builder.appendQueryParameter("safe", "active")
                    modified = true
                }
            } else if (host.contains("bing.com") && uri.path?.contains("/search") == true) {
                val adltParam = uri.getQueryParameter("adlt")
                if (adltParam != "strict") {
                    builder.appendQueryParameter("adlt", "strict")
                    modified = true
                }
            } else if ((host.contains("yandex.com") || host.contains("yandex.ru")) && uri.path?.contains("/search") == true) {
                val familyParam = uri.getQueryParameter("family")
                if (familyParam != "yes") {
                    builder.appendQueryParameter("family", "yes")
                    modified = true
                }
            } else if (host.contains("duckduckgo.com") && (uri.path == "/" || uri.path == "/index.html" || uri.path.isNullOrEmpty())) {
                val kpParam = uri.getQueryParameter("kp")
                if (kpParam != "1") {
                    builder.appendQueryParameter("kp", "1")
                    modified = true
                }
            }

            if (modified) {
                return builder.build().toString()
            }
        } catch (_: Exception) {}
        return url
    }

    private fun isSearchEngineUrl(url: String): Boolean {
        val host = try {
            Uri.parse(url).host?.lowercase() ?: ""
        } catch (e: Exception) {
            ""
        }
        return host.contains("google.com") || host.contains("bing.com") || host.contains("yandex.com") || host.contains("yandex.ru") || host.contains("duckduckgo.com")
    }

    private fun extractSearchQuery(url: String): String? {
        try {
            val uri = Uri.parse(url)
            val host = uri.host?.lowercase() ?: ""
            if (host.contains("google.com") || host.contains("bing.com") || host.contains("duckduckgo.com")) {
                return uri.getQueryParameter("q")
            } else if (host.contains("yandex.com") || host.contains("yandex.ru")) {
                return uri.getQueryParameter("text")
            }
        } catch (_: Exception) {}
        return null
    }

    /**
     * Generates a secure SHA-256 hash of a PIN code, preventing plain-text exposure.
     */
    fun hashPin(pin: String): String {
        try {
            val bytes = pin.toByteArray()
            val md = MessageDigest.getInstance("SHA-256")
            val digest = md.digest(bytes)
            return digest.fold("") { str, it -> str + "%02x".format(it) }
        } catch (e: Exception) {
            return pin // fallback
        }
    }

    /**
     * Compatibility helper to check if a URL is allowed under safety constraints
     */
    fun isUrlAllowed(url: String, isMinorSafe: Boolean, isAdultBlocking: Boolean, customDomains: List<String> = emptyList(), customKeywords: List<String> = emptyList()): Boolean {
        if (!isMinorSafe && !isAdultBlocking) return true
        return !isUrlBlocked(url, customDomains, customKeywords)
    }

    /**
     * Helper to modify and build standard or modified search query URLs
     */
    fun modifySearchQuery(query: String, searchEngine: String, isMinorSafe: Boolean): String {
        return if (isMinorSafe) {
            buildSafeSearchUrl(query, searchEngine)
        } else {
            val baseUrl = when (searchEngine.lowercase()) {
                "google" -> "https://www.google.com/search?q="
                "microsoft bing", "bing" -> "https://www.bing.com/search?q="
                "yandex" -> "https://yandex.com/search/?text="
                "duckduckgo" -> "https://duckduckgo.com/?q="
                else -> "https://www.google.com/search?q="
            }
            "$baseUrl${Uri.encode(query)}"
        }
    }
}
