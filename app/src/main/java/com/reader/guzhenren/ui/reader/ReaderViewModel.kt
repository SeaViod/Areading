package com.reader.guzhenren.ui.reader

import android.text.StaticLayout
import android.text.TextPaint
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.reader.guzhenren.data.Chapter
import com.reader.guzhenren.data.Novel
import com.reader.guzhenren.data.NovelRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ReaderViewModel @Inject constructor(
    private val repository: NovelRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<ReaderUiState>(ReaderUiState.Loading)
    val uiState: StateFlow<ReaderUiState> = _uiState.asStateFlow()

    private var novel: Novel? = null
    private var chapters: List<Chapter> = emptyList()
    private var chapterIdx: Int = 0
    private var pageIdx: Int = 0
    private var pages: List<String> = emptyList()

    private var pageWidth: Int = 720
    private var pageHeight: Int = 1200
    var fontSize: Float = 18f
        private set

    fun loadNovel(chaptersFile: String) {
        viewModelScope.launch {
            _uiState.value = ReaderUiState.Loading
            try {
                novel = repository.loadNovels().find { it.chaptersFile == chaptersFile }
                    ?: throw Exception("未找到小说")
                chapters = repository.loadChapters(novel!!)
                if (chapters.isEmpty()) {
                    _uiState.value = ReaderUiState.Error("没有章节")
                    return@launch
                }
                chapterIdx = 0; pageIdx = 0
                paginateAndUpdate()
            } catch (e: Exception) {
                _uiState.value = ReaderUiState.Error(e.message ?: "加载失败")
            }
        }
    }

    fun updateLayout(w: Int, h: Int) {
        pageWidth = w; pageHeight = h
        paginateAndUpdate()
    }

    fun changeFont(delta: Int) {
        fontSize = (fontSize + delta).coerceIn(12f, 30f)
        paginateAndUpdate()
    }

    fun cycleTheme(): Int {
        val cur = _uiState.value
        if (cur !is ReaderUiState.Content) return 0
        val next = (cur.theme + 1) % 3
        _uiState.value = cur.copy(theme = next)
        return next
    }

    fun nextPage(): Boolean {
        if (pageIdx + 1 < pages.size) { pageIdx++; emitContent(); return true }
        if (chapterIdx + 1 < chapters.size) {
            chapterIdx++; pageIdx = 0; paginateAndUpdate(); return true
        }
        return false
    }

    fun prevPage(): Boolean {
        if (pageIdx > 0) { pageIdx--; emitContent(); return true }
        if (chapterIdx > 0) {
            chapterIdx--
            paginatePages()
            pageIdx = (pages.size - 1).coerceAtLeast(0)
            emitContent()
            return true
        }
        return false
    }

    fun selectChapter(idx: Int) {
        if (idx < 0 || idx >= chapters.size) return
        chapterIdx = idx; pageIdx = 0
        paginateAndUpdate()
    }

    fun getChapterList(): List<String> = chapters.map { it.title }
    fun getCurrentChapterIdx(): Int = chapterIdx

    private fun paginateAndUpdate() {
        paginatePages()
        emitContent()
    }

    private fun paginatePages() {
        val ch = chapters.getOrNull(chapterIdx) ?: return
        pages = paginateText(ch.content, pageWidth, pageHeight, fontSize)
    }

    private fun emitContent() {
        val ch = chapters.getOrNull(chapterIdx)
        val cur = _uiState.value
        val theme = if (cur is ReaderUiState.Content) cur.theme else 0
        _uiState.value = ReaderUiState.Content(
            novelTitle = novel?.title ?: "",
            chapterTitle = ch?.title ?: "",
            pageContent = pages.getOrNull(pageIdx) ?: "",
            chapterIndex = chapterIdx,
            chapterCount = chapters.size,
            pageIndex = pageIdx,
            totalPages = pages.size,
            theme = theme
        )
    }

    companion object {
        fun paginateText(text: String, width: Int, height: Int, fontSize: Float): List<String> {
            if (width <= 0 || height <= 0) return listOf(text)

            val paint = TextPaint().apply {
                this.textSize = fontSize * 2.5f
                isAntiAlias = true
            }

            val paragraphs = text.split("\n").filter { it.isNotBlank() }
            val full = paragraphs.joinToString("\n") { "　　$it" }

            val layout = if (android.os.Build.VERSION.SDK_INT >= 23) {
                StaticLayout.Builder.obtain(full, 0, full.length, paint, width)
                    .setLineSpacing(fontSize * 0.3f, 1f)
                    .build()
            } else {
                StaticLayout(full, paint, width, android.text.Layout.Alignment.ALIGN_NORMAL, 1.5f, 0f, false)
            }

            val result = mutableListOf<String>()
            var line = 0
            while (line < layout.lineCount) {
                val startOff = layout.getLineStart(line)
                val startY = layout.getLineTop(line)
                var end = line + 1
                while (end < layout.lineCount && layout.getLineBottom(end) - startY <= height) end++
                val endOff = if (end < layout.lineCount) layout.getLineStart(end) else full.length
                result.add(full.substring(startOff, endOff).trim())
                line = end
            }
            return result.ifEmpty { listOf(full) }
        }
    }
}

sealed class ReaderUiState {
    object Loading : ReaderUiState()
    data class Error(val message: String) : ReaderUiState()
    data class Content(
        val novelTitle: String,
        val chapterTitle: String,
        val pageContent: String,
        val chapterIndex: Int,
        val chapterCount: Int,
        val pageIndex: Int,
        val totalPages: Int,
        val theme: Int = 0
    ) : ReaderUiState()
}
