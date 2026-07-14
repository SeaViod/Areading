package com.reader.guzhenren.ui.reader

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel

val BgLight = Color(0xFFF5F1E8)
val BgNight = Color(0xFF1A1A1A)
val BgSepia = Color(0xFFEFE0C0)
val TxLight = Color(0xFF2C2C2C)
val TxNight = Color(0xFFC8C3B8)
val TxSepia = Color(0xFF4A3520)
val Accent = Color(0xFF8B5A2B)

@Composable
fun ReaderScreen(
    chaptersFile: String, onBack: () -> Unit,
    viewModelFactory: () -> ReaderViewModel
) {
    val viewModel: ReaderViewModel = viewModel(factory = ViewModelProvider.Factory { viewModelFactory() })
    val state by viewModel.uiState.collectAsState()
    var showControls by remember { mutableStateOf(true) }
    var showList by remember { mutableStateOf(false) }

    LaunchedEffect(chaptersFile) { viewModel.loadNovel(chaptersFile) }

    BoxWithConstraints(Modifier.fillMaxSize()) {
        val wp = with(LocalDensity.current) { maxWidth.toPx().toInt() }
        val hp = with(LocalDensity.current) { maxHeight.toPx().toInt() }
        LaunchedEffect(wp, hp) { viewModel.updateLayout(wp, hp) }
    }

    val isContent = state is ReaderUiState.Content
    val th = if (isContent) (state as ReaderUiState.Content).theme else 0
    val bg = when (th) { 1 -> BgNight; 2 -> BgSepia; else -> BgLight }
    val tx = when (th) { 1 -> TxNight; 2 -> TxSepia; else -> TxLight }

    Box(Modifier.fillMaxSize().background(bg)
        .pointerInput(Unit) { detectHorizontalDragGestures { _, a -> if (a < -50) viewModel.nextPage() else if (a > 50) viewModel.prevPage() } }
        .pointerInput(Unit) { detectTapGestures(onTap = { o -> if (o.x < size.width / 3f) viewModel.prevPage() else viewModel.nextPage() }) }
    ) {
        when (state) {
            is ReaderUiState.Loading -> CircularProgressIndicator(Modifier.align(Alignment.Center), color = Accent)
            is ReaderUiState.Error -> Column(Modifier.align(Alignment.Center).padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text((state as ReaderUiState.Error).message, color = MaterialTheme.colorScheme.error)
                Button(onClick = { viewModel.loadNovel(chaptersFile) }) { Text("重试") }
            }
            is ReaderUiState.Content -> {
                val p = state as ReaderUiState.Content
                AnimatedContent(Modifier.fillMaxSize().padding(20.dp, 36.dp, 20.dp, 48.dp),
                    targetState = "${p.chapterIndex}_${p.pageIndex}",
                    transitionSpec = { slideInHorizontally { it / 4 } + fadeIn() togetherWith slideOutHorizontally { -it / 4 } + fadeOut() }) {
                    Text(p.pageContent, style = TextStyle(fontSize = viewModel.fontSize.sp, lineHeight = (viewModel.fontSize * 2.0).sp, color = tx, fontFamily = FontFamily.Serif))
                }
                Surface(Modifier.align(Alignment.BottomCenter).padding(bottom = 10.dp), shape = RoundedCornerShape(10.dp), color = bg.copy(alpha = .85f)) {
                    Text("${p.pageIndex + 1} / ${p.totalPages}", fontSize = 11.sp, color = tx.copy(alpha = .5f), modifier = Modifier.padding(horizontal = 12.dp, vertical = 3.dp))
                }
                AnimatedVisibility(showControls, Modifier.align(Alignment.TopCenter), enter = slideInVertically(), exit = slideOutVertically()) {
                    Surface(color = bg.copy(alpha = .92f), shadowElevation = 2.dp) {
                        Row(Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = onBack) { Text("←", fontSize = 20.sp, color = tx) }
                            TextButton(onClick = { showList = true }) { Text("☰", fontSize = 18.sp, color = tx) }
                            Text(p.chapterTitle, fontSize = 14.sp, color = tx, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f), maxLines = 1)
                            TextButton(onClick = { viewModel.cycleTheme() }) { Text(when(p.theme){1->"☀️";2->"📜";else->"🌙"}, fontSize = 16.sp) }
                            TextButton(onClick = { viewModel.changeFont(-1) }) { Text("A−", fontSize = 14.sp, color = tx) }
                            TextButton(onClick = { viewModel.changeFont(1) }) { Text("A+", fontSize = 14.sp, color = tx) }
                        }
                    }
                }
            }
        }

        if (showList && isContent) {
            Box(Modifier.fillMaxSize()) {
                Surface(Modifier.fillMaxSize().clickable { showList = false }, color = Color.Black.copy(alpha = .4f)) {}
                Surface(Modifier.fillMaxHeight().fillMaxWidth(.7f).align(Alignment.CenterStart), color = bg) {
                    Column {
                        Text("📖 目录", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Accent, modifier = Modifier.padding(16.dp))
                        HorizontalDivider()
                        LazyColumn(Modifier.weight(1f)) {
                            itemsIndexed(viewModel.getChapterList()) { idx, title ->
                                Surface(onClick = { viewModel.selectChapter(idx); showList = false }, color = if (idx == viewModel.getCurrentIdx()) Accent.copy(alpha = .15f) else Color.Transparent) {
                                    Text(title, fontSize = 14.sp, color = if (idx == viewModel.getCurrentIdx()) Accent else tx, fontWeight = if (idx == viewModel.getCurrentIdx()) FontWeight.Bold else FontWeight.Normal, modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp), maxLines = 1)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
