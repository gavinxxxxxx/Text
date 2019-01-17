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
import kotlin.math.max
import kotlin.math.min

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
                flinger?.cancel()
                vt = VelocityTracker.obtain().also {
                    it.addMovement(event)
                }
            }
            MotionEvent.ACTION_MOVE -> {
                vt?.addMovement(event)
                view.scrollY = max(0, min(words.last().y.toInt() - view.height + Config.textBottom + Config.bottomPadding,
                        view.scrollY + (lastY - event.y).toInt()))
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
                            .also {
                                flinger = it
                                it.fling(vt.yVelocity, words.last().y - view.height + Config.textBottom + Config.bottomPadding)
                            })
                }?.recycle()
                vt = null
            }
        }
        lastY = event.y
        return true
    }

    override fun onDraw(canvas: Canvas) {
        for (it in words) {
            if (it.y < view.scrollY) continue
            it.draw(canvas, 0f, 0f)
            if (it.y > view.scrollY + view.height + Config.textHeight) break
        }
    }

}

class FlingRunnable(val view: View) : Runnable {

    private val mScroller = OverScroller(view.context)

    fun fling(velocityY: Float, maxY: Float) {
        mScroller.fling(0, view.scrollY, 0, -velocityY.toInt(),
                0, 0, 0, maxY.toInt())
    }

    fun cancel() {
        mScroller.forceFinished(true)
    }

    override fun run() {
        if (mScroller.isFinished) return
        if (mScroller.computeScrollOffset()) {
            view.scrollY = mScroller.currY.toFloat().toInt()
            view.postOnAnimation(this)
        }
    }

}
