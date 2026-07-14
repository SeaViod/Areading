package com.reader.guzhenren

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.reader.guzhenren.data.Novel
import com.reader.guzhenren.data.NovelRepository
import com.reader.guzhenren.ui.library.LibraryScreen
import com.reader.guzhenren.ui.library.LibraryViewModel
import com.reader.guzhenren.ui.reader.ReaderScreen
import com.reader.guzhenren.ui.reader.ReaderViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val repo = NovelRepository(applicationContext)
        setContent {
            ReaderApp(repo)
        }
    }
}

@Composable
fun ReaderApp(repo: NovelRepository) {
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Library) }
    var selectedNovel by remember { mutableStateOf<Novel?>(null) }

    when (currentScreen) {
        is Screen.Library -> {
            LibraryScreen(
                onSelectNovel = { novel ->
                    selectedNovel = novel
                    currentScreen = Screen.Reader
                },
                viewModelFactory = { LibraryViewModel(repo) }
            )
        }
        is Screen.Reader -> {
            selectedNovel?.let { novel ->
                ReaderScreen(
                    chaptersFile = novel.chaptersFile,
                    onBack = { currentScreen = Screen.Library },
                    viewModelFactory = { ReaderViewModel(repo) }
                )
            }
        }
    }
}

sealed class Screen {
    data object Library : Screen()
    data object Reader : Screen()
}
