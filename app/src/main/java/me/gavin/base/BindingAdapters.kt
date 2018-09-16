package me.gavin.base

import android.databinding.BindingAdapter
import android.widget.ImageView

import me.gavin.R
import me.gavin.util.ImageLoader

/**
 * dataBinding 数据绑定适配器
 *
 * @author gavin.xiong 2017/8/15
 */
object BindingAdapters {

    @BindingAdapter("imageUrl")
    fun loadImage(imageView: ImageView, url: String?) {
        ImageLoader.loadImage(imageView, url)
    }

    @BindingAdapter("resId")
    fun loadIcon(imageView: ImageView, resId: Int) {
        imageView.setImageResource(if (resId > 0) resId else R.mipmap.ic_launcher)
    }

}