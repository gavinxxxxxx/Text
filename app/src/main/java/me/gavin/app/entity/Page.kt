package me.gavin.app.entity

import android.graphics.Canvas
import me.gavin.app.text.Config


class Page(val chapter: String) {

    var index: Int = 0
    var start: Long = 0
    var end: Long = 0

    var isFirst: Boolean = false
    var isLast: Boolean = false

    var isReverse = false

    var indent = false
    var align = false
    var suffix = false

    var text: String = ""

    val lineList = ArrayList<Line>() // 页面文字分行
    val wordList = ArrayList<Word>() // 页面按字词拆分
}

class Line(val src: String, val suffix: String, var y: Int, val lineIndent: Boolean, val lineAlign: Boolean) {

    val text
        get() = src + suffix

    override fun toString(): String {
        return "Line(src='$src', suffix='$suffix', y=$y, lineIndent=$lineIndent, lineAlign=$lineAlign)"
    }

}

class Word(val text: String, val x: Float, val y: Float) {
    fun draw(canvas: Canvas, offsetX: Float, offsetY: Float) {
        canvas.drawText(text, x + offsetX, y + offsetY, Config.textPaint)
    }
}