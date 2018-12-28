package me.gavin.app.entity

import android.arch.persistence.room.*
import android.graphics.Canvas
import android.os.Parcelable
import com.chainfor.finance.base.addTo
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.android.parcel.Parcelize
import me.gavin.app.AppDatabase
import me.gavin.app.text.Config
import me.gavin.base.App
import me.gavin.base.Provider.api
import org.jsoup.Jsoup
import java.util.*
import java.util.regex.Pattern
import kotlin.math.max
import kotlin.math.min


@Parcelize
@Entity(tableName = "book")
data class Book(
        @PrimaryKey(autoGenerate = false)
        var url: String,
        var name: String,
        var author: String,
        var cover: String,
        var category: String,
        var intro: String,
        var srcs: String,
        var srcNames: String,
        var state: String = "",
        var updateTime: String = "",
        var updateChapter: String = "",
        var chapterUrl: String = "",
        var src: String = "",
        var srcName: String = "",
        var chapterCount: Int = 0,
        var chapterIndex: Int = 0,
        var chapterOffset: Long = 0L,
        var lastReadTime: Long = 0L,
        var readTime: Long = 0L) : Parcelable {

    @Ignore
    @IgnoredOnParcel
    lateinit var source: Source

    @Ignore
    @IgnoredOnParcel
    lateinit var chapters: List<Chapter>

    val isSourceInit
        get() = this::source.isInitialized

    val isChaptersInit
        get() = this::chapters.isInitialized

    val authorExt
        get() = "作者：$author"

    val categoryExt
        get() = "类型：$category"

    val stateExt
        get() = "状态：$state"

    val updateTimeExt
        get() = "更新：$updateTime"

    val updateChapterExt
        get() = "章节：$updateChapter"

    val srcNameExt
        get() = "书源：$srcName"

    fun curr(page: Page) {
        Single.just(this).source().chapters().text()
                .map {
                    page.apply {
                        chapter = it

                        index = this@Book.chapterIndex

                        isReverse = false
                        start = max(0, min(it.length.toLong(), this@Book.chapterOffset))
                        isFirst = index == 0 && start == 0L
                        text = it.substring(start.toInt(), min(it.length, start.toInt() + Config.pagePreCount))
                        indent = start == 0L
                                || Config.REGEX_SEGMENT_SUFFIX.toRegex().matches(text)
                                || Config.REGEX_SEGMENT_PREFIX.toRegex().matches(it.substring(0, start.toInt()))

                        text2line(this@Book)
                        line2Word()
                    }
                }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    println("~~~~ $it ~~~~")
                    // println("~~~~ ${it.chapter} ~~~~")
                    println("~~~~ ${it.text} ~~~~")
                    println("~~~~ ${it.lineList} ~~~~")

                    page.ready = true
                    onPageReady?.invoke(0)
                }, { it.printStackTrace() })
                .addTo(mCompositeDisposable)
    }

    // todo 非章节间翻页不需要重新请求
    fun next(target: Page, next: Page) {
        Single.just(this).source().chapters().text()
                .map {
                    next.apply {
                        chapter = it

                        index = this@Book.chapterIndex

                        isReverse = false
                        start = max(0, min(it.length.toLong(), target.end))
                        isFirst = index == 0 && start == 0L
                        text = it.substring(start.toInt(), min(it.length, start.toInt() + Config.pagePreCount))
                        indent = start == 0L
                                || Config.REGEX_SEGMENT_SUFFIX.toRegex().matches(text)
                                || Config.REGEX_SEGMENT_PREFIX.toRegex().matches(it.substring(0, start.toInt()))

                        text2line(this@Book)
                        line2Word()
                    }
                }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    println("~~~~ $it ~~~~")
                    // println("~~~~ ${it.chapter} ~~~~")
                    println("~~~~ ${it.text} ~~~~")
                    println("~~~~ ${it.lineList} ~~~~")

                    next.ready = true
                    onPageReady?.invoke(1)
                }, { it.printStackTrace() })
                .addTo(mCompositeDisposable)
    }

    fun last(target: Page, last: Page) {
        Single.just(this).source().chapters().text()
                .map {
                    last.apply {
                        chapter = it

                        index = this@Book.chapterIndex

                        isReverse = true
                        end = max(0, min(it.length.toLong(), target.start))
                        isLast = index >= this@Book.chapterCount - 1 && end == it.length.toLong()
                        val start = max(0, end.toInt() - Config.pagePreCount)
                        text = it.substring(start, end.toInt())
                        indent = start == 0
                                || Config.REGEX_SEGMENT_SUFFIX.toRegex().matches(text)
                                || Config.REGEX_SEGMENT_PREFIX.toRegex().matches(it.substring(0, start))
                        align = end < it.length
                                && !Config.REGEX_SEGMENT_PREFIX.toRegex().matches(text)
                                && !Config.REGEX_SEGMENT_SUFFIX.toRegex().matches(it.substring(end.toInt()))
                        suffix = end < it.length
                                && Config.REGEX_WORD.toRegex().matches(it.substring(end.toInt() - 1, end.toInt() + 1))

                        text2line(this@Book)
                        line2Word()
                    }
                }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    println("~~~~ $it ~~~~")
                    // println("~~~~ ${it.chapter} ~~~~")
                    println("~~~~ ${it.text} ~~~~")
                    println("~~~~ ${it.lineList} ~~~~")

                    last.ready = true
                    onPageReady?.invoke(-1)
                }, { it.printStackTrace() })
                .addTo(mCompositeDisposable)
    }

    @Ignore
    @IgnoredOnParcel
    private val mCompositeDisposable = CompositeDisposable()
    @Ignore
    @IgnoredOnParcel
    var onPageReady: ((Int) -> Unit)? = null

    private fun Single<Book>.source(): Single<Book> {
        return this.flatMap { book ->
            return@flatMap if (book.isSourceInit) Single.just(book)
            else AppDatabase.getInstance(App.app)
                    .sourceDao()
                    .load(book.src)
                    .map { source -> book.also { it.source = source } }
        }
    }

    private fun Single<Book>.chapters(): Single<Book> {
        return this.flatMap { book ->
            if (!book.isSourceInit) throw NullPointerException("source is null")
            return@flatMap if (book.isChaptersInit) Single.just(book)
            else book.source.chapters(book).map { chapters -> book.also { it.chapters = chapters } }
        }
    }

    private fun Single<Book>.text(): Single<String> {
        return this.flatMap { book ->
            if (!book.isSourceInit || !book.isChaptersInit) throw NullPointerException("source is null")
            val index = max(0, minOf(book.chapters.size, book.chapterIndex))
            return@flatMap book.chapters[index].run {
                book.source.text(this).map { s -> s.also { text = it } }
            }
        }
    }

    private fun Source.chapters(book: Book): Single<List<Chapter>> {
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

    private fun Source.text(chapter: Chapter): Single<String> {
        return Single.just(chapter.url)
                .flatMap { api.get(it, "max-stale=31536000") }
                .map { Jsoup.parse(it) }
                .map { it.single(this.ruleContent, chapter.url) }
    }
}

