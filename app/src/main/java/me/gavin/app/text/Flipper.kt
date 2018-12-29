package me.gavin.app.text

import android.graphics.Canvas
import android.view.MotionEvent
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import me.gavin.app.AppDatabase
import me.gavin.app.entity.*
import me.gavin.base.App
import me.gavin.base.Provider.api
import org.jsoup.Jsoup
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.abs
import kotlin.math.max

/**
 * 翻页器
 *
 * @author gavin.xiong 2018/4/22.
 */
abstract class Flipper(val view: TextView) {

    lateinit var book: Book

    val mCompositeDisposable = CompositeDisposable()

    abstract fun onBookReady()

    abstract fun onTouchEvent(event: MotionEvent): Boolean

    abstract fun onDraw(canvas: Canvas)

    protected fun Single<Book>.source(): Single<Book> {
        return this.flatMap { book ->
            return@flatMap if (book.isSourceInit) Single.just(book)
            else AppDatabase.getInstance(App.app)
                    .sourceDao()
                    .load(book.src)
                    .map { source -> book.also { it.source = source } }
        }
    }

    protected fun Single<Book>.chapters(): Single<Book> {
        return this.flatMap { book ->
            if (!book.isSourceInit) throw NullPointerException("source is null")
            return@flatMap if (book.isChaptersInit) Single.just(book)
            else book.source.chapters(book).map { chapters -> book.also { it.chapters = chapters } }
        }
    }

    protected fun Single<Book>.text(): Single<String> {
        return this.flatMap { book ->
            if (!book.isSourceInit || !book.isChaptersInit) throw NullPointerException("source is null")
            val index = max(0, minOf(book.chapters.size, book.chapterIndex))
            return@flatMap book.chapters[index].run {
                book.source.text(this).map { s -> s.also { text = it } }
            }
        }
    }

    protected fun Single<String>.lines(): Single<List<Line>> {
        return this.map { chapter ->
            var y = Config.topPadding // 行文字顶部
            val lineList = ArrayList<Line>()
            Config.REGEX_SEGMENT.toRegex().split(chapter)
                    .filterNot { it.isBlank() }
                    .forEach { segment ->
                        var segmentStart = 0
                        while (segmentStart < segment.length) { // 当前段落还有字符
                            val lineIndent = segmentStart == 0 // 行缩进
                            var lineCount = Utils.breakText(segment.substring(segmentStart), lineIndent)
                            val lineSuffix = if (lineCount < 0) "-" else ""
                            lineCount = abs(lineCount)
                            val lineText = segment.substring(segmentStart, segmentStart + lineCount)
                            val lineAlign = segmentStart + lineCount < segment.length // 分散对齐 - 不是段落的尾行
                            val line = Line(lineText.trim { it <= ' ' || it == '　' }, lineSuffix, y - Config.textTop, lineIndent, lineAlign)
                            lineList.add(line)

                            y += Config.textHeight + Config.lineSpacing
                            segmentStart += lineCount
                        }
                        y += Config.segmentSpacing // todo 章末需要回退否？
                    } // 退出字符循环 - 字符已全部装载完
            return@map lineList
        }
    }

    protected fun Single<List<Line>>.words(): Single<List<Word>> {
        return this.map { Utils.line2Word(it) }
    }

    fun Source.chapters(book: Book): Single<List<Chapter>> {
        return Single.just(book.chapterUrl)
                .flatMap { api.get(it, "max-stale=31536000") }
                .map { Jsoup.parse(it) }
                .map { it.list(this.ruleChapterList) }
                .map {
                    it.mapIndexedTo(LinkedList()) { index, element ->
                        val url = element.single(this.ruleChapterContentUrl, book.chapterUrl)
                        val name = element.single(this.ruleChapterName, book.chapterUrl)
                        return@mapIndexedTo Chapter(url, name, book.url, index)
                    }
                }
    }

    fun Source.text(chapter: Chapter): Single<String> {
        return Single.just(chapter.url)
                .flatMap { api.get(it, "max-stale=31536000") }
                .map { Jsoup.parse(it) }
                .map { it.single(this.ruleContent, chapter.url) }
    }

}

//class SimpleFlipper(view: TextView) : Flipper(view) {
//
//    override fun onTouchEvent(event: MotionEvent): Boolean {
//        if (event.action == MotionEvent.ACTION_UP) {
//            if (event.x > view.width * 0.5f && !view.pageCurr.isLast) {
//                view.pageLast = view.pageCurr
//                view.pageCurr = view.pageNext
//                view.pageNext = Page().also {
//                    view.book?.next(view.pageCurr, it)
//                }
//                view.invalidate()
//            } else if (event.x < view.width * 0.5f && !view.pageCurr.isFirst) {
//                view.pageNext = view.pageCurr
//                view.pageCurr = view.pageLast
//                view.pageLast = Page().also {
//                    view.book?.last(view.pageCurr, it)
//                }
//                view.invalidate()
//            }
//        }
//        return true
//    }
//
//    override fun onDraw(canvas: Canvas) {
//        view.pageCurr.run {
//            if (ready) {
//                wordList.forEach {
//                    it.draw(canvas, 0F, 0F)
//                }
//            }
//        }
//    }
//
//}


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
