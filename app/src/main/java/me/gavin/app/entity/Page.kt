package me.gavin.app.entity

import android.graphics.Canvas


class Page {

    var index: Int = 0
    var start: Long = 0
    var end: Long = 0

    var first: Boolean = false
    var last: Boolean = false

    var reverse = false

    var indent = false
    var align = false
    var suffix = false

    var text: String = ""
}


class Line(val src: String, val suffix: String, val y: Int, val lineIndent: Boolean, val lineAlign: Boolean) {

    val text
        get() = src + suffix

}

class Word(val text: String, val x: Float, val y: Float) {

    fun draw(canvas: Canvas, offsetX: Float, offsetY: Float) {
        // canvas.drawText(text, x + offsetX, y + offsetY, Config.textPaint)
    }
}