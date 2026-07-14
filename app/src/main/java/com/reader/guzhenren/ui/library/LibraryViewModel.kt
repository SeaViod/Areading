package com.reader.guzhenren.ui.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.reader.guzhenren.data.Novel
import com.reader.guzhenren.data.NovelRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val repository: NovelRepository
) : ViewModel() {

    private val _novels = MutableStateFlow<List<Novel>>(emptyList())
    val novels: StateFlow<List<Novel>> = _novels.asStateFlow()

    init {
        viewModelScope.launch {
            _novels.value = repository.loadNovels()
        }
    }
}
