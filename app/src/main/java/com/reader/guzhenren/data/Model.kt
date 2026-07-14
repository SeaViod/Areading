package com.reader.guzhenren.data

data class Novel(
    val id: String,
    val title: String,
    val author: String,
    val chaptersFile: String,
    val coverColor: Long
)

data class Chapter(
    val title: String,
    val content: String
)
