package me.gavin.util

import android.widget.ImageView
import com.bumptech.glide.Glide
import me.gavin.R

object ImageLoader {

    private val COLORS = intArrayOf(
            R.color.colorHolder00,
            R.color.colorHolder01,
            R.color.colorHolder02,
            R.color.colorHolder03,
            R.color.colorHolder04,
            R.color.colorHolder05,
            R.color.colorHolder06)

    private val placeholderColor: Int
        get() = COLORS[(Math.random() * COLORS.size).toInt()]

    fun loadImage(imageView: ImageView, url: String?) {
        val colorRes = placeholderColor
        Glide.with(imageView.context)
                .load(url)
                .placeholder(colorRes)
                .error(colorRes)
                .into(imageView)
    }

}
