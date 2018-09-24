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
import me.gavin.app.entity.*
import me.gavin.base.BaseActivity
import me.gavin.base.Provider.api
import me.gavin.base.fromJson
import me.gavin.databinding.ActivityReadBinding
import okio.Okio
import org.jsoup.Jsoup
import java.util.*


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
                .flatMap { it.chapters(mBook) }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    println(it)
                }, { it.printStackTrace() })
                .addTo(mCompositeDisposable)

    }

    private fun Source.chapters(book: Book): Observable<List<Chapter>> {
        return Observable.just(book.chapterUrl)
                .flatMap { api.get(it, "max-stale=31536000") }
                .map { Jsoup.parse(it) }
                .map { it.list(this.ruleChapterList) }
                .map {
                    it.mapIndexedTo(LinkedList()) { index, element ->
                        println(" --------------------------- $element --------------------------")
                        val url = element.single(this.ruleChapterContentUrl, book.chapterUrl)
                        val name = element.single(this.ruleChapterName, book.chapterUrl)
                        return@mapIndexedTo Chapter(url, name, book.url, index)
                    }
                }
    }

}