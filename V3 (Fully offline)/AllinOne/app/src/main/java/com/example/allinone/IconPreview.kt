package com.example.allinone

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Preview(showBackground = true)
@Composable
fun ArrowIconsPreview() {
    val icons = listOf(
        "icons8_arrow_100" to R.drawable.icons8_arrow_100,
        "icons8_arrow_100_2" to R.drawable.icons8_arrow_100_2,
        "icons8_arrow_100_3" to R.drawable.icons8_arrow_100_3,
        "icons8_arrow_100_4" to R.drawable.icons8_arrow_100_4,
        "icons8_arrow_100_5" to R.drawable.icons8_arrow_100_5,
        "icons8_arrow_100_6" to R.drawable.icons8_arrow_100_6,
        "icons8_arrow_100_7" to R.drawable.icons8_arrow_100_7,
        "icons8_arrow_100_8" to R.drawable.icons8_arrow_100_8,
        "icons8_arrow_100_9" to R.drawable.icons8_arrow_100_9,
        "icons8_arrow_100_10" to R.drawable.icons8_arrow_100_10,
        "icons8_arrow_100_11" to R.drawable.icons8_arrow_100_11,
        "icons8_arrow_100_12_apng" to R.drawable.icons8_arrow_100_12_apng
    )

    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        modifier = Modifier.fillMaxSize().padding(16.dp)
    ) {
        items(icons) { pair ->
            val name = pair.first
            val resId = pair.second
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(8.dp)
            ) {
                Image(
                    painter = painterResource(id = resId),
                    contentDescription = name,
                    modifier = Modifier.size(48.dp)
                )
                Text(text = name)
            }
        }
    }
}
