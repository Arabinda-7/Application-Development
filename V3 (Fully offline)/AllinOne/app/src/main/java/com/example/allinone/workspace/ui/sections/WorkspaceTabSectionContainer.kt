package com.example.allinone.workspace.ui.sections

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.allinone.LocalAppStyle
import kotlinx.coroutines.launch

/**
 * Reusable container composable for workspace tabbed views (Features, Bugs, Tasks, Goals, Ideas).
 * Eliminates duplicate ScrollableTabRow and HorizontalPager boilerplate across section files.
 */
@Composable
fun WorkspaceTabSectionContainer(
    tabs: List<String>,
    counts: List<Int> = emptyList(),
    header: (@Composable () -> Unit)? = null,
    content: @Composable (pageIndex: Int, status: String) -> Unit
) {
    val style = LocalAppStyle.current
    val pagerState = rememberPagerState(pageCount = { tabs.size })
    val scope = rememberCoroutineScope()

    Column(modifier = Modifier.fillMaxSize()) {
        header?.invoke()

        ScrollableTabRow(
            selectedTabIndex = pagerState.currentPage,
            containerColor = Color.Transparent,
            contentColor = style.accentColor,
            edgePadding = 0.dp
        ) {
            tabs.forEachIndexed { index, title ->
                val isSelected = pagerState.currentPage == index
                val countText = if (index < counts.size && counts[index] > 0) " (${counts[index]})" else ""
                Tab(
                    selected = isSelected,
                    onClick = { scope.launch { pagerState.animateScrollToPage(index) } },
                    text = {
                        Text(
                            text = "$title$countText",
                            fontSize = 13.sp,
                            color = if (isSelected) style.accentColor else Color.White.copy(alpha = 0.5f)
                        )
                    }
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxWidth().weight(1f)
        ) { page ->
            content(page, tabs[page])
        }
    }
}
