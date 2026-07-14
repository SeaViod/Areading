package com.reader.guzhenren.data

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

class NovelRepository(private val context: Context) {
    private val cache = mutableMapOf<Int, List<Chapter>>()

    suspend fun loadNovels(): List<Novel> = withContext(Dispatchers.IO) {
        val json = context.assets.open("novels_config.json").bufferedReader().readText()
        val arr = JSONObject(json).getJSONArray("novels")
        (0 until arr.length()).map { i ->
            val o = arr.getJSONObject(i)
            Novel(o.getString("id"), o.getString("title"), o.getString("author"),
                o.getString("chapters_file"), o.getString("cover_color").removePrefix("#").toLong(16) or 0xFF000000)
        }
    }

    suspend fun loadChapters(novel: Novel): List<Chapter> = withContext(Dispatchers.IO) {
        cache[novel.id.hashCode()]?.let { return@withContext it }
        val json = context.assets.open(novel.chaptersFile).bufferedReader().readText()
        val arr = JSONObject(json).getJSONArray("chapters")
        val chs = (0 until arr.length()).map { i ->
            val o = arr.getJSONObject(i)
            Chapter(o.getString("title"), o.getString("content"))
        }
        cache[novel.id.hashCode()] = chs; chs
    }
}
