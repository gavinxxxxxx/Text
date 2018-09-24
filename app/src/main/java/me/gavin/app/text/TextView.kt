package me.gavin.app.text

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.util.AttributeSet
import android.view.View


class TextView(context: Context, attr: AttributeSet) : View(context, attr) {

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawColor(Color.YELLOW)
    }
}