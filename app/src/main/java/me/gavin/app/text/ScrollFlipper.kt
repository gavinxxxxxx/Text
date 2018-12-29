package me.gavin.app.text

import android.graphics.Canvas
import android.view.MotionEvent
import android.view.View
import android.widget.OverScroller
import android.widget.Scroller
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import me.gavin.app.entity.Word

class ScrollFlipper(view: TextView) : Flipper(view) {

    private var words = listOf<Word>()

    override fun onBookReady() {
        Single.just(book)
                .source()
                .chapters()
                .text()
                .lines()
                .words()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    words = it
                    view.invalidate()
                }, { it.printStackTrace() })
                .also { mCompositeDisposable.add(it) }
    }

    var lastY = 0F

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_MOVE -> {
                view.scrollY += (lastY - event.y).toInt()
            }
            MotionEvent.ACTION_UP -> {
                val runnable = FlingRunnable(view)
                runnable.fling()
                view.post(runnable)
//                view.post(FlingRunnable(view).also {
//                    it.fling(view.height, 150)
//                })
            }
        }
        lastY = event.y
        return true
    }

    override fun onDraw(canvas: Canvas) {
        words.forEach { it.draw(canvas, 0f, 0f) }
    }

}

class FlingRunnable(val view: View) : Runnable {

    private val mScroller = OverScroller(view.context)

    fun fling() {
        mScroller.fling(300, view.scrollY, 100, 100, 0, 2000, 0, 2000)
    }

    override fun run() {
        if (mScroller.isFinished) return
        if (mScroller.computeScrollOffset()) {
            println(mScroller.currY)
            view.scrollY = mScroller.currY
            view.scrollX = mScroller.currX
            view.postOnAnimation(this)
        }
    }
}
