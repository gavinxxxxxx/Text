package me.gavin.base

import android.databinding.DataBindingUtil
import android.databinding.ViewDataBinding
import android.os.Bundle
import me.yokeyword.fragmentation.SupportActivity


abstract class BaseActivity<T : ViewDataBinding> : SupportActivity() {

    lateinit var mBinding: T

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = DataBindingUtil.setContentView(this, getLayoutId())
        afterCreate(savedInstanceState)
    }

    protected abstract fun getLayoutId(): Int

    protected abstract fun afterCreate(savedInstanceState: Bundle?)
}