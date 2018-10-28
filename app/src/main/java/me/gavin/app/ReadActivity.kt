package me.gavin.app

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.chainfor.finance.base.addTo
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import me.gavin.R
import me.gavin.app.entity.*
import me.gavin.app.text.Config
import me.gavin.base.App
import me.gavin.base.BaseActivity
import me.gavin.base.Provider.api
import me.gavin.databinding.ActivityReadBinding
import org.jsoup.Jsoup
import java.util.*
import java.util.regex.Pattern
import kotlin.math.max
import kotlin.math.min


class ReadActivity : BaseActivity<ActivityReadBinding>() {

    private lateinit var mBook: Book

    companion object {
        fun start(context: Context, book: Book) {
            context.startActivity(Intent(context, ReadActivity::class.java)
                    .putExtra("book", book))
        }
    }

    override fun getLayoutId() = R.layout.activity_read

    override fun afterCreate(savedInstanceState: Bundle?) {
        mBook = intent.getParcelableExtra("book") ?: return

//        mBook.lastReadTime = System.currentTimeMillis()
//        Flowable.just(mBook)
//                .observeOn(Schedulers.io())
//                .subscribe({
//                    AppDatabase.getInstance(this)
//                            .bookDao()
//                            .insert(it)
//                }, { it.printStackTrace() })
//                .addTo(mCompositeDisposable)

        Observable.just(mBook).curr()
    }

    private fun Observable<Book>.curr() {
        mBook.chapterIndex = 1
        this.source().chapters().text()
                .map {
                    Page(it).apply {
                        index = mBook.chapterIndex

                        isReverse = false
                        start = max(0, min(it.length.toLong(), mBook.chapterOffset))
                        isFirst = index == 0 && start == 0L
                        text = it.substring(start.toInt(), min(it.length, start.toInt() + Config.pagePreCount))
                        indent = start == 0L
                                || Config.REGEX_SEGMENT_SUFFIX.toRegex().matches(text)
                                || Config.REGEX_SEGMENT_PREFIX.toRegex().matches(it.substring(0, start.toInt()))

                        text2line()
                        line2Word()
                    }
                }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    println("~~~~ $it ~~~~")
                    println("~~~~ ${it.chapter} ~~~~")
                    println("~~~~ ${it.text} ~~~~")
                    println("~~~~ ${it.lineList} ~~~~")

                    mBinding.text.page = it
                    mBinding.text.invalidate()
                }, { it.printStackTrace() })
                .addTo(mCompositeDisposable)
    }

    fun Page.text2line() {
        val length = chapter.length
        var y = Config.topPadding // 行文字顶部
        var subText = text.trim { it <= ' ' || it == '　' } // 预加载剩余字符
        val textSp = Config.REGEX_SEGMENT.toRegex().split(subText).dropLastWhile { it.isEmpty() }
        textSp.forEachIndexed { segmentIndex, segment ->
            var segmentStart = 0
            while (segmentStart < segment.length) { // 当前段落还有字符
                if (!isReverse && y + Config.textHeight > Config.height - Config.bottomPadding) { // 正向 & 已排满页面
                    end = start + text.indexOf(subText)
                    isLast = index >= mBook.chapters.size - 1 && end >= length
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
            isLast = index >= mBook.chapters.size - 1
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

    private fun Observable<Book>.source(): Observable<Book> {
        return this.flatMap { book ->
            return@flatMap if (book.isSourceInit) Observable.just(book)
            else AppDatabase.getInstance(App.app)
                    .sourceDao()
                    .load(book.src)
                    .toObservable()
                    .map { source -> book.also { it.source = source } }
        }
    }

    private fun Observable<Book>.chapters(): Observable<Book> {
        return this.flatMap { book ->
            if (!book.isSourceInit) throw NullPointerException("source is null")
            return@flatMap if (book.isChaptersInit) Observable.just(book)
            else book.source.chapters(book).map { chapters -> book.also { it.chapters = chapters } }
        }
    }

    private fun Observable<Book>.text(): Observable<String> {
        return this.flatMap { book ->
            if (!book.isSourceInit || !book.isChaptersInit) throw NullPointerException("source is null")
            val index = max(0, minOf(book.chapters.size, book.chapterIndex))
            return@flatMap book.chapters[index].run {
                book.source.text(this).map { s -> s.also { text = it } }
            }
        }
    }

    private fun Source.chapters(book: Book): Observable<List<Chapter>> {
        return Observable.just(book.chapterUrl)
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

    private fun Source.text(chapter: Chapter): Observable<String> {
        return Observable.just(chapter.url)
                .flatMap { api.get(it, "max-stale=31536000") }
                .map { Jsoup.parse(it) }
                .map { it.single(this.ruleContent, chapter.url) }
    }

}