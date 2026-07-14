package com.reader.guzhenren.data

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NovelRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val novels: MutableList<Novel> = mutableListOf()
    private val chaptersCache: MutableMap<Int, List<Chapter>> = mutableMapOf()

    suspend fun loadNovels(): List<Novel> = withContext(Dispatchers.IO) {
        if (novels.isNotEmpty()) return@withContext novels
        val json = context.assets.open("novels_config.json").bufferedReader().readText()
        val config = JSONObject(json)
        val arr = config.getJSONArray("novels")
        for (i in 0 until arr.length()) {
            val obj = arr.getJSONObject(i)
            novels.add(Novel(
                id = obj.getString("id"),
                title = obj.getString("title"),
                author = obj.getString("author"),
                chaptersFile = obj.getString("chapters_file"),
                coverColor = parseColor(obj.getString("cover_color"))
            ))
        }
        novels
    }

    suspend fun loadChapters(novel: Novel): List<Chapter> = withContext(Dispatchers.IO) {
        val cacheKey = novel.id.hashCode()
        chaptersCache[cacheKey]?.let { return@withContext it }

        val json = context.assets.open(novel.chaptersFile).bufferedReader().readText()
        val root = JSONObject(json)
        val arr = root.getJSONArray("chapters")
        val chapters = mutableListOf<Chapter>()
        for (i in 0 until arr.length()) {
            val obj = arr.getJSONObject(i)
            chapters.add(Chapter(
                title = obj.getString("title"),
                content = obj.getString("content")
            ))
        }
        chaptersCache[cacheKey] = chapters
        chapters
    }

    private fun parseColor(hex: String): Long {
        return hex.removePrefix("#").toLong(16) or 0xFF000000
    }
}
