package me.gavin.app.text

import android.graphics.Canvas
import android.view.MotionEvent
import me.gavin.app.entity.Page

/**
 * 翻页器
 *
 * @author gavin.xiong 2018/4/22.
 */
abstract class Flipper(val view: TextView) {

    abstract fun onTouchEvent(event: MotionEvent): Boolean

    abstract fun onDraw(canvas: Canvas)

}

class SimpleFlipper(view: TextView) : Flipper(view) {

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_UP) {
            if (event.x > view.width * 0.5f && !view.pageCurr.isLast) {
                view.pageLast = view.pageCurr
                view.pageCurr = view.pageNext
                view.pageNext = Page().also {
                    view.book?.next(view.pageCurr, it)
                }
                view.invalidate()
            } else if (event.x < view.width * 0.5f && !view.pageCurr.isFirst) {
                view.pageNext = view.pageCurr
                view.pageCurr = view.pageLast
                view.pageLast = Page().also {
                    view.book?.last(view.pageCurr, it)
                }
                view.invalidate()
            }
        }
        return true
    }

    override fun onDraw(canvas: Canvas) {
        view.pageCurr.run {
            if (ready) {
                wordList.forEach {
                    it.draw(canvas, 0F, 0F)
                }
            }
        }
    }

}


//class CoverFlipper(view: TextView) : Flipper(view) {
//
//    var lastX = 0F
//
//    override fun onTouchEvent(event: MotionEvent): Boolean {
//        when (event.action) {
//            MotionEvent.ACTION_MOVE -> {
//                view.scrollX += (lastX - event.x).toInt()
//            }
//        }
//        lastX = event.x
//        return true
//    }
//
//    override fun onDraw(canvas: Canvas) {
//        view.page?.run {
//            for (word in wordList) {
//                word.draw(canvas, 0F, 0F)
//            }
//        }
//    }
//}
