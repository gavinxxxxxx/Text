package me.gavin.base

import android.content.Context
import android.databinding.ViewDataBinding
import android.view.View
import me.gavin.BR
import me.gavin.R

/**
 * 这里是萌萌哒注释君
 *
 * @author gavin.xiong 2018/9/16.
 */
class BindingAdapter<T>(context: Context, list: MutableList<T>, layoutId: Int)
    : RecyclerAdapter<T, ViewDataBinding>(context, list, layoutId) {

    var callback: ((T) -> Unit)? = null

    override fun onBind(holder: RecyclerHolder<ViewDataBinding>, position: Int, t: T) {
        holder.binding.setVariable(BR.item, t)
        holder.binding.executePendingBindings()
        if (callback != null) {
            holder.binding.root.findViewById<View>(R.id.item)
                    ?.setOnClickListener {
                        callback?.invoke(t)
                    }
        }
    }
}