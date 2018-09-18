package me.gavin.app

import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.widget.SearchView
import me.gavin.R
import me.gavin.base.BaseActivity
import me.gavin.databinding.ActivityShelfBinding


class ShelfActivity : BaseActivity<ActivityShelfBinding>() {

    override fun getLayoutId() = R.layout.activity_shelf

    override fun afterCreate(savedInstanceState: Bundle?) {
        mBinding.includeToolbar?.run {
            toolbar.setNavigationIcon(R.drawable.ic_menu_24dp)
            toolbar.setNavigationOnClickListener {
                Snackbar.make(mBinding.recycler, "menu", Snackbar.LENGTH_SHORT).show()
            }
            toolbar.inflateMenu(R.menu.action_search)
            val searchView = toolbar.menu.findItem(R.id.actionSearch)?.actionView as? SearchView ?: return
            searchView.queryHint = "输入书名或作者"
            searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextChange(newText: String) = false
                override fun onQueryTextSubmit(query: String): Boolean {
                    SearchActivity.start(this@ShelfActivity, query)
                    return false
                }
            })
        }
    }

}