@Dao
interface BookDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(book: Book): Long

    @Query("SELECT * FROM book ORDER BY lastReadTime DESC")
    fun listAll(): Flowable<List<Book>>

}

data class Chapter(
        val url: String,
        val title: String,
        val bookUrl: String,
        val index: Int,
        val offset: Long = 0,
        val selected: Boolean = false,
        var text: String = "")

class Page {

    var chapter: String = ""

    var index: Int = 0
    var start: Long = 0
    var end: Long = 0

    var isFirst: Boolean = false
    var isLast: Boolean = false

    var isReverse = false

    var indent = false
    var align = false
    var suffix = false

    var text: String = ""

    val lineList = ArrayList<Line>() // 页面文字分行
    val wordList = ArrayList<Word>() // 页面按字词拆分

    var ready: Boolean = false
}

class Line(val src: String, val suffix: String, var y: Int, val lineIndent: Boolean, val lineAlign: Boolean) {

    val text
        get() = src + suffix

    override fun toString(): String {
        return "Line(src='$src', suffix='$suffix', y=$y, lineIndent=$lineIndent, lineAlign=$lineAlign)"
    }

}

class Word(val text: String, val x: Float, val y: Float) {
    fun draw(canvas: Canvas, offsetX: Float, offsetY: Float) {
        canvas.drawText(text, x + offsetX, y + offsetY, Config.textPaint)
    }
}


fun Page.text2line(book: Book) {
    val length = chapter.length
    var y = Config.topPadding // 行文字顶部
    var subText = text.trim { it <= ' ' || it == '　' } // 预加载剩余字符
    val textSp = Config.REGEX_SEGMENT.toRegex().split(subText).dropLastWhile { it.isEmpty() }
    textSp.forEachIndexed { segmentIndex, segment ->
        var segmentStart = 0
        while (segmentStart < segment.length) { // 当前段落还有字符
            if (!isReverse && y + Config.textHeight > Config.height - Config.bottomPadding) { // 正向 & 已排满页面
                end = start + text.indexOf(subText)
                isLast = index >= book.chapters.size - 1 && end >= length
                return
            }
            // 行缩进
            val lineIndent = segmentIndex == 0 && segmentStart == 0
                    && indent || segmentIndex != 0 && segmentStart == 0
            var lineCount = breakText(segment.substring(segmentStart), lineIndent)
            val lineSuffix = if (suffix || lineCount < 0) "-" else ""
            lineCount = Math.abs(lineCount)
            val lineText = segment.substring(segmentStart, segmentStart + lineCount)
            // 分散对齐 - 不是段落的最后一行 | 是页面尾行且页面尾行分散对齐
            val lineAlign = (segmentStart + lineCount < segment.length // 不是段落的尾行
                    || align && segmentIndex == textSp.size - 1 && segmentStart + lineCount >= segment.length)
            val line = Line(lineText.trim { it <= ' ' || it == '　' }, lineSuffix, y - Config.textTop, lineIndent, lineAlign)
            lineList.add(line)

            y += Config.textHeight + Config.lineSpacing
            segmentStart += lineCount
            subText = subText.substring(subText.indexOf(lineText) + lineText.length)
        }
        y += Config.segmentSpacing
    } // 退出字符循环 - 字符已全部装载完

    if (!isReverse && start + text.length >= length) { // 正向 & 还能显示却没有了
        end = length.toLong()
        isLast = index >= book.chapters.size - 1
    } else if (isReverse) { // 反向
        val lines = ArrayList<Line>()
        y = y - Config.segmentSpacing - Config.lineSpacing // 最后一行文字底部 - 去掉多余的空隙
        subText = text // 子字符串 - 计算字符数量

        var ey = y - Config.height + Config.bottomPadding + Config.topPadding // 超出的高度
        for (line in lineList) {
            if (line.y + Config.textTop < ey) { // 底部对齐后去掉顶部超出的行
                subText = subText.substring(subText.indexOf(line.src) + line.src.length)
            } else {
                lines.add(line)
            }
        }
        // 顶部对齐
        ey = if (lines.isEmpty()) -1 else lines.get(0).y - Config.topPadding // 距顶部对齐行偏移量
        for (line in lines) {
            line.y = line.y - ey + Config.textSize
        }
        lineList.clear()
        lineList.addAll(lines)

        start = end - subText.length
        isFirst = index <= 0 && start <= 0
    }
}

