package me.gavin.app.text

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import me.gavin.app.entity.Page


class TextView(context: Context, attr: AttributeSet) : View(context, attr) {

    val flipper: Flipper by lazy {
        SimpleFlipper(this)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        if (w != oldw || h != oldh) {
            Config.syncSize(w, h)
        }
    }

    var page: Page? = null

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent) = flipper.onTouchEvent(event)

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawColor(Config.bgColor)
        flipper.onDraw(canvas)
    }
}