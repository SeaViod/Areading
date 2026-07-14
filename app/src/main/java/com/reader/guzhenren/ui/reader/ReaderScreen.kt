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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import androidx.hilt.navigation.compose.hiltViewModel

val BgLight = Color(0xFFF5F1E8)
val BgNight = Color(0xFF1A1A1A)
val BgSepia = Color(0xFFEFE0C0)
val TxLight = Color(0xFF2C2C2C)
val TxNight = Color(0xFFC8C3B8)
val TxSepia = Color(0xFF4A3520)
val Accent = Color(0xFF8B5A2B)

@Composable
fun ReaderScreen(
    chaptersFile: String,
    onBack: () -> Unit,
    viewModel: ReaderViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    var showControls by remember { mutableStateOf(true) }
    var showChapterList by remember { mutableStateOf(false) }
    var lastTapTime by remember { mutableStateOf(0L) }

    LaunchedEffect(chaptersFile) { viewModel.loadNovel(chaptersFile) }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val wPx = with(LocalDensity.current) { maxWidth.toPx().toInt() }
        val hPx = with(LocalDensity.current) { maxHeight.toPx().toInt() }
        LaunchedEffect(wPx, hPx) { viewModel.updateLayout(wPx, hPx) }
    }

    val content = state
    val isContent = content is ReaderUiState.Content
    val themeIdx = if (isContent) (content as ReaderUiState.Content).theme else 0

    val bg = when (themeIdx) { 1 -> BgNight; 2 -> BgSepia; else -> BgLight }
    val tx = when (themeIdx) { 1 -> TxNight; 2 -> TxSepia; else -> TxLight }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bg)
            .pointerInput(Unit) {
                detectHorizontalDragGestures { _, amount ->
                    if (amount < -50) viewModel.nextPage()
                    else if (amount > 50) viewModel.prevPage()
                }
            }
            .pointerInput(Unit) {
                detectTapGestures(
                    onDoubleTap = {
                        showControls = !showControls
                    },
                    onTap = { offset ->
                        val now = System.currentTimeMillis()
                        val isDoubleTap = now - lastTapTime < 300
                        lastTapTime = now
                        if (isDoubleTap) return@detectTapGestures
                        val midX = size.width / 3f
                        if (offset.x < midX) viewModel.prevPage()
                        else viewModel.nextPage()
                    }
                )
            }
    ) {
        when (content) {
            is ReaderUiState.Loading -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = Accent
                )
            }
            is ReaderUiState.Error -> {
                Column(
                    modifier = Modifier.align(Alignment.Center).padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(content.message, color = MaterialTheme.colorScheme.error)
                    Button(onClick = { viewModel.loadNovel(chaptersFile) }) { Text("重试") }
                }
            }
            is ReaderUiState.Content -> {
                val page = content

                // Page text
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(
                            start = 20.dp, end = 20.dp,
                            top = if (showControls) 56.dp else 32.dp,
                            bottom = 40.dp
                        )
                ) {
                    AnimatedContent(
                        targetState = "${page.chapterIndex}_${page.pageIndex}",
                        transitionSpec = {
                            slideInHorizontally { it / 4 } + fadeIn() togetherWith
                                slideOutHorizontally { -it / 4 } + fadeOut()
                        },
                        label = "page"
                    ) {
                        Text(
                            text = page.pageContent,
                            style = TextStyle(
                                fontSize = viewModel.fontSize.sp,
                                lineHeight = (viewModel.fontSize * 2.0).sp,
                                color = tx,
                                fontFamily = FontFamily.Serif
                            )
                        )
                    }
                }

                // Page indicator
                Surface(
                    modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 12.dp),
                    shape = RoundedCornerShape(10.dp),
                    color = bg.copy(alpha = 0.85f)
                ) {
                    Text(
                        text = "${page.pageIndex + 1} / ${page.totalPages}",
                        fontSize = 11.sp,
                        color = tx.copy(alpha = 0.5f),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 3.dp)
                    )
                }

                // Top controls
                AnimatedVisibility(
                    visible = showControls,
                    enter = slideInVertically(),
                    exit = slideOutVertically(),
                    modifier = Modifier.align(Alignment.TopCenter)
                ) {
                    Surface(color = bg.copy(alpha = 0.92f), shadowElevation = 2.dp) {
                        Row(
                            modifier = Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(onClick = onBack) {
                                Text("←", fontSize = 20.sp, color = tx)
                            }
                            TextButton(onClick = { showChapterList = true }) {
                                Text("☰", fontSize = 18.sp, color = tx)
                            }
                            Text(
                                text = page.chapterTitle,
                                fontSize = 14.sp,
                                color = tx,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.weight(1f),
                                maxLines = 1
                            )
                            TextButton(onClick = { viewModel.cycleTheme() }) {
                                Text(
                                    when (page.theme) { 1 -> "☀️"; 2 -> "📜"; else -> "🌙" },
                                    fontSize = 16.sp
                                )
                            }
                            TextButton(onClick = { viewModel.changeFont(-1) }) {
                                Text("A−", fontSize = 14.sp, color = tx)
                            }
                            TextButton(onClick = { viewModel.changeFont(1) }) {
                                Text("A+", fontSize = 14.sp, color = tx)
                            }
                        }
                    }
                }
            }
        }

        // Chapter list drawer
        if (showChapterList && isContent) {
            val chapters = viewModel.getChapterList()
            val curIdx = viewModel.getCurrentChapterIdx()

            Box(modifier = Modifier.fillMaxSize()) {
                // Backdrop
                Surface(
                    modifier = Modifier.fillMaxSize().clickable { showChapterList = false },
                    color = Color.Black.copy(alpha = 0.4f)
                ) {}

                // Drawer
                Surface(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(0.7f)
                        .align(Alignment.CenterStart),
                    color = bg
                ) {
                    Column {
                        Text(
                            "📖 ${(content as ReaderUiState.Content).novelTitle}",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Accent,
                            modifier = Modifier.padding(16.dp)
                        )
                        HorizontalDivider()
                        LazyColumn(modifier = Modifier.weight(1f)) {
                            itemsIndexed(chapters) { idx, title ->
                                Surface(
                                    onClick = {
                                        viewModel.selectChapter(idx)
                                        showChapterList = false
                                    },
                                    color = if (idx == curIdx) Accent.copy(alpha = 0.15f) else Color.Transparent
                                ) {
                                    Text(
                                        text = title,
                                        fontSize = 14.sp,
                                        color = if (idx == curIdx) Accent else tx,
                                        fontWeight = if (idx == curIdx) FontWeight.Bold else FontWeight.Normal,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 16.dp, vertical = 10.dp),
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
