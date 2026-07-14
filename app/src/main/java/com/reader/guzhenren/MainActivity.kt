package com.reader.guzhenren

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.reader.guzhenren.data.Novel
import com.reader.guzhenren.ui.library.LibraryScreen
import com.reader.guzhenren.ui.reader.ReaderScreen
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ReaderNavHost()
        }
    }
}

@Composable
fun ReaderNavHost() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "library") {
        composable("library") {
            LibraryScreen(
                onSelectNovel = { novel ->
                    navController.navigate("reader/${novel.chaptersFile}")
                }
            )
        }
        composable(
            route = "reader/{chaptersFile}",
            arguments = listOf(navArgument("chaptersFile") { type = NavType.StringType })
        ) { backStackEntry ->
            val file = backStackEntry.arguments?.getString("chaptersFile") ?: ""
            ReaderScreen(
                chaptersFile = file,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
