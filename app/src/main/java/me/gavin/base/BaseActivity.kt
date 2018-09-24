package me.gavin.base

import android.databinding.DataBindingUtil
import android.databinding.ViewDataBinding
import android.os.Bundle
import io.reactivex.disposables.CompositeDisposable
import me.yokeyword.fragmentation.SupportActivity


abstract class BaseActivity<T : ViewDataBinding> : SupportActivity() {

    protected val mCompositeDisposable = CompositeDisposable()

    lateinit var mBinding: T

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = DataBindingUtil.setContentView(this, getLayoutId())
        afterCreate(savedInstanceState)
    }

    protected abstract fun getLayoutId(): Int

    protected abstract fun afterCreate(savedInstanceState: Bundle?)

    override fun onDestroy() {
        super.onDestroy()
        mCompositeDisposable.dispose()
    }
}