private fun breakText(remaining: String, lineIndent: Boolean): Int {
    val count = Config.textPaint.breakText(remaining, true, Config.width
            - Config.leftPadding - Config.rightPadding - if (lineIndent) Config.indent else 0F, null)
    return if (count >= remaining.length) count else countReset(remaining, count, lineIndent)
}


/**
 * 分行衔接调整 - 标点 & 单词 - 只考虑标准情况
 *
 * @return count 数量 负数代表要加 -
 */
private fun countReset(remaining: String, countz: Int, lineIndent: Boolean): Int {
    var count = countz
    if (Config.REGEX_PUNCTUATION_N.toRegex().matches(remaining.substring(count, count + 1))) { // 下一行第一个是标点
        if (!"${Config.REGEX_PUNCTUATION_N}*".toRegex().matches(remaining.substring(count - 2, count))) { // 当前行末两不都是标点 - 可退一个字符去下一行
            count -= 1
            return countReset(remaining, count, lineIndent)
        }
    } else if (Config.REGEX_WORD.toRegex().matches(remaining.substring(count - 1, count + 1))) { // 上一行最后一个字符和下一行第一个字符符合单词形式
        val line = remaining.substring(0, count)
        val matcher = Pattern.compile(Config.REGEX_CHARACTER).matcher(line)
        var groupCount = 0
        var group = ""
        while (matcher.find()) {
            groupCount++
            group = matcher.group()
        }
        val indent = if (lineIndent) Config.indent else 0F
        val lineWidth = Config.width - Config.leftPadding - Config.rightPadding - indent
        val end = line.lastIndexOf(group)
        val textWidth = Config.textPaint.measureText(line.substring(0, end))
        val extraSpace = lineWidth - textWidth // 剩余空间
        val spacing = extraSpace / Math.max(1, groupCount - 1)
        return if (spacing <= Config.wordSpacingSlop) end else 1 - count // 单词间距过大 - 改为 abc- 形式
    }
    return count
}


/**
 * 显示文字
 */
private fun Page.line2Word() {
    for (line in lineList) {
        val indent = if (line.lineIndent) Config.indent else 0F
        if (!line.lineAlign || line.text.length <= 1) { // 不需要分散对齐 | 只有一个字符
            wordList.add(Word(line.text, Config.leftPadding + indent, line.y.toFloat()))
            continue
        }

        val textWidth = Config.textPaint.measureText(line.text)
        val lineWidth = Config.width - Config.leftPadding - Config.rightPadding - indent
        val extraSpace = lineWidth - textWidth // 剩余空间
        if (extraSpace <= 10) { // 没有多余空间 - 不需要分散对齐
            wordList.add(Word(line.text, Config.leftPadding + indent, line.y.toFloat()))
            continue
        }

        val matcher = Pattern.compile(Config.REGEX_CHARACTER).matcher(line.text)
        var wordCount = 0
        while (matcher.find()) {
            wordCount++
        }
        if (wordCount > 1) { // 多个单词 - 词间距
            val workSpacing = extraSpace / (wordCount - 1)
            val startX = Config.leftPadding + indent
            var x: Float
            val sb = StringBuilder()
            var spacingCount = 0
            matcher.reset()
            while (matcher.find()) {
                val word = matcher.group()
                x = startX + Config.textPaint.measureText(sb.toString()) + workSpacing * spacingCount
                wordList.add(Word(word, x, line.y.toFloat()))
                sb.append(word)
                spacingCount++
            }
        } else { // 单个单词 - 字间距
            val workSpacing = extraSpace / (line.text.length - 1)
            val startX = Config.leftPadding + indent
            var x: Float
            for (i in 0 until line.text.length) {
                val word = line.text[i].toString()
                x = startX + Config.textPaint.measureText(line.text.substring(0, i)) + workSpacing * i
                wordList.add(Word(word, x, line.y.toFloat()))
            }
        }
    }
}