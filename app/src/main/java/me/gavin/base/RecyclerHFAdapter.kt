package me.gavin.base

import android.content.Context
import android.databinding.DataBindingUtil
import android.databinding.ViewDataBinding
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.StaggeredGridLayoutManager
import android.view.LayoutInflater
import android.view.ViewGroup
import java.util.*

/**
 * Recycler 基类适配器
 * type: 0:TYPE_NORMAL -:TYPE_HEADER + TYPE_FOOTER
 *
 * @author gavin.xiong 2017/8/15
 */
abstract class RecyclerHFAdapter<T, in B : ViewDataBinding>(
        protected val mContext: Context,
        protected val mList: MutableList<T>,
        private val layoutId: Int) : RecyclerView.Adapter<RecyclerHolder<*>>() {

    private val mHeaders = ArrayList<ViewDataBinding>()
    private val mFooters = ArrayList<ViewDataBinding>()

    fun addHeader(binding: ViewDataBinding) {
        this.mHeaders.add(binding)
    }

    fun addFooter(binding: ViewDataBinding) {
        this.mFooters.add(binding)
    }

    override fun getItemViewType(position: Int): Int {
        return when {
            position < mHeaders.size -> -1 - position // 以 -1 作为 header 的第一个下标
            position >= mHeaders.size + mList.size -> 1 + position - mHeaders.size - mList.size // 以 1 作为 footer 的第一个下标
            else -> 0
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerHolder<*> {
        return when {
            viewType < 0 -> RecyclerHolder(mHeaders[-1 - viewType]) // header
            viewType > 0 -> RecyclerHolder(mFooters[viewType - 1]) // footer
            else -> RecyclerHolder<B>(DataBindingUtil.inflate( // 0: normal
                    LayoutInflater.from(mContext), layoutId, parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerHolder<*>, position: Int) {
        if (getItemViewType(position) == 0) {
            val realPosition = holder.adapterPosition - mHeaders.size
            onBind(holder as RecyclerHolder<B>, realPosition, mList[realPosition])
        }
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        val layoutManager = recyclerView.layoutManager
        if (layoutManager != null && layoutManager is GridLayoutManager) {
            layoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                override fun getSpanSize(position: Int): Int {
                    return if (getItemViewType(position) == 0) 1 else layoutManager.spanCount
                }
            }
        }
    }

    override fun onViewAttachedToWindow(holder: RecyclerHolder<*>) {
        super.onViewAttachedToWindow(holder)
        val lp = holder.itemView.layoutParams ?: return
        if (lp.javaClass == RecyclerView.LayoutParams::class.java) {
            lp.width = RecyclerView.LayoutParams.MATCH_PARENT // 线性布局头尾全屏 (仅纵向有效，且所有 item 并非头尾有效)
        } else if (lp is StaggeredGridLayoutManager.LayoutParams) {
            lp.isFullSpan = getItemViewType(holder.layoutPosition) != 0 || lp.isFullSpan
        }
    }

    override fun getItemCount() = mHeaders.size + mList.size + mFooters.size

    protected abstract fun onBind(holder: RecyclerHolder<B>, position: Int, t: T)

}
