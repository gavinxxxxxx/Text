package me.gavin.base

import android.content.Context
import android.databinding.DataBindingUtil
import android.databinding.ViewDataBinding
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup

/**
 * RecyclerView 基类列表适配器
 *
 * @author gavin.xiong 2016/12/9  2016/12/9
 */
abstract class RecyclerAdapter<T, B : ViewDataBinding>(
        protected val mContext: Context,
        protected val mList: MutableList<T>,
        private val layoutId: Int) : RecyclerView.Adapter<RecyclerHolder<B>>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerHolder<B> {
        val bing = DataBindingUtil.inflate<B>(LayoutInflater.from(mContext), layoutId, parent, false)
        return RecyclerHolder(bing)
    }

    override fun onBindViewHolder(holder: RecyclerHolder<B>, position: Int) {
        onBind(holder, position, mList[position])
    }

    override fun getItemCount() = mList.size

    protected abstract fun onBind(holder: RecyclerHolder<B>, position: Int, t: T)

}
