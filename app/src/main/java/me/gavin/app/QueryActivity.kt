package me.gavin.app

import android.content.Context
import android.content.Intent
import android.os.Bundle
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import me.gavin.R
import me.gavin.base.BaseActivity
import me.gavin.base.BindingAdapter
import me.gavin.base.Provider.api
import me.gavin.base.fromJson
import me.gavin.databinding.LayoutToolbarRecyclerBinding
import me.gavin.util.DisplayUtil
import okio.Okio
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.jsoup.select.Elements
import java.net.URL


class QueryActivity : BaseActivity<LayoutToolbarRecyclerBinding>() {

    private lateinit var mQuery: String

    private val mList = ArrayList<Book>()
    private lateinit var mAdapter: BindingAdapter<Book>

    companion object {
        fun start(context: Context, query: String) {
            context.startActivity(Intent(context, QueryActivity::class.java)
                    .putExtra("query", query))
        }
    }

    override fun getLayoutId() = R.layout.layout_toolbar_recycler

    override fun afterCreate(savedInstanceState: Bundle?) {
        mQuery = intent.getStringExtra("query") ?: "全职法师"

        mBinding.includeToolbar?.run {
            toolbar.title = mQuery
            toolbar.setNavigationIcon(R.drawable.ic_arrow_24dp)
            toolbar.setNavigationOnClickListener { finish() }
        }

        val padding = DisplayUtil.dp2px(4F)
        mBinding.recycler.setPadding(padding, padding, padding, padding)
        mBinding.recycler.clipToPadding = false
        mAdapter = BindingAdapter(this, mList, R.layout.query_activity_item)
        mAdapter.callback = { println(it) }
        mBinding.recycler.adapter = mAdapter

        doQuery(mQuery)
    }

    private fun doQuery(query: String) {
        Flowable.just("test.json")
                .map { assets.open(it) }
                .map { Okio.buffer(Okio.source(it)).readUtf8() }
                .map { it.fromJson<List<Source>>() }
//                .map { AppDatabase.getInstance(this).sourceDao().insertAll(it) }
//                .subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe({
//                    println(it)
//                }, { it.printStackTrace() })
//
//        AppDatabase.getInstance(this)
//                .sourceDao()
//                .listAll()
                .flatMap {
                    val bos = it.mapTo(ArrayList()) { it.query(query) }
                    Flowable.merge(bos)
                }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    println(it)
                    mList.addAll(it)
                    mAdapter.notifyDataSetChanged()
                }, { it.printStackTrace() })
    }

    private fun Source.query(query: String): Flowable<List<Book>> {
        return Observable.just(query)
                .map { ruleQueryUrl.replace("{query}", query) }
                .flatMap { api.get(it, "max-stale=31536000") }
                // .map { it.source().readUtf8() }
                .map { Jsoup.parse(it) }
                .map { it.list(this.ruleQueryList) }
                // .map { it.select(this.ruleQueryList) }
                .flatMap { Observable.fromIterable(it) }
                .map {
                    println(" --------------------------- $it --------------------------")
                    val name = it.single(this.ruleQueryName, this.ruleQueryUrl)
                    val author = it.single(this.ruleQueryAuthor, this.ruleQueryUrl)
                    val cover = it.single(this.ruleQueryCover, this.ruleQueryUrl)
                    val category = it.single(this.ruleQueryCategory, this.ruleQueryUrl)
                    val intro = it.single(this.ruleQueryIntro, this.ruleQueryUrl)
                    val url = it.single(this.ruleQueryBookUrl, this.ruleQueryUrl)
                    Book(name, author, cover, category, intro, url, this.name)
                }
                .toList()
                .toFlowable()
                .subscribeOn(Schedulers.io())
    }

    private fun Element.list(listRule: String): Elements { // todo list 方法优化
        val result = Elements()
        try {
            val ruleAs = listRule.split('@')
            if (ruleAs.size > 1) {
                this.list(ruleAs[0]).forEach {
                    val index = listRule.indexOf('@') + 1
                    val listRuleSub = listRule.substring(index)
                    result.addAll(it.list(listRuleSub))
                }
            } else {
                val ruleEs = listRule.split('!')
                val rulePs = ruleEs[0].split('.')
                when (rulePs[0]) {
                    "children" -> {
                        result.addAll(this.children())
                    }
                    "text" -> {
                        result.addAll(this.getElementsContainingOwnText(rulePs[1]))
                    }
                    "id" -> {
                        result.add(this.getElementById(rulePs[1]))
                    }
                    "class" -> {
                        if (rulePs.size == 2) {
                            result.addAll(this.getElementsByClass(rulePs[1]))
                        } else {
                            result.add(this.getElementsByClass(rulePs[1])[rulePs[2].toInt()])
                        }
                    }
                    "tag" -> {
                        if (rulePs.size == 2) {
                            result.addAll(this.getElementsByTag(rulePs[1]))
                        } else {
                            result.add(this.getElementsByTag(rulePs[1])[rulePs[2].toInt()])
                        }
                    }
                }
                if (ruleEs.size > 1) {
                    val ruleCs = ruleEs[1].split(':')
                    val ruleCss = ruleCs.mapTo(ArrayList()) {
                        val index = it.toInt()
                        if (index < 0) result.size + index else index
                    }
                    val filterResult = result.filterIndexed { index, _ ->
                        ruleCss.contains(index)
                    }
                    result.removeAll(filterResult)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return result
    }

    private fun Element.single(singleRule: String?, url: String): String {
        if (singleRule == null || singleRule.isEmpty()) return ""
        try {
            val ruleAs = singleRule.split('@')
            if (ruleAs.size > 1) {
                val index = singleRule.lastIndexOf('@')
                val singleRuleSub = singleRule.substring(0, index)
                this.list(singleRuleSub).forEach {
                    val result = it.single(ruleAs.last(), url)
                    if (result.isNotEmpty()) return result
                }
            } else {
                return when (singleRule) {
                    "text" -> this.text()
                    "textNode" -> this.wholeText() // todo content 过滤
                    else -> URL(URL(url), this.attr(singleRule)).toString() // 相对路径转绝对路径
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return ""
    }

}