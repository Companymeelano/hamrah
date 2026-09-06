package ir.atiran.hamrah.viewer.ui.screens.browse

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ir.atiran.hamrah.viewer.data.AtiranRepository
import ir.atiran.hamrah.viewer.ui.AppViewModel
import ir.atiran.hamrah.viewer.ui.components.EmptyBox
import ir.atiran.hamrah.viewer.ui.components.ErrorBanner
import ir.atiran.hamrah.viewer.ui.components.LoadingBox
import ir.atiran.hamrah.viewer.ui.components.SearchField

/**
 * Shared paged-browse screen:
 *  - stable repository from [vm] (recreated only when server/CPUID change)
 *  - loads consecutive pages as the list approaches the end
 *  - filters loaded rows locally by query
 *  - calls [onItemTap] when a row is tapped (typically a detail dialog)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> BrowseListScreen(
    title: String,
    vm: AppViewModel,
    load: suspend (AtiranRepository, start: Int, fetch: Int) -> List<T>,
    fetchSize: Int = 100,
    localFilter: (T, String) -> Boolean = { _, _ -> true },
    itemKey: (T) -> Any? = { it.hashCode() },
    itemContent: @Composable (T) -> Unit,
    onItemTap: (T) -> Unit = {},
) {
    val settings by vm.settings.collectAsState()
    val repo = remember(settings.serverUrl, settings.cpuId) { vm.repository() }

    var items by remember { mutableStateOf<List<T>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var loadingMore by remember { mutableStateOf(false) }
    var endReached by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var query by remember { mutableStateOf("") }
    var page by remember { mutableIntStateOf(0) }

    val listState = rememberLazyListState()
    val shouldLoadMore by remember {
        derivedStateOf {
            val last = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            last >= items.size - 5
        }
    }

    suspend fun fetchPage(start: Int, append: Boolean) {
        val r = repo ?: throw IllegalStateException("تنظیمات ناقص است")
        val pageItems = load(r, start, fetchSize)
        items = if (append) items + pageItems else pageItems
        if (pageItems.size < fetchSize) endReached = true
    }

    LaunchedEffect(repo) {
        loading = true
        error = null
        page = 0
        endReached = false
        try {
            fetchPage(0, append = false)
        } catch (e: Exception) {
            error = e.message ?: "خطا در دریافت داده‌ها"
        } finally {
            loading = false
        }
    }

    LaunchedEffect(shouldLoadMore, items.size) {
        if (shouldLoadMore && !loading && !loadingMore && !endReached && items.isNotEmpty()) {
            loadingMore = true
            error = null
            try {
                fetchPage((page + 1) * fetchSize, append = true)
                page += 1
            } catch (e: Exception) {
                error = e.message ?: "خطا در بارگذاری بیشتر"
            } finally {
                loadingMore = false
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text(title) },
            actions = {
                Text(
                    "تعداد: ${items.size}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(end = 12.dp),
                )
            },
        )
        SearchField(
            value = query,
            onValueChange = { query = it },
            placeholder = "جستجو...",
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
        )
        ErrorBanner(error)

        when {
            loading -> LoadingBox()
            items.isEmpty() -> EmptyBox()
            else -> {
                val filtered = remember(items, query) {
                    items.filter { localFilter(it, query) }
                }
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                ) {
                    itemsIndexed(filtered, key = { i, item -> itemKey(item) ?: i }) { _, item ->
                        Card(
                            onClick = { onItemTap(item) },
                            modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                itemContent(item)
                            }
                        }
                    }
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(8.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            if (loadingMore) {
                                LoadingBox(modifier = Modifier)
                            } else if (!endReached) {
                                Button(
                                    onClick = { /* auto-load is active */ },
                                    enabled = false,
                                ) { Text("در حال بارگذاری...") }
                            } else {
                                Text(
                                    "پایان لیست",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
