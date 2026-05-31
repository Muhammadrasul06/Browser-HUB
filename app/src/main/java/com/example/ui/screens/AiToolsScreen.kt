package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.data.AiTool
import com.example.data.AiToolsProvider
import com.example.data.BrowserSafetyFilter
import com.example.ui.BrowserViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiToolsScreen(
    viewModel: BrowserViewModel,
    onNavigateBack: () -> Unit,
    onOpenUrl: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val coroutineScope = rememberCoroutineScope()

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }
    var selectedPricing by remember { mutableStateOf("All") }

    var selectedToolForBottomSheet by remember { mutableStateOf<AiTool?>(null) }
    val sheetState = rememberModalBottomSheetState()

    val categories = listOf(
        "All", "Chatbots", "Writing", "Coding", "Image", "Video",
        "Audio", "Productivity", "Research", "Design", "Education", "Business", "Other"
    )

    val pricingOptions = listOf("All", "Free", "Paid", "Freemium")

    // Collect safety settings to respect active parental controls
    val isMinorSafe by viewModel.minorSafeMode.collectAsState()
    val isAdultBlocking by viewModel.adultSiteBlocking.collectAsState()
    val customDomains by viewModel.customBlockedDomains.collectAsState()
    val customKeywords by viewModel.customUnsafeKeywords.collectAsState()

    // Filter tools dynamically based on search, category, and pricing constraints
    val filteredTools = remember(searchQuery, selectedCategory, selectedPricing) {
        AiToolsProvider.tools.filter { tool ->
            // Category filter
            val matchesCategory = if (selectedCategory == "All") {
                true
            } else {
                tool.category.equals(selectedCategory, ignoreCase = true)
            }

            // Pricing filter
            val matchesPricing = if (selectedPricing == "All") {
                true
            } else {
                tool.pricing.equals(selectedPricing, ignoreCase = true)
            }

            // Search query filter (name, category, useful/goodFor text)
            val matchesSearch = if (searchQuery.isBlank()) {
                true
            } else {
                tool.name.contains(searchQuery, ignoreCase = true) ||
                        tool.category.contains(searchQuery, ignoreCase = true) ||
                        tool.goodFor.contains(searchQuery, ignoreCase = true)
            }

            matchesCategory && matchesPricing && matchesSearch
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "AI Tools Catalog",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Browse outstanding directory resources",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("ai_tools_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Navigate Back"
                        )
                    }
                },
                actions = {
                    Box {
                        var expandedPricingMenu by remember { mutableStateOf(false) }
                        IconButton(
                            onClick = { expandedPricingMenu = true },
                            modifier = Modifier.testTag("pricing_filter_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.FilterList,
                                contentDescription = "Filter Pricing",
                                tint = if (selectedPricing != "All") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        DropdownMenu(
                            expanded = expandedPricingMenu,
                            onDismissRequest = { expandedPricingMenu = false }
                        ) {
                            pricingOptions.forEach { opt ->
                                DropdownMenuItem(
                                    text = { Text(if (opt == "All") "All Prices" else opt) },
                                    onClick = {
                                        selectedPricing = opt
                                        expandedPricingMenu = false
                                    },
                                    leadingIcon = {
                                        if (selectedPricing == opt) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = "Selected",
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                )
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
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
        ) {
            // Search Input Row
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search tools",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(end = 8.dp)
                    )

                    TextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = {
                            Text(
                                text = "Search name, category, utility...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        },
                        singleLine = true,
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            disabledContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(onSearch = { keyboardController?.hide() }),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("search_tools_input")
                    )

                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear search",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Category Chips Row
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(categories) { cat ->
                    val isSelected = selectedCategory == cat
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedCategory = cat },
                        label = { Text(cat) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.testTag("category_chip_$cat")
                    )
                }
            }

            // Grid catalog of beautiful AI Tools
            if (filteredTools.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.SmartToy,
                            contentDescription = "Empty Catalog",
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = if (searchQuery.isNotEmpty()) "No tools found" else "No tools in this category",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Try clearing search queries or choosing another filter category.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 160.dp),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f).fillMaxWidth()
                ) {
                    items(filteredTools) { tool ->
                        AiToolCard(
                            tool = tool,
                            onClick = {
                                selectedToolForBottomSheet = tool
                            },
                            onOpenWebsite = {
                                openToolWebsite(
                                    context = context,
                                    viewModel = viewModel,
                                    url = tool.websiteUrl,
                                    isMinorSafe = isMinorSafe,
                                    isAdultBlocking = isAdultBlocking,
                                    customDomains = customDomains,
                                    customKeywords = customKeywords,
                                    onOpenUrl = onOpenUrl
                                )
                            }
                        )
                    }
                }
            }
        }
    }

    // Modal Bottom Sheet Detail Drawer
    if (selectedToolForBottomSheet != null) {
        val tool = selectedToolForBottomSheet!!
        ModalBottomSheet(
            onDismissRequest = { selectedToolForBottomSheet = null },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface,
            scrimColor = Color.Black.copy(alpha = 0.5f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Large header icon/logo
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    if (!tool.logoUrl.isNullOrEmpty()) {
                        AsyncImage(
                            model = tool.logoUrl,
                            contentDescription = "${tool.name} Logo",
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            error = rememberVectorPainter(Icons.Default.SmartToy),
                            fallback = rememberVectorPainter(Icons.Default.SmartToy)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.SmartToy,
                            contentDescription = "Standard Fallback",
                            modifier = Modifier.size(32.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = tool.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Tag Chips
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SuggestionChip(
                        onClick = {},
                        label = { Text(tool.category) },
                        modifier = Modifier.testTag("sheet_category_tag")
                    )

                    SuggestionChip(
                        onClick = {},
                        label = { Text(tool.pricing) },
                        colors = SuggestionChipDefaults.suggestionChipColors(
                            labelColor = MaterialTheme.colorScheme.secondary
                        ),
                        modifier = Modifier.testTag("sheet_pricing_tag")
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Usefulness insight Box
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "GOOD FOR",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = tool.goodFor,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                // Actions Layout
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Copy link button
                    OutlinedButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("AI Tool Website", tool.websiteUrl)
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, "Link copied: ${tool.name}", Toast.LENGTH_SHORT).show()
                        },
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .testTag("sheet_copy_link_btn")
                    ) {
                        Icon(imageVector = Icons.Default.ContentCopy, contentDescription = "Copy")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Copy Link")
                    }

                    // Open Web browser button
                    val isUrlAllowed = remember(tool.websiteUrl, isMinorSafe, isAdultBlocking, customDomains, customKeywords) {
                        BrowserSafetyFilter.isUrlAllowed(
                            url = tool.websiteUrl,
                            isMinorSafe = isMinorSafe,
                            isAdultBlocking = isAdultBlocking,
                            customDomains = customDomains,
                            customKeywords = customKeywords
                        )
                    }

                    Button(
                        onClick = {
                            coroutineScope.launch { sheetState.hide() }.invokeOnCompletion {
                                selectedToolForBottomSheet = null
                                openToolWebsite(
                                    context = context,
                                    viewModel = viewModel,
                                    url = tool.websiteUrl,
                                    isMinorSafe = isMinorSafe,
                                    isAdultBlocking = isAdultBlocking,
                                    customDomains = customDomains,
                                    customKeywords = customKeywords,
                                    onOpenUrl = onOpenUrl
                                )
                            }
                        },
                        shape = RoundedCornerShape(14.dp),
                        enabled = tool.websiteUrl.isNotEmpty(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isUrlAllowed) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .testTag("sheet_open_web_btn")
                    ) {
                        Icon(
                            imageVector = if (isUrlAllowed) Icons.Default.Launch else Icons.Default.Security,
                            contentDescription = "Launch"
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (tool.websiteUrl.isEmpty()) {
                                "Website unavailable"
                            } else if (!isUrlAllowed) {
                                "Restricted"
                            } else {
                                "Open Website"
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AiToolCard(
    tool: AiTool,
    onClick: () -> Unit,
    onOpenWebsite: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Logo Icon Box
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    if (!tool.logoUrl.isNullOrEmpty()) {
                        AsyncImage(
                            model = tool.logoUrl,
                            contentDescription = "${tool.name} Logo",
                            modifier = Modifier
                                .size(24.dp)
                                .clip(RoundedCornerShape(6.dp)),
                            error = rememberVectorPainter(Icons.Default.SmartToy),
                            fallback = rememberVectorPainter(Icons.Default.SmartToy)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.SmartToy,
                            contentDescription = "Fallback icon",
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = tool.name,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = tool.category,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Good for (brief content utility)
            Text(
                text = tool.goodFor,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Pricing Indicator + Action Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = tool.pricing,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.8f),
                    fontWeight = FontWeight.Medium
                )

                IconButton(
                    onClick = onOpenWebsite,
                    modifier = Modifier.size(32.dp).testTag("open_tool_${tool.name}")
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = "Open ${tool.name}",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

/**
 * Universal safe executor to open a tool website in existing WebView
 */
fun openToolWebsite(
    context: Context,
    viewModel: BrowserViewModel,
    url: String,
    isMinorSafe: Boolean,
    isAdultBlocking: Boolean,
    customDomains: List<String>,
    customKeywords: List<String>,
    onOpenUrl: (String) -> Unit
) {
    if (url.isEmpty()) {
        Toast.makeText(context, "Website unavailable", Toast.LENGTH_SHORT).show()
        return
    }

    val allowed = BrowserSafetyFilter.isUrlAllowed(
        url = url,
        isMinorSafe = isMinorSafe,
        isAdultBlocking = isAdultBlocking,
        customDomains = customDomains,
        customKeywords = customKeywords
    )

    if (!allowed) {
        // Enforce Minor Protection limits: Show Toast warning & open blocked placeholder link inside Existing Browser
        Toast.makeText(context, "Content Restricted: Violates parental safety limits.", Toast.LENGTH_LONG).show()
        val blockedUrl = "about:blocked?url=${android.net.Uri.encode(url)}"
        viewModel.openNewTab(blockedUrl)
        onOpenUrl(blockedUrl)
    } else {
        // Create new tab and route inside Existing Browser WebView (No external redirection)
        viewModel.openNewTab(url)
        onOpenUrl(url)
    }
}
