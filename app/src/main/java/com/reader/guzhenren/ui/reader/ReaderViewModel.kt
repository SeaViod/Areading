package com.reader.guzhenren.ui.reader

import android.text.StaticLayout
import android.text.TextPaint
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.reader.guzhenren.data.Chapter
import com.reader.guzhenren.data.NovelRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ReaderViewModel(
    private val repository: NovelRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<ReaderUiState>(ReaderUiState.Loading)
    val uiState: StateFlow<ReaderUiState> = _uiState.asStateFlow()

    private var chapters: List<Chapter> = emptyList()
    private var chapterIdx: Int = 0
    private var pageIdx: Int = 0
    private var pages: List<String> = emptyList()
    private var pageW: Int = 720
    private var pageH: Int = 1200
    var fontSize: Float = 18f; private set

    fun loadNovel(chaptersFile: String) {
        viewModelScope.launch {
            _uiState.value = ReaderUiState.Loading
            try {
                val novels = repository.loadNovels()
                val novel = novels.find { it.chaptersFile == chaptersFile } ?: throw Exception("未找到小说")
                chapters = repository.loadChapters(novel)
                if (chapters.isEmpty()) { _uiState.value = ReaderUiState.Error("没有章节"); return@launch }
                chapterIdx = 0; pageIdx = 0; paginate()
            } catch (e: Exception) {
                _uiState.value = ReaderUiState.Error(e.message ?: "加载失败")
            }
        }
    }

    fun updateLayout(w: Int, h: Int) { pageW = w; pageH = h; paginate() }
    fun changeFont(d: Int) { fontSize = (fontSize + d).coerceIn(12f, 30f); paginate() }

    fun cycleTheme(): Int {
        val cur = _uiState.value
        if (cur !is ReaderUiState.Content) return 0
        val next = (cur.theme + 1) % 3
        _uiState.value = cur.copy(theme = next)
        return next
    }

    fun nextPage(): Boolean {
        if (pageIdx + 1 < pages.size) { pageIdx++; emit(); return true }
        if (chapterIdx + 1 < chapters.size) { chapterIdx++; pageIdx = 0; paginate(); return true }
        return false
    }
    fun prevPage(): Boolean {
        if (pageIdx > 0) { pageIdx--; emit(); return true }
        if (chapterIdx > 0) { chapterIdx--; paginate(); pageIdx = (pages.size - 1).coerceAtLeast(0); emit(); return true }
        return false
    }

    fun selectChapter(idx: Int) { if (idx in chapters.indices) { chapterIdx = idx; pageIdx = 0; paginate() } }
    fun getChapterList(): List<String> = chapters.map { it.title }
    fun getCurrentIdx(): Int = chapterIdx

    private fun paginate() {
        val ch = chapters.getOrNull(chapterIdx) ?: return
        pages = paginateText(ch.content, pageW, pageH, fontSize)
        emit()
    }

    private fun emit() {
        val ch = chapters.getOrNull(chapterIdx); val cur = _uiState.value
        _uiState.value = ReaderUiState.Content(
            chapterTitle = ch?.title ?: "",
            pageContent = pages.getOrNull(pageIdx) ?: "",
            chapterIndex = chapterIdx, chapterCount = chapters.size,
            pageIndex = pageIdx, totalPages = pages.size,
            theme = (cur as? ReaderUiState.Content)?.theme ?: 0
        )
    }

    companion object {
        fun paginateText(text: String, w: Int, h: Int, fs: Float): List<String> {
            if (w <= 0 || h <= 0) return listOf(text)
            val paint = TextPaint().apply { textSize = fs * 2.5f; isAntiAlias = true }
            val paras = text.split("\n").filter { it.isNotBlank() }
            val full = paras.joinToString("\n") { "\u3000\u3000$it" }
            val layout = StaticLayout.Builder.obtain(full, 0, full.length, paint, w)
                .setLineSpacing(fs * 0.3f, 1f).build()
            val result = mutableListOf<String>()
            var line = 0
            while (line < layout.lineCount) {
                val startY = layout.getLineTop(line); var end = line + 1
                while (end < layout.lineCount && layout.getLineBottom(end) - startY <= h) end++
                val endOff = if (end < layout.lineCount) layout.getLineStart(end) else full.length
                result.add(full.substring(layout.getLineStart(line), endOff).trim())
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
        val chapterTitle: String, val pageContent: String,
        val chapterIndex: Int, val chapterCount: Int,
        val pageIndex: Int, val totalPages: Int, val theme: Int = 0
    ) : ReaderUiState()
}
