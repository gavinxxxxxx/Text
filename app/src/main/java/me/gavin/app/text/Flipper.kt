package me.gavin.app.text

import android.graphics.Canvas
import android.view.MotionEvent

/**
 * 翻页器
 *
 * @author gavin.xiong 2018/4/22.
 */
abstract class Flipper(val view: TextView) {

    abstract fun onTouchEvent(event: MotionEvent): Boolean

    abstract fun onDraw(canvas: Canvas)

}
