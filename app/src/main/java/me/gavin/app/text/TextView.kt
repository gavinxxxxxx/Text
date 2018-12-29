package me.gavin.app.text

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import me.gavin.app.entity.Book
import me.gavin.app.entity.Page


class TextView(context: Context, attr: AttributeSet?) : View(context, attr) {

    val flipper: Flipper by lazy {
        ScrollFlipper(this)
    }

//    var book: Book? = null
//        set(value) {
//            field = value?.also { book ->
//                book.onPageReady = {
//                    invalidate()
//                    if (it == 0) {
//                        book.next(pageCurr, pageNext)
//                        book.last(pageCurr, pageLast)
//                    }
//                }
//                book.curr(pageCurr)
//            }
//        }

    fun setBook(book: Book) {
        flipper.book = book
        flipper.onBookReady()
    }

    override fun onSizeChanged(w: Int, h: Int, ow: Int, oh: Int) {
        Config.syncSize(w, h)
    }

    // todo -> Chapter<Page>
    var pageCurr = Page()
    var pageNext = Page()
    var pageLast = Page()

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent) = flipper.onTouchEvent(event)

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawColor(Config.bgColor)
        flipper.onDraw(canvas)
    }
}