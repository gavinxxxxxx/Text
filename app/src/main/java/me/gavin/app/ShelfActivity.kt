package me.gavin.app

import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.widget.SearchView
import com.chainfor.finance.base.addTo
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import me.gavin.R
import me.gavin.app.entity.Book
import me.gavin.base.BaseActivity
import me.gavin.base.BindingAdapter
import me.gavin.databinding.ActivityShelfBinding


class ShelfActivity : BaseActivity<ActivityShelfBinding>() {

    private val mList = ArrayList<Book>()
    private lateinit var mAdapter: BindingAdapter<Book>

    override fun getLayoutId() = R.layout.activity_shelf

    override fun afterCreate(savedInstanceState: Bundle?) {
        mBinding.includeToolbar.run {
            toolbar.setNavigationIcon(R.drawable.ic_menu_24dp)
            toolbar.setNavigationOnClickListener {
                Snackbar.make(mBinding.recycler, "menu", Snackbar.LENGTH_SHORT).show()
            }
            toolbar.inflateMenu(R.menu.action_search)
            val searchView = toolbar.menu.findItem(R.id.actionSearch)?.actionView as? SearchView
                    ?: return
            searchView.queryHint = "输入书名或作者"
            searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextChange(newText: String) = false
                override fun onQueryTextSubmit(query: String): Boolean {
                    QueryActivity.start(this@ShelfActivity, query)
                    return false
                }
            })

        }

        mAdapter = BindingAdapter(this, mList, R.layout.item_shelf_book)
        mAdapter.callback = { ReadActivity.start(this, it) }
        mBinding.recycler.adapter = mAdapter

        listBooks()

        mBinding.includeToolbar.toolbar.setOnClickListener {
            QueryActivity.start(this, "全职法师")
        }
    }

    private fun listBooks() {
        AppDatabase.getInstance(this)
                .bookDao()
                .listAll()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    mList.clear()
                    mList.addAll(it)
                    mAdapter.notifyDataSetChanged()
                }, { it.printStackTrace() })
                .addTo(mCompositeDisposable)
    }

}