package me.gavin.app.text

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.View
import me.gavin.app.entity.Page


class TextView(context: Context, attr: AttributeSet) : View(context, attr) {

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        if (w != oldw || h != oldh) {
            Config.syncSize(w, h)
        }
    }

    var page: Page? = null

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawColor(Config.bgColor)

        page?.run {
            for (word in wordList) {
                word.draw(canvas, 0F, 0F)
            }
        }
    }
}