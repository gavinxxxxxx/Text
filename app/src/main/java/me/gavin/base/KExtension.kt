package com.chainfor.finance.base

import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import java.util.*


fun Disposable.addTo(compositeDisposable: CompositeDisposable) {
    compositeDisposable.add(this)
}

//fun <T> Observable<T>.log(): Observable<T> = this.map { L.d(it) }

inline operator fun Int.rem(blk: () -> Unit) {
    if (Random(System.currentTimeMillis()).nextInt(100) < this) blk()
}