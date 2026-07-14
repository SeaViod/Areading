package com.reader.guzhenren.ui.library

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import androidx.hilt.navigation.compose.hiltViewModel
import com.reader.guzhenren.data.Novel
import com.reader.guzhenren.ui.reader.BgLight
import com.reader.guzhenren.ui.reader.Accent

@Composable
fun LibraryScreen(
    onSelectNovel: (Novel) -> Unit,
    viewModel: LibraryViewModel = hiltViewModel()
) {
    val novels by viewModel.novels.collectAsState()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = BgLight
    ) {
        Column {
            Text(
                "📖 我的书架",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Accent,
                modifier = Modifier.padding(20.dp).statusBarsPadding()
            )

            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(novels) { novel ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectNovel(novel) },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Text(
                                text = novel.title,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF2C2C2C)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "作者：${novel.author}",
                                fontSize = 13.sp,
                                color = Color(0xFF999999)
                            )
                        }
                    }
                }
            }
        }
    }
}
