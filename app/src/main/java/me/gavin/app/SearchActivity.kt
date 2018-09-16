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
import me.gavin.base.dataLayer
import me.gavin.databinding.LayoutToolbarRecyclerBinding


class SearchActivity : BaseActivity<LayoutToolbarRecyclerBinding>() {

    private lateinit var mQuery: String

    companion object {
        fun start(context: Context, query: String) {
            context.startActivity(Intent(context, SearchActivity::class.java)
                    .putExtra("query", query))
        }
    }

    override fun getLayoutId() = R.layout.layout_toolbar_recycler

    override fun afterCreate(savedInstanceState: Bundle?) {
        mQuery = intent.getStringExtra("query") ?: return

        mBinding.includeToolbar?.run {
            toolbar.title = mQuery
            toolbar.setNavigationIcon(R.drawable.ic_arrow_24dp)
            toolbar.setNavigationOnClickListener { finish() }
        }

        val adapter = BindingAdapter(this, mutableListOf(""), R.layout.item_search)
        adapter.callback = {
            print(it)
        }

        println(" --------------- ${dataLayer.api} ------------------ ")


        AppDatabase.getInstance(this)
                .sourceDao()
                .listAll()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    println(it)
                }, { it.printStackTrace() })

        val source = Source(
                "ymoxuan",
                "ymoxuan",
                "ymoxuan",
                "ymoxuan",
                "ymoxuan",
                "ymoxuan",
                "ymoxuan",
                "ymoxuan",
                "ymoxuan",
                "ymoxuan",
                "ymoxuan",
                "ymoxuan",
                "ymoxuan",
                "ymoxuan",
                "ymoxuan",
                "ymoxuan",
                "ymoxuan",
                "ymoxuan",
                "ymoxuan",
                "ymoxuan",
                "ymoxuan",
                "ymoxuan",
                "ymoxuan",
                "ymoxuan",
                "ymoxuan",
                true)

        Observable.just(source)
                .map { listOf(it) }
                .map {
                    AppDatabase.getInstance(this)
                            .sourceDao().insertAll(it)
                }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    println(it)
                }, { it.printStackTrace() })
        
        AppDatabase.getInstance(this)
                .sourceDao()
                .listAll()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .flatMap { Flowable.fromIterable(it) }
                .subscribe({
                    println("------query-----$it")
                }, { it.printStackTrace() })
    }
}