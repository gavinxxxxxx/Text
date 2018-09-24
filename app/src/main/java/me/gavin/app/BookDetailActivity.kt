package me.gavin.app

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.chainfor.finance.base.addTo
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import me.gavin.R
import me.gavin.base.BaseActivity
import me.gavin.base.Provider.api
import me.gavin.base.fromJson
import me.gavin.databinding.ActivityDetailBinding
import okio.Okio
import org.jsoup.Jsoup


class BookDetailActivity : BaseActivity<ActivityDetailBinding>() {

    private lateinit var mBook: Book
    private lateinit var mSource: Source

    companion object {
        fun start(context: Context, book: Book) {
            context.startActivity(Intent(context, BookDetailActivity::class.java)
                    .putExtra("book", book))
        }
    }

    override fun getLayoutId() = R.layout.activity_detail

    override fun afterCreate(savedInstanceState: Bundle?) {
        mBook = intent.getParcelableExtra("book") ?: return

        mBinding.includeToolbar.toolbar.title = "书籍详情"
        mBinding.includeToolbar.toolbar.setNavigationIcon(R.drawable.ic_arrow_24dp)
        mBinding.includeToolbar.toolbar.setNavigationOnClickListener { finish() }
        initMenu()

        mBinding.fab.setOnClickListener { ReadActivity.start(this, mBook) }

        mBinding.item = mBook

        source()
    }

    private fun initMenu() {
        mBinding.includeToolbar.toolbar.inflateMenu(R.menu.details)
        val source = mBinding.includeToolbar.toolbar.menu.findItem(R.id.action_source)
        source.title = mBook.srcName
        val srcs = mBook.srcs.split(',')
        val srcNames = mBook.srcNames.split(',')
        srcNames.forEach { source.subMenu.add(it) }
        mBinding.includeToolbar.toolbar.setOnMenuItemClickListener {
            if (it.itemId != R.id.action_source) {
                source.title = it.title
                val index = srcNames.indexOf(source.title.toString())
                mBook.src = srcs[index]
                mBook.srcName = srcNames[index]
                source()
            }
            return@setOnMenuItemClickListener true
        }
    }

    private fun source() {
        Flowable.just("test.json")
                .map { assets.open(it) }
                .map { Okio.buffer(Okio.source(it)).readUtf8() }
                .map { it.fromJson<List<Source>>() }
                .map {
                    val src = mBook.srcs.split(',').first()
                    it.forEach {
                        if (it.url == src) return@map it
                    }
                    throw NullPointerException()
                }

//        AppDatabase.getInstance(this)
//                .sourceDao()
//                .load(mBook.srcs.split(',').first())
                .toObservable()
                .flatMap { it.detail(mBook) }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    mBinding.item = it
                    mBinding.fab.show()
                }, { it.printStackTrace() })
                .addTo(mCompositeDisposable)
    }

    private fun Source.detail(book: Book): Observable<Book> {
        return Observable.just(book.url)
                .flatMap { api.get(it, "max-stale=31536000") }
                .map { Jsoup.parse(it) }
                .map {
                    println(" --------------------------- $it --------------------------")
                    val name = it.single(this.ruleBookName, mBook.url)
                    val author = it.single(this.ruleBookAuthor, mBook.url)
                    val cover = it.single(this.ruleBookCover, mBook.url)
                    val category = it.single(this.ruleBookCategory, mBook.url)
                    val state = it.single(this.ruleBookState, mBook.url)
                    val time = it.single(this.ruleBookLastTime, mBook.url)
                    val chapter = it.single(this.ruleBookLastChapter, mBook.url)
                    val intro = it.single(this.ruleBookIntro, mBook.url)
                    val chapterUrl = it.single(this.ruleBookChapterUrl, mBook.url)
                    if (!name.isEmpty()) book.name = name
                    if (!author.isEmpty()) book.author = author
                    if (!cover.isEmpty()) book.cover = cover
                    if (!category.isEmpty()) book.category = category
                    if (!state.isEmpty()) book.state = state
                    if (!time.isEmpty()) book.updateTime = time
                    if (!chapter.isEmpty()) book.updateChapter = chapter
                    if (!intro.isEmpty()) book.intro = intro
                    if (!chapterUrl.isEmpty()) book.chapterUrl = chapterUrl
                    book.src = this.url
                    book.srcName = this.name
                    return@map book
                }
    }
}
