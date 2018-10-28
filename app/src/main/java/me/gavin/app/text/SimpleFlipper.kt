package me.gavin.app.text

import android.graphics.Canvas
import android.view.MotionEvent


class SimpleFlipper(view: TextView) : Flipper(view) {

    var lastX = 0F

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_MOVE -> {
                view.scrollX += (lastX - event.x).toInt()
            }
        }
        lastX = event.x
        return true
    }

    override fun onDraw(canvas: Canvas) {
        view.page?.run {
            for (word in wordList) {
                word.draw(canvas, 0F, 0F)
            }
        }
    }
}