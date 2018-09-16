package me.gavin.base

import android.databinding.ViewDataBinding
import android.support.v7.widget.RecyclerView

class RecyclerHolder<out B : ViewDataBinding>(val binding: B) : RecyclerView.ViewHolder(binding.root)