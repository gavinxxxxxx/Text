package me.gavin.app.entity


data class Chapter(
        val url: String,
        val title: String,
        val bookUrl: String,
        val index: Int,
        val offset: Long = 0,
        val selected: Boolean = false,
        var text: String = "")