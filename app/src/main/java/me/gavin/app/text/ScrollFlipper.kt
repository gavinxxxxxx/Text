package me.gavin.app.text

import android.graphics.Canvas
import android.view.MotionEvent
import android.view.VelocityTracker
import android.view.View
import android.widget.OverScroller
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
    var vt: VelocityTracker? = null
    var flinger: FlingRunnable? = null

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                vt = VelocityTracker.obtain().also {
                    it.addMovement(event)
                }
                flinger?.cancel()
            }
            MotionEvent.ACTION_MOVE -> {
                view.scrollY += (lastY - event.y).toInt()
//                view.translationY -= (lastY - event.y).toInt()
                vt?.addMovement(event)
            }
            MotionEvent.ACTION_CANCEL -> {
                vt?.recycle()
                vt = null
            }
            MotionEvent.ACTION_UP -> {
                vt?.also { vt ->
                    vt.addMovement(event)
                    vt.computeCurrentVelocity(1000)

                    view.post(FlingRunnable(view)
                            .also { fl ->
                                flinger = fl
                                fl.fling(vt.yVelocity, words.last().y - view.height + Config.bottomPadding)
                            })
                }?.recycle()
                vt = null
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

    fun fling(velocityY: Float, maxY: Float) {
        println("fling - $velocityY")
        mScroller.fling(0, view.scrollY,
                0, -velocityY.toInt(), 0, 0, 0, maxY.toInt(), 0, 0)
    }

    fun cancel() {
        mScroller.forceFinished(true)
    }

    override fun run() {
        if (mScroller.isFinished) return
        if (mScroller.computeScrollOffset()) {
            println(mScroller.currY)
            view.scrollY = mScroller.currY.toFloat().toInt()
            view.postOnAnimation(this)
        }
    }
